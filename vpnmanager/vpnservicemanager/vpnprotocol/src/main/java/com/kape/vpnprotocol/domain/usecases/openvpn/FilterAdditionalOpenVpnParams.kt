package com.kape.vpnprotocol.domain.usecases.openvpn

import com.kape.vpnprotocol.data.models.VPNProtocolConfiguration
import com.kape.vpnprotocol.data.models.VPNProtocolOpenVpnConfiguration

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

internal class FilterAdditionalOpenVpnParams : IFilterAdditionalOpenVpnParams {

    override suspend operator fun invoke(config: VPNProtocolConfiguration): Result<VPNProtocolConfiguration> =
        Result.success(
            VPNProtocolConfiguration(
                sessionName = config.sessionName,
                protocolTarget = config.protocolTarget,
                mtu = config.mtu,
                allowedIps = config.allowedIps,
                openVpnClientConfiguration = filterOpenVpnClientConfiguration(config.openVpnClientConfiguration),
                wireguardClientConfiguration = config.wireguardClientConfiguration
            )
        )

    private fun filterOpenVpnClientConfiguration(openVpnConfig: VPNProtocolOpenVpnConfiguration): VPNProtocolOpenVpnConfiguration =
        VPNProtocolOpenVpnConfiguration(
            server = openVpnConfig.server,
            caCertificate = openVpnConfig.caCertificate,
            username = openVpnConfig.username,
            password = openVpnConfig.password,
            socksProxy = openVpnConfig.socksProxy,
            additionalParameters = filterAdditionalParameters(openVpnConfig.additionalParameters)
        )

    private fun filterAdditionalParameters(additionalParameters: String): String {
        var isParamWhitelisted = false
        return additionalParameters.split("\\s+".toRegex()).filter { p ->
            when {
                p.isBlank() -> false
                p.startsWith("--") -> {
                    isParamWhitelisted = isWhiteListed(p)
                    isParamWhitelisted
                }
                !p.startsWith("--") && isParamWhitelisted -> true
                !p.startsWith("--") && !isParamWhitelisted -> false
                else -> false
            }
        }.joinToString(separator = " ")
    }

    private fun isWhiteListed(param: String): Boolean =
        WHITELIST.any { whitelisted -> param.equals(other = whitelisted, ignoreCase = true) }

    companion object {
        val WHITELIST = listOf(
            "--allow-pull-fqdn",
            "--block-outside-dns",
            "--cert",
            "--client-nat",
            "--comp-lzo",
            "--comp-noadapt",
            "--compress",
            "--connect-retry-max",
            "--fast-io",
            "--fragment",
            "--inactive",
            "--ip-win32",
            "--keepalive",
            "--key",
            "--keysize",
            "--link-mtu",
            "--mssfix",
            "--mtu-test",
            "--ncp-ciphers",
            "--persist-key",
            "--persist-local-ip",
            "--persist-remote-ip",
            "--persist-tun",
            "--ping",
            "--ping-exit",
            "--ping-restart",
            "--prng",
            "--proto-force",
            "--pull",
            "--pull-filter",
            "--push-peer-info",
            "--rcvbuf",
            "--redirect-gateway",
            "--remote-random",
            "--reneg-sec",
            "--replay-window",
            "--route",
            "--route-delay",
            "--route-gateway",
            "--route-method",
            "--route-metric",
            "--service",
            "--sndbuf",
            "--socket-flags",
            "--tls-client",
            "--tls-timeout",
            "--topology",
            "--tun-mtu",
            "--tun-mtu-extra",
            "--windows-driver",
            "--cipher",
            "--pia-signal-settings",
            "--ncp-disable",
            "--ifconfig-ipv6",
            "--route-ipv6",
            "--block-ipv6"
        )
    }
}
