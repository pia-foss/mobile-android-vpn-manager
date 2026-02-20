package com.kape.vpnmanager.data.externals

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnmanager.data.models.Configuration
import com.kape.vpnmanager.data.models.DnsInformation
import com.kape.vpnmanager.data.models.ProtocolCipher
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.data.models.ServerPeerInformation
import com.kape.vpnmanager.data.models.TransportProtocol
import com.kape.vpnmanager.presenters.VPNManagerError
import com.kape.vpnmanager.presenters.VPNManagerErrorCode
import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget
import com.kape.vpnservicemanager.data.models.VPNServiceDnsInformation
import com.kape.vpnservicemanager.data.models.VPNServiceManagerConfiguration
import com.kape.vpnservicemanager.data.models.VPNServiceManagerOpenVpnClientConfiguration
import com.kape.vpnservicemanager.data.models.VPNServiceManagerWireguardClientConfiguration
import com.kape.vpnservicemanager.data.models.VPNServiceProtocolCipher
import com.kape.vpnservicemanager.data.models.VPNServiceServer
import com.kape.vpnservicemanager.data.models.VPNServiceTransportProtocol
import com.kape.vpnservicemanager.presenters.VPNServiceManagerAPI
import com.kape.vpnservicemanager.presenters.VPNServiceManagerProtocolTarget
import kotlinx.coroutines.CompletableDeferred

/*
 *  Copyright (c) 2022 Private Internet Access, Inc.
 *
 *  This file is part of the Private Internet Access Android Client.
 *
 *  The Private Internet Access Android Client is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  The Private Internet Access Android Client is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with the Private
 *  Internet Access Android Client.  If not, see <https://www.gnu.org/licenses/>.
 */

