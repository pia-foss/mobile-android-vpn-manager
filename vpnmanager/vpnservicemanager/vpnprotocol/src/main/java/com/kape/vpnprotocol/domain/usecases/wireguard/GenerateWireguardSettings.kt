package com.kape.vpnprotocol.domain.usecases.wireguard

import com.kape.vpnprotocol.data.externals.common.ICacheProtocol
import com.kape.vpnprotocol.data.externals.wireguard.ICacheWireguardKeys
import com.kape.vpnprotocol.presenters.VPNProtocolError
import com.kape.vpnprotocol.presenters.VPNProtocolErrorCode
import com.wireguard.crypto.Key

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

internal class GenerateWireguardSettings(
    private val cacheProtocol: ICacheProtocol,
    private val cacheKeys: ICacheWireguardKeys,
) : IGenerateWireguardSettings {

    // region IGenerateWireguardSettings
    override suspend fun invoke(): Result<String> {
        val protocolConfiguration = cacheProtocol.getProtocolConfiguration().getOrElse {
            return Result.failure(
                VPNProtocolError(code = VPNProtocolErrorCode.PROTOCOL_CONFIGURATION_NOT_READY)
            )
        }
        val keyPair = cacheKeys.getKeyPair().getOrElse {
            return Result.failure(
                VPNProtocolError(code = VPNProtocolErrorCode.KEY_PAIR_NOT_READY)
            )
        }
        val addKeyResponse = cacheKeys.getAddKeyResponse().getOrElse {
            return Result.failure(
                VPNProtocolError(code = VPNProtocolErrorCode.KEY_RESPONSE_NOT_READY)
            )
        }
        val localPrivateKeyHex = keyPair.getPrivateKeyHex().getOrElse {
            return Result.failure(
                VPNProtocolError(code = VPNProtocolErrorCode.PRIVATE_KEY_NOT_READY)
            )
        }

        val serverPublicKeyHex = Key.fromBase64(addKeyResponse.serverKey).toHex()
        val wireguardSettings = StringBuilder()
        wireguardSettings.append("private_key=${localPrivateKeyHex}\n")
        wireguardSettings.append("replace_peers=true\n")
        wireguardSettings.append("public_key=${serverPublicKeyHex}\n")
        protocolConfiguration.allowedIps.forEach {
            wireguardSettings.append("allowed_ip=$it\n")
        }
        wireguardSettings.append("allowed_ip=${addKeyResponse.serverVip}/32\n")
        wireguardSettings.append("endpoint=${protocolConfiguration.wireguardClientConfiguration.server.ip}:${protocolConfiguration.wireguardClientConfiguration.server.port}\n")
        wireguardSettings.append("persistent_keepalive_interval=25\n")
        return Result.success(wireguardSettings.toString())
    }
    // endregion
}
