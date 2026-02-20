package com.kape.vpnservicemanager.data.externals

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnprotocol.data.models.VPNProtocolCipher
import com.kape.vpnprotocol.data.models.VPNProtocolConfiguration
import com.kape.vpnprotocol.data.models.VPNProtocolOpenVpnConfiguration
import com.kape.vpnprotocol.data.models.VPNProtocolServer
import com.kape.vpnprotocol.data.models.VPNProtocolWireguardConfiguration
import com.kape.vpnprotocol.data.models.VPNTransportProtocol
import com.kape.vpnprotocol.presenters.ServiceConfigurationFileDescriptorProvider
import com.kape.vpnprotocol.presenters.VPNProtocolAPI
import com.kape.vpnprotocol.presenters.VPNProtocolService
import com.kape.vpnprotocol.presenters.VPNProtocolTarget
import com.kape.vpnservicemanager.data.models.NetworkDetails
import com.kape.vpnservicemanager.data.models.VPNServiceManagerConfiguration
import com.kape.vpnservicemanager.data.models.VPNServiceProtocolCipher
import com.kape.vpnservicemanager.data.models.VPNServiceServer
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation
import com.kape.vpnservicemanager.data.models.VPNServiceTransportProtocol
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

internal class Protocol(
    private val cache: ICache,
    private val vpnProtocolApi: VPNProtocolAPI,
) : IProtocol {

    // region IProtocol
    override suspend fun startConnection(
        vpnService: VPNProtocolService,
        allowedIps: List<NetworkDetails>,
        serviceConfigurationFileDescriptorProvider: ServiceConfigurationFileDescriptorProvider,
    ): Result<VPNServiceServerPeerInformation> {
        val protocolConfiguration = cache.getProtocolConfiguration().getOrThrow()
        val adaptedConfiguration = adaptConfiguration(
            allowedIps = allowedIps,
            protocolConfiguration = protocolConfiguration
        ).getOrThrow()
        val deferred: CompletableDeferred<Result<VPNServiceServerPeerInformation>> =
            CompletableDeferred()
        vpnProtocolApi.startConnection(
            vpnService = vpnService,
            protocolConfiguration = adaptedConfiguration,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProvider
        ) { result ->
            deferred.complete(
                result.map {
                    VPNServiceServerPeerInformation(
                        networkInterface = it.networkInterface,
                        gateway = it.gateway
                    )
                }
            )
        }
        return deferred.await()
    }

    override suspend fun startReconnection(): Result<Unit> {
        val deferred: CompletableDeferred<Result<Unit>> = CompletableDeferred()
        vpnProtocolApi.startReconnection { result ->
            deferred.complete(result)
        }
        return deferred.await()
    }

    override suspend fun stopConnection(disconnectReason: DisconnectReason): Result<Unit> {
        val protocolTarget = getProtocolTarget().getOrThrow()
        val deferred: CompletableDeferred<Result<Unit>> = CompletableDeferred()
        vpnProtocolApi.stopConnection(
            protocolTarget = protocolTarget,
            disconnectReason = disconnectReason
        ) { result ->
            deferred.complete(result)
        }
        return deferred.await()
    }

    override suspend fun getVpnProtocolLogs(
        protocolTarget: VPNServiceManagerProtocolTarget,
    ): Result<List<String>> {
        val adaptedProtocolTarget =
            adaptProtocolTarget(protocolTarget = protocolTarget).getOrThrow()
        val deferred: CompletableDeferred<Result<List<String>>> = CompletableDeferred()
        vpnProtocolApi.getVpnProtocolLogs(protocolTarget = adaptedProtocolTarget) { result ->
            deferred.complete(result)
        }
        return deferred.await()
    }

    override suspend fun getTargetServer(): Result<VPNServiceServer> {
        val deferred: CompletableDeferred<Result<VPNServiceServer>> = CompletableDeferred()
        vpnProtocolApi.getTargetServer() { result ->
            deferred.complete(
                result.fold(
                    onSuccess = {
                        Result.success(adaptServer(server = it))
                    },
                    onFailure = {
                        Result.failure(it)
                    }
                )
            )
        }
        return deferred.await()
    }
    // endregion

    // region private
    private fun getProtocolTarget(): Result<VPNProtocolTarget> {
        val protocolConfiguration = cache.getProtocolConfiguration().getOrThrow()
        return adaptProtocolTarget(protocolTarget = protocolConfiguration.protocolTarget)
    }

    private fun adaptProtocolTarget(
        protocolTarget: VPNServiceManagerProtocolTarget,
    ): Result<VPNProtocolTarget> =
        when (protocolTarget) {
            VPNServiceManagerProtocolTarget.OPENVPN ->
                Result.success(VPNProtocolTarget.OPENVPN)

            VPNServiceManagerProtocolTarget.WIREGUARD ->
                Result.success(VPNProtocolTarget.WIREGUARD)
        }

    private fun adaptConfiguration(
        allowedIps: List<NetworkDetails>,
        protocolConfiguration: VPNServiceManagerConfiguration,
    ): Result<VPNProtocolConfiguration> {
        val protocolTarget = adaptProtocolTarget(protocolConfiguration.protocolTarget).getOrElse {
            return Result.failure(it)
        }

        val adaptedAllowedIps = allowedIps.map { "${it.address}/${it.prefix}" }
        val openVpnClientConfiguration = VPNProtocolOpenVpnConfiguration(
            server = VPNProtocolServer(
                ip = protocolConfiguration.openVpnClientConfiguration.server.ip,
                port = protocolConfiguration.openVpnClientConfiguration.server.port,
                commonOrDistinguishedName = protocolConfiguration.openVpnClientConfiguration.server.commonOrDistinguishedName,
                transport = adaptTransportProtocol(transport = protocolConfiguration.openVpnClientConfiguration.server.transport),
                ciphers = adaptServiceProtocolCiphers(ciphers = protocolConfiguration.openVpnClientConfiguration.server.ciphers)
            ),
            serverList = adaptServers(protocolConfiguration.openVpnClientConfiguration.serverList),
            caCertificate = protocolConfiguration.openVpnClientConfiguration.caCertificate,
            username = protocolConfiguration.openVpnClientConfiguration.username,
            password = protocolConfiguration.openVpnClientConfiguration.password,
            socksProxy = protocolConfiguration.openVpnClientConfiguration.socksProxy,
            additionalParameters = protocolConfiguration.openVpnClientConfiguration.additionalParameters
        )
        val wireguardClientConfiguration = VPNProtocolWireguardConfiguration(
            server = VPNProtocolServer(
                ip = protocolConfiguration.wireguardClientConfiguration.server.ip,
                port = protocolConfiguration.wireguardClientConfiguration.server.port,
                commonOrDistinguishedName = protocolConfiguration.wireguardClientConfiguration.server.commonOrDistinguishedName,
                transport = adaptTransportProtocol(transport = protocolConfiguration.wireguardClientConfiguration.server.transport),
                ciphers = adaptServiceProtocolCiphers(ciphers = protocolConfiguration.openVpnClientConfiguration.server.ciphers)
            ),
            serverList = adaptServers(protocolConfiguration.wireguardClientConfiguration.serverList),
            token = protocolConfiguration.wireguardClientConfiguration.token,
            pinningCertificate = protocolConfiguration.wireguardClientConfiguration.pinningCertificate
        )
        return Result.success(
            VPNProtocolConfiguration(
                sessionName = protocolConfiguration.sessionName,
                protocolTarget = protocolTarget,
                mtu = protocolConfiguration.mtu,
                allowedIps = adaptedAllowedIps,
                openVpnClientConfiguration = openVpnClientConfiguration,
                wireguardClientConfiguration = wireguardClientConfiguration
            )
        )
    }

    private fun adaptServers(
        servers: List<VPNServiceServer>,
    ): List<VPNProtocolServer> =
        servers.map {
            VPNProtocolServer(
                ip = it.ip,
                port = it.port,
                commonOrDistinguishedName = it.commonOrDistinguishedName,
                transport = adaptTransportProtocol(transport = it.transport),
                ciphers = adaptServiceProtocolCiphers(ciphers = it.ciphers)
            )
        }

    private fun adaptServer(
        server: VPNProtocolServer,
    ): VPNServiceServer =
        VPNServiceServer(
            ip = server.ip,
            port = server.port,
            commonOrDistinguishedName = server.commonOrDistinguishedName,
            transport = adaptTransportProtocol(transport = server.transport),
            ciphers = adaptProtocolCiphers(ciphers = server.ciphers)
        )

    private fun adaptTransportProtocol(transport: VPNServiceTransportProtocol): VPNTransportProtocol =
        when (transport) {
            VPNServiceTransportProtocol.UDP -> VPNTransportProtocol.UDP
            VPNServiceTransportProtocol.TCP -> VPNTransportProtocol.TCP
        }

    private fun adaptTransportProtocol(transport: VPNTransportProtocol): VPNServiceTransportProtocol =
        when (transport) {
            VPNTransportProtocol.UDP -> VPNServiceTransportProtocol.UDP
            VPNTransportProtocol.TCP -> VPNServiceTransportProtocol.TCP
        }

    private fun adaptServiceProtocolCiphers(ciphers: List<VPNServiceProtocolCipher>): List<VPNProtocolCipher> =
        ciphers.map { cipher ->
            when (cipher) {
                VPNServiceProtocolCipher.AES_128_GCM -> VPNProtocolCipher.AES_128_GCM
                VPNServiceProtocolCipher.AES_256_GCM -> VPNProtocolCipher.AES_256_GCM
                VPNServiceProtocolCipher.CHA_CHA_20 -> VPNProtocolCipher.CHA_CHA_20
            }
        }

    private fun adaptProtocolCiphers(ciphers: List<VPNProtocolCipher>): List<VPNServiceProtocolCipher> =
        ciphers.map { cipher ->
            when (cipher) {
                VPNProtocolCipher.AES_128_GCM -> VPNServiceProtocolCipher.AES_128_GCM
                VPNProtocolCipher.AES_256_GCM -> VPNServiceProtocolCipher.AES_256_GCM
                VPNProtocolCipher.CHA_CHA_20 -> VPNServiceProtocolCipher.CHA_CHA_20
            }
        }
    // endregion
}