internal class ServiceManager(
    private val serviceManagerApi: VPNServiceManagerAPI,
    private val cache: ICache,
) : IServiceManager {

    // region IServiceManager
    override suspend fun start(): Result<ServerPeerInformation> {
        val configuration = cache.getState().getOrThrow().configuration
        val adaptedConfiguration = adaptConfiguration(configuration = configuration).getOrThrow()
        val deferred: CompletableDeferred<Result<ServerPeerInformation>> = CompletableDeferred()
        serviceManagerApi.startConnection(protocolConfiguration = adaptedConfiguration) { result ->
            deferred.complete(
                result.map {
                    ServerPeerInformation(
                        networkInterface = it.networkInterface,
                        gateway = it.gateway
                    )
                }
            )
        }
        return deferred.await()
    }

    override suspend fun stop(disconnectReason: DisconnectReason): Result<Unit> {
        val deferred: CompletableDeferred<Result<Unit>> = CompletableDeferred()
        serviceManagerApi.stopConnection(disconnectReason) {
            deferred.complete(it)
        }
        return deferred.await()
    }

    override suspend fun getVpnProtocolLogs(protocolTarget: VPNManagerProtocolTarget): Result<List<String>> {
        val adaptedProtocolTarget =
            adaptProtocolTarget(protocolTarget = protocolTarget).getOrThrow()
        val deferred: CompletableDeferred<Result<List<String>>> = CompletableDeferred()
        serviceManagerApi.getVpnProtocolLogs(protocolTarget = adaptedProtocolTarget) {
            deferred.complete(it)
        }
        return deferred.await()
    }
    // endregion

    // region private
    private fun adaptConfiguration(
        configuration: Configuration,
    ): Result<VPNServiceManagerConfiguration> {
        val server = configuration.server
            ?: return Result.failure(
                VPNManagerError(code = VPNManagerErrorCode.UNKNOWN_SERVER_OBJECT)
            )
        val clientConfiguration = configuration.clientConfiguration
            ?: return Result.failure(
                VPNManagerError(code = VPNManagerErrorCode.UNKNOWN_CLIENT_CONFIGURATION)
            )
        val protocolTarget = adaptProtocolTarget(clientConfiguration.protocolTarget).getOrElse {
            return Result.failure(it)
        }
        val dnsInformation = adaptDnsInformation(server.dnsInformation).getOrElse {
            return Result.failure(it)
        }

        val openVpnClientConfiguration = VPNServiceManagerOpenVpnClientConfiguration(
            server = VPNServiceServer(
                ip = server.ip,
                port = server.port,
                commonOrDistinguishedName = server.commonOrDistinguishedName,
                transport = adaptTransportProtocol(transport = server.transport),
                ciphers = adaptProtocolCiphers(ciphers = server.ciphers)
            ),
            serverList = adaptServerList(clientConfiguration.serverList),
            caCertificate = clientConfiguration.openVpnClientConfiguration.caCertificate,
            username = clientConfiguration.openVpnClientConfiguration.username,
            password = clientConfiguration.openVpnClientConfiguration.password,
            socksProxy = clientConfiguration.openVpnClientConfiguration.socksProxy,
            additionalParameters = clientConfiguration.openVpnClientConfiguration.additionalParameters
        )
        val wireguardClientConfiguration = VPNServiceManagerWireguardClientConfiguration(
            server = VPNServiceServer(
                ip = server.ip,
                port = server.port,
                commonOrDistinguishedName = server.commonOrDistinguishedName,
                transport = adaptTransportProtocol(transport = server.transport),
                ciphers = adaptProtocolCiphers(ciphers = server.ciphers)
            ),
            serverList = adaptServerList(clientConfiguration.serverList),
            token = clientConfiguration.wireguardClientConfiguration.token,
            pinningCertificate = clientConfiguration.wireguardClientConfiguration.pinningCertificate
        )
        return Result.success(
            VPNServiceManagerConfiguration(
                sessionName = clientConfiguration.sessionName,
                configureIntent = clientConfiguration.configureIntent,
                protocolTarget = protocolTarget,
                mtu = clientConfiguration.mtu,
                dnsInformation = dnsInformation,
                notificationId = clientConfiguration.notificationId,
                notification = clientConfiguration.notification,
                allowedApplicationPackages = clientConfiguration.allowedApplicationPackages,
                disallowedApplicationPackages = clientConfiguration.disallowedApplicationPackages,
                allowLocalNetworkAccess = clientConfiguration.allowLocalNetworkAccess,
                openVpnClientConfiguration = openVpnClientConfiguration,
                wireguardClientConfiguration = wireguardClientConfiguration
            )
        )
    }

    private fun adaptProtocolTarget(
        protocolTarget: VPNManagerProtocolTarget,
    ): Result<VPNServiceManagerProtocolTarget> =
        when (protocolTarget) {
            VPNManagerProtocolTarget.OPENVPN ->
                Result.success(VPNServiceManagerProtocolTarget.OPENVPN)

            VPNManagerProtocolTarget.WIREGUARD ->
                Result.success(VPNServiceManagerProtocolTarget.WIREGUARD)
        }

    private fun adaptDnsInformation(
        dnsInformation: DnsInformation,
    ): Result<VPNServiceDnsInformation> =
        Result.success(
            VPNServiceDnsInformation(
                dnsList = dnsInformation.dnsList,
                systemDnsResolverEnabled = dnsInformation.systemDnsResolverEnabled
            )
        )

    private fun adaptTransportProtocol(transport: TransportProtocol): VPNServiceTransportProtocol =
        when (transport) {
            TransportProtocol.UDP -> VPNServiceTransportProtocol.UDP
            TransportProtocol.TCP -> VPNServiceTransportProtocol.TCP
        }

    private fun adaptProtocolCiphers(ciphers: List<ProtocolCipher>): List<VPNServiceProtocolCipher> =
        ciphers.map { cipher ->
            when (cipher) {
                ProtocolCipher.AES_128_GCM -> VPNServiceProtocolCipher.AES_128_GCM
                ProtocolCipher.AES_256_GCM -> VPNServiceProtocolCipher.AES_256_GCM
                ProtocolCipher.CHA_CHA_20 -> VPNServiceProtocolCipher.CHA_CHA_20
            }
        }

    private fun adaptServerList(list: ServerList?): List<VPNServiceServer> {
        val servers = mutableListOf<VPNServiceServer>()
        list?.servers?.forEach {
            servers.add(
                VPNServiceServer(
                    ip = it.ip,
                    port = it.port,
                    commonOrDistinguishedName = it.commonOrDistinguishedName,
                    transport = adaptTransportProtocol(it.transport),
                    ciphers = adaptProtocolCiphers(it.ciphers)
                )
            )
        }
        return servers
    }
    // endregion
}
