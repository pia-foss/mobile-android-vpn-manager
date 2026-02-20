package com.kape.vpnservicemanager.testutils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kape.vpnmanager.api.OpenVpnSocksProxyDetails
import com.kape.vpnservicemanager.data.models.VPNServiceDnsInformation
import com.kape.vpnservicemanager.data.models.VPNServiceManagerConfiguration
import com.kape.vpnservicemanager.data.models.VPNServiceManagerOpenVpnClientConfiguration
import com.kape.vpnservicemanager.data.models.VPNServiceManagerWireguardClientConfiguration
import com.kape.vpnservicemanager.data.models.VPNServiceProtocolCipher
import com.kape.vpnservicemanager.data.models.VPNServiceServer
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation
import com.kape.vpnservicemanager.data.models.VPNServiceTransportProtocol
import com.kape.vpnservicemanager.presenters.VPNServiceManagerProtocolTarget

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

    fun vpnServiceManagerOpenVpnClientConfiguration(
        server: VPNServiceServer = GivenModel.vpnServiceServer(),
        caCertificate: String = "caCertificate",
        username: String = "username",
        password: String = "password",
        socksProxy: OpenVpnSocksProxyDetails? = null,
    ) =
        VPNServiceManagerOpenVpnClientConfiguration(
            server = server,
            serverList = listOf(server),
            caCertificate = caCertificate,
            username = username,
            password = password,
            socksProxy = socksProxy,
            additionalParameters = ""
        )

    fun vpnServiceManagerWireguardClientConfiguration(
        server: VPNServiceServer = GivenModel.vpnServiceServer(),
        token: String = "token",
        pinningCertificate: String = "pinningCertificate",
    ) =
        VPNServiceManagerWireguardClientConfiguration(
            server = server,
            serverList = listOf(server),
            token = token,
            pinningCertificate = pinningCertificate
        )

    fun vpnServiceManagerConfiguration(
        context: Context,
        sessionName: String = "sessionName",
        configureIntent: PendingIntent = PendingIntent.getBroadcast(context, 99, Intent(), 100),
        protocolTarget: VPNServiceManagerProtocolTarget = VPNServiceManagerProtocolTarget.WIREGUARD,
        mtu: Int = 1280,
        dnsInformation: VPNServiceDnsInformation = GivenModel.vpnServiceDnsInformation(),
        notificationId: Int = 9999,
        notification: Notification = Notification(),
        allowedApplicationPackages: List<String> = emptyList(),
        disallowedApplicationPackages: List<String> = emptyList(),
        allowLocalNetworkAccess: Boolean = false,
        openVpnClientConfiguration: VPNServiceManagerOpenVpnClientConfiguration = GivenModel.vpnServiceManagerOpenVpnClientConfiguration(),
        wireguardClientConfiguration: VPNServiceManagerWireguardClientConfiguration = GivenModel.vpnServiceManagerWireguardClientConfiguration(),
    ) =
        VPNServiceManagerConfiguration(
            sessionName = sessionName,
            configureIntent = configureIntent,
            protocolTarget = protocolTarget,
            mtu = mtu,
            dnsInformation = dnsInformation,
            notificationId = notificationId,
            notification = notification,
            allowedApplicationPackages = allowedApplicationPackages,
            disallowedApplicationPackages = disallowedApplicationPackages,
            allowLocalNetworkAccess = allowLocalNetworkAccess,
            openVpnClientConfiguration = openVpnClientConfiguration,
            wireguardClientConfiguration = wireguardClientConfiguration
        )

    fun vpnServiceDnsInformation(
        dnsList: List<String> = emptyList(),
        systemDnsResolverEnabled: Boolean = false,
    ) =
        VPNServiceDnsInformation(
            dnsList = dnsList,
            systemDnsResolverEnabled = systemDnsResolverEnabled
        )

    fun vpnServiceServerPeerInformation(
        networkInterface: String = "tun0",
        gateway: String = "1.1.1.1",
    ) =
        VPNServiceServerPeerInformation(
            networkInterface = networkInterface,
            gateway = gateway
        )

    fun vpnServiceServer(
        ip: String = "1.1.1.1",
        port: Int = 8080,
        commonOrDistinguishedName: String = "commonOrDistinguishedName",
        transport: VPNServiceTransportProtocol = VPNServiceTransportProtocol.UDP,
        ciphers: List<VPNServiceProtocolCipher> = listOf(VPNServiceProtocolCipher.AES_128_GCM),
    ) =
        VPNServiceServer(
            ip = ip,
            port = port,
            commonOrDistinguishedName = commonOrDistinguishedName,
            transport = transport,
            ciphers = ciphers
        )
}
