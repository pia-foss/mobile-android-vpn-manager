package com.kape.vpnprotocol.domain.usecases.wireguard

import com.kape.vpnprotocol.data.externals.common.ICacheProtocol
import com.kape.vpnprotocol.data.externals.common.INetworkClient
import com.kape.vpnprotocol.data.externals.common.ISerializer
import com.kape.vpnprotocol.data.externals.wireguard.ICacheWireguardKeys
import com.kape.vpnprotocol.data.models.WireguardAddKeyResponse
import com.kape.vpnprotocol.presenters.VPNProtocolError
import com.kape.vpnprotocol.presenters.VPNProtocolErrorCode
import io.ktor.util.encodeBase64

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

internal class PerformWireguardAddKeyRequest(
    private val networkClient: INetworkClient,
    private val serializer: ISerializer,
    private val cacheProtocol: ICacheProtocol,
    private val cacheKeys: ICacheWireguardKeys,
) : IPerformWireguardAddKeyRequest {

    companion object {
        private const val ADD_KEY_PATH = "addKey"
        private const val HEADER_AUTHORIZATION_KEY = "Authorization"
        private const val PARAMETER_PUBLIC_KEY_KEY = "pubkey"
    }

    // region IPerformWireguardAddKeyRequest
    override suspend fun invoke(): Result<WireguardAddKeyResponse> {
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
        val publicKeyBase64 = keyPair.getPublicKeyBase64().getOrElse {
            return Result.failure(
                VPNProtocolError(code = VPNProtocolErrorCode.PRIVATE_KEY_NOT_READY)
            )
        }

        val vpnTokenBase64 = protocolConfiguration.wireguardClientConfiguration.token.encodeBase64()
        val response = networkClient.performGetRequest(
            host = protocolConfiguration.wireguardClientConfiguration.server.ip,
            port = protocolConfiguration.wireguardClientConfiguration.server.port,
            path = ADD_KEY_PATH,
            headers = listOf(
                Pair(HEADER_AUTHORIZATION_KEY, "Basic $vpnTokenBase64")
            ),
            parameters = listOf(
                Pair(PARAMETER_PUBLIC_KEY_KEY, publicKeyBase64)
            ),
            certificate = protocolConfiguration.wireguardClientConfiguration.pinningCertificate,
            commonName = protocolConfiguration.wireguardClientConfiguration.server.commonOrDistinguishedName
        ).getOrElse {
            return Result.failure(it)
        }
        return serializer.deserialize(value = response)
    }
    // endregion
}
