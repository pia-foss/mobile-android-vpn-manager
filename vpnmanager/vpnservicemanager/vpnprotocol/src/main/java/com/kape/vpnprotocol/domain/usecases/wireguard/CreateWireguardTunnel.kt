package com.kape.vpnprotocol.domain.usecases.wireguard

import com.kape.vpnprotocol.data.externals.common.ICacheProtocol
import com.kape.vpnprotocol.data.externals.common.ICacheService
import com.kape.vpnprotocol.data.externals.wireguard.ICacheWireguardKeys
import com.kape.vpnprotocol.data.externals.wireguard.IWireguard
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

internal class CreateWireguardTunnel(
    private val cacheProtocol: ICacheProtocol,
    private val cacheKeys: ICacheWireguardKeys,
    private val cacheService: ICacheService,
    private val wireguard: IWireguard,
) : ICreateWireguardTunnel {

    // region ICreateWireguardTunnel
    override suspend fun invoke(generatedSettings: String): Result<Int> {
        val protocolConfiguration = cacheProtocol.getProtocolConfiguration().getOrElse {
            return Result.failure(it)
        }
        val addKeyResponse = cacheKeys.getAddKeyResponse().getOrElse {
            return Result.failure(it)
        }
        val serviceConfigurationFileDescriptorProvider =
            cacheService.getServiceFileDescriptorProvider().getOrElse {
                return Result.failure(it)
            }
        val serviceFd = serviceConfigurationFileDescriptorProvider.establish(
            peerIp = addKeyResponse.peerIp
        ).getOrElse {
            return Result.failure(
                VPNProtocolError(code = VPNProtocolErrorCode.PROTOCOL_CONFIGURATION_NOT_READY)
            )
        }

        return wireguard.turnOn(
            tunnelName = protocolConfiguration.sessionName,
            builderParcelFileDescriptorFd = serviceFd,
            settings = generatedSettings
        )
    }
    // endregion
}
