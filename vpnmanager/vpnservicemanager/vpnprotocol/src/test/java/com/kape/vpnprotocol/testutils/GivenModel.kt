package com.kape.vpnprotocol.testutils

import com.kape.openvpn.data.models.OpenVpnServerPeerInformation
import com.kape.openvpn.presenters.OpenVpnUserCredentials
import com.kape.vpnmanager.api.OpenVpnSocksProxyDetails
import com.kape.vpnprotocol.data.models.VPNProtocolCipher
import com.kape.vpnprotocol.data.models.VPNProtocolConfiguration
import com.kape.vpnprotocol.data.models.VPNProtocolOpenVpnConfiguration
import com.kape.vpnprotocol.data.models.VPNProtocolServer
import com.kape.vpnprotocol.data.models.VPNProtocolServerPeerInformation
import com.kape.vpnprotocol.data.models.VPNProtocolWireguardConfiguration
import com.kape.vpnprotocol.data.models.VPNTransportProtocol
import com.kape.vpnprotocol.data.models.WireguardAddKeyResponse
import com.kape.vpnprotocol.presenters.VPNProtocolTarget

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

    fun vpnProtocolOpenVpnConfiguration(
        server: VPNProtocolServer = GivenModel.vpnProtocolServer(),
        caCertificate: String = "caCertificate",
        username: String = "username",
        password: String = "password",
        socksProxy: OpenVpnSocksProxyDetails? = null,
    ) =
        VPNProtocolOpenVpnConfiguration(
            server = server,
            serverList = listOf(server),
            caCertificate = caCertificate,
            username = username,
            password = password,
            socksProxy = socksProxy,
            additionalParameters = ""
        )

    fun vpnProtocolWireguardConfiguration(
        server: VPNProtocolServer = GivenModel.vpnProtocolServer(),
        token: String = "token",
        pinningCertificate: String = "pinningCertificate",
    ) =
        VPNProtocolWireguardConfiguration(
            server = server,
            serverList = listOf(server),
            token = token,
            pinningCertificate = pinningCertificate
        )

    fun vpnProtocolConfiguration(
        sessionName: String = "sessionName",
        protocolTarget: VPNProtocolTarget = VPNProtocolTarget.WIREGUARD,
        mtu: Int = 1420,
        allowedIps: List<String> = emptyList(),
        openVpnClientConfiguration: VPNProtocolOpenVpnConfiguration =
            GivenModel.vpnProtocolOpenVpnConfiguration(),
        wireguardClientConfiguration: VPNProtocolWireguardConfiguration =
            GivenModel.vpnProtocolWireguardConfiguration(),
    ) =
        VPNProtocolConfiguration(
            sessionName = sessionName,
            protocolTarget = protocolTarget,
            mtu = mtu,
            allowedIps = allowedIps,
            openVpnClientConfiguration = openVpnClientConfiguration,
            wireguardClientConfiguration = wireguardClientConfiguration
        )

    fun vpnProtocolServerPeerInformation(
        networkInterface: String = "networkInterface",
        gateway: String = "1.1.1.1",
    ) =
        VPNProtocolServerPeerInformation(
            networkInterface = networkInterface,
            gateway = gateway
        )

    fun wireguardAddKeyResponse(
        peerIp: String = "1.1.1.1",
        peerPubKey: String = "peerPubKey",
        serverIp: String = "1.1.1.2",
        serverKey: String = "serverKey",
        serverPort: Int = 1234,
        serverVip: String = "1.1.1.3",
        status: String = "status",
    ) =
        WireguardAddKeyResponse(
            peerIp = peerIp,
            peerPubKey = peerPubKey,
            serverIp = serverIp,
            serverKey = serverKey,
            serverPort = serverPort,
            serverVip = serverVip,
            status = status
        )

    fun openVpnServerPeerInformation(
        address: String = "1.1.1.2",
        gateway: String = "1.1.1.1",
    ) =
        OpenVpnServerPeerInformation(
            address = address,
            gateway = gateway
        )

    fun openVpnUserCredentials(
        username: String = "username",
        password: String = "password",
    ) =
        OpenVpnUserCredentials(
            username = username,
            password = password
        )

    fun vpnProtocolServer(
        ip: String = "1.1.1.1",
        port: Int = 1337,
        commonOrDistinguishedName: String = "commonName",
        transport: VPNTransportProtocol = VPNTransportProtocol.UDP,
        ciphers: List<VPNProtocolCipher> = listOf(VPNProtocolCipher.AES_128_GCM),
    ) =
        VPNProtocolServer(
            ip = ip,
            port = port,
            commonOrDistinguishedName = commonOrDistinguishedName,
            transport = transport,
            ciphers = ciphers
        )

    fun openVpnConfig(
        server: VPNProtocolServer = vpnProtocolServer(),
        caCertificate: String = "caCertificate",
        username: String = "username",
        password: String = "password",
        socksProxy: Pair<String, Int>? = null,
        additionalParameters: String = "",
    ) =
        VPNProtocolOpenVpnConfiguration(
            server = server,
            serverList = listOf(server),
            caCertificate = caCertificate,
            username = username,
            password = password,
            socksProxy = null,
            additionalParameters = additionalParameters
        )

    fun wireguardConfig(
        server: VPNProtocolServer = vpnProtocolServer(),
        token: String = "token",
        pinningCertificate: String = "pinningCertificate",
    ) =
        VPNProtocolWireguardConfiguration(
            server = server,
            serverList = listOf(server),
            token = token,
            pinningCertificate = pinningCertificate
        )

    fun protocolConfiguration(
        sessionName: String = "sessionName",
        protocolTarget: VPNProtocolTarget = VPNProtocolTarget.OPENVPN,
        mtu: Int = 1280,
        allowedIps: List<String> = emptyList(),
        openVpnClientConfiguration: VPNProtocolOpenVpnConfiguration = openVpnConfig(),
        wireguardClientConfiguration: VPNProtocolWireguardConfiguration = wireguardConfig(),
    ) =
        VPNProtocolConfiguration(
            sessionName = sessionName,
            protocolTarget = protocolTarget,
            mtu = mtu,
            allowedIps = allowedIps,
            openVpnClientConfiguration = openVpnClientConfiguration,
            wireguardClientConfiguration = wireguardClientConfiguration
        )
}
