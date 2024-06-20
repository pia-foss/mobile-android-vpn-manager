package com.kape.vpnprotocol.domain.usecases.openvpn

import com.kape.vpnprotocol.data.externals.common.ICacheProtocol
import com.kape.vpnprotocol.data.externals.common.IFile
import com.kape.vpnprotocol.data.models.VPNProtocolCipher
import com.kape.vpnprotocol.data.models.VPNTransportProtocol
import com.kape.vpnprotocol.presenters.VPNProtocolError
import com.kape.vpnprotocol.presenters.VPNProtocolErrorCode

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

internal class GenerateOpenVpnSettings(
    private val cacheProtocol: ICacheProtocol,
    private val file: IFile,
) : IGenerateOpenVpnSettings {

    // region IGenerateOpenVpnSettings
    override suspend fun invoke(certificateFilePath: String): Result<List<String>> {
        val protocolConfiguration = cacheProtocol.getProtocolConfiguration().getOrElse {
            return Result.failure(it)
        }

        val managementPath = cacheProtocol.getManagementPath().getOrElse {
            return Result.failure(it)
        }

        val tmpDir = file.createTemporaryDirectory().getOrElse {
            return Result.failure(it)
        }

        val transport = when (protocolConfiguration.openVpnClientConfiguration.server.transport) {
            VPNTransportProtocol.UDP -> "udp"
            VPNTransportProtocol.TCP -> "tcp"
        }

        if (protocolConfiguration.openVpnClientConfiguration.server.ciphers.contains(VPNProtocolCipher.CHA_CHA_20)) {
            return Result.failure(
                VPNProtocolError(code = VPNProtocolErrorCode.UNSUPPORTED_CIPHER_ERROR)
            )
        }

        val dataCiphers = protocolConfiguration.openVpnClientConfiguration.server.ciphers.joinToString(separator = ":") { cipher ->
            when (cipher) {
                VPNProtocolCipher.AES_128_GCM -> "AES-128-GCM"
                VPNProtocolCipher.AES_256_GCM -> "AES-256-GCM"
                VPNProtocolCipher.CHA_CHA_20 -> "CHA-CHA-20"
            }
        }

        val commandLineParams = mutableListOf(
            "--status-version", "3",
            "--machine-readable-output",
            "--management-query-passwords",
            "--management-forget-disconnect",
            "--management-hold",
            "--management", "$managementPath/management", "unix",
            "--tmp-dir", tmpDir,
            "--ca", certificateFilePath,
            "--remote", protocolConfiguration.openVpnClientConfiguration.server.ip, protocolConfiguration.openVpnClientConfiguration.server.port.toString(),
            "--dev", "tun",
            "--auth-user-pass",
            "--client",
            "--proto", transport,
            "--connect-retry", "2", "300",
            "--allow-recursive-routing",
            "--resolv-retry", "infinite",
            "--persist-key",
            "--persist-tun",
            "--nobind",
            "--data-ciphers", dataCiphers,
            "--auth", "SHA256",
            "--auth-nocache",
            "--script-security", "2",
            "--remote-cert-tls", "server",
            "--verb", "3",
            "--mute-replay-warnings"
        )

        if (protocolConfiguration.openVpnClientConfiguration.server.transport == VPNTransportProtocol.UDP) {
            commandLineParams.add("--explicit-exit-notify")
            commandLineParams.add("2")
        }

        commandLineParams.addAll(protocolConfiguration.openVpnClientConfiguration.additionalParameters.split("\\s+".toRegex()).filter { it.isNotBlank() })

        protocolConfiguration.openVpnClientConfiguration.socksProxy?.let {
            commandLineParams.add("--socks-proxy")
            commandLineParams.add(it.clientProxyAddress)
            commandLineParams.add(it.clientProxyPort.toString())
        }

        return Result.success(commandLineParams)
    }
    // endregion
}
