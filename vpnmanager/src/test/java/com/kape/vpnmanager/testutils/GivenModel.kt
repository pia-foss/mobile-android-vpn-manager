package com.kape.vpnmanager.testutils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kape.vpnmanager.api.OpenVpnSocksProxyDetails
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.DnsInformation
import com.kape.vpnmanager.data.models.OpenVpnClientConfiguration
import com.kape.vpnmanager.data.models.ProtocolCipher
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.data.models.ServerPeerInformation
import com.kape.vpnmanager.data.models.TransportProtocol
import com.kape.vpnmanager.data.models.WireguardClientConfiguration
import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation

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

internal object GivenModel {

    fun serverList(servers: List<ServerList.Server> = emptyList()) =
        ServerList(servers = servers)

    fun server(
        ip: String = "1.1.1.1",
        port: Int = 8080,
        commonOrDistinguishedName: String = "commonOrDistinguishedName",
        transport: TransportProtocol = TransportProtocol.UDP,
        ciphers: List<ProtocolCipher> = listOf(ProtocolCipher.AES_128_GCM),
        latency: Long? = null,
    ) =
        ServerList.Server(
            ip = ip,
            port = port,
            commonOrDistinguishedName = commonOrDistinguishedName,
            transport = transport,
            ciphers = ciphers,
            latency = latency,
            dnsInformation = dnsInformation()
        )

    fun openVpnClientConfiguration(
        caCertificate: String = "caCertificate",
        username: String = "username",
        password: String = "password",
        socksProxy: OpenVpnSocksProxyDetails? = null,
    ) =
        OpenVpnClientConfiguration(
            caCertificate = caCertificate,
            username = username,
            password = password,
            socksProxy = socksProxy
        )

    fun wireguardClientConfiguration(
        token: String = "token",
        pinningCertificate: String = "pinningCertificate",
    ) =
        WireguardClientConfiguration(
            token = token,
            pinningCertificate = pinningCertificate
        )

    fun clientConfiguration(
        context: Context,
        sessionName: String = "sessionName",
        configureIntent: PendingIntent = PendingIntent.getBroadcast(context, 99, Intent(), 100),
        protocolTarget: VPNManagerProtocolTarget = VPNManagerProtocolTarget.WIREGUARD,
        mtu: Int = 1280,
        notificationId: Int = 9999,
        notification: Notification = Notification(),
        allowedApplicationPackages: List<String> = emptyList(),
        disallowedApplicationPackages: List<String> = emptyList(),
        allowLocalNetworkAccess: Boolean = false,
        serverList: ServerList = serverList(),
        openVpnClientConfiguration: OpenVpnClientConfiguration = GivenModel.openVpnClientConfiguration(),
        wireguardClientConfiguration: WireguardClientConfiguration = GivenModel.wireguardClientConfiguration(),
    ) =
        ClientConfiguration(
            sessionName = sessionName,
            configureIntent = configureIntent,
            protocolTarget = protocolTarget,
            mtu = mtu,
            notificationId = notificationId,
            notification = notification,
            allowedApplicationPackages = allowedApplicationPackages,
            disallowedApplicationPackages = disallowedApplicationPackages,
            allowLocalNetworkAccess = allowLocalNetworkAccess,
            serverList = serverList,
            openVpnClientConfiguration = openVpnClientConfiguration,
            wireguardClientConfiguration = wireguardClientConfiguration
        )

    fun dnsInformation(
        dnsList: List<String> = emptyList(),
        systemDnsResolverEnabled: Boolean = false,
    ) =
        DnsInformation(
            dnsList = dnsList,
            systemDnsResolverEnabled = systemDnsResolverEnabled
        )

    fun serverPeerInformation(
        networkInterface: String = "tun0",
        gateway: String = "1.1.1.1",
    ) =
        ServerPeerInformation(
            networkInterface = networkInterface,
            gateway = gateway
        )

    fun serviceServerPeerInformation(
        networkInterface: String = "tun0",
        gateway: String = "1.1.1.1",
    ) =
        VPNServiceServerPeerInformation(
            networkInterface = networkInterface,
            gateway = gateway
        )
}
