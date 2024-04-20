package com.kape.vpnprotocol.domain.usecases.wireguard

import com.kape.vpnprotocol.data.externals.wireguard.ICacheWireguardKeys
import com.kape.vpnprotocol.data.models.VPNProtocolServerPeerInformation
import com.kape.vpnprotocol.presenters.VPNProtocolError
import com.kape.vpnprotocol.presenters.VPNProtocolErrorCode
import java.net.InetAddress
import java.net.NetworkInterface

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

internal class GenerateWireguardServerPeerInformation(
    private val cacheKeys: ICacheWireguardKeys,
) : IGenerateWireguardServerPeerInformation {

    // region IGenerateWireguardServerPeerInformation
    override suspend fun invoke(): Result<VPNProtocolServerPeerInformation> {
        val addKeyResponse = cacheKeys.getAddKeyResponse().getOrElse {
            return Result.failure(it)
        }

        try {
            val networkInterface = NetworkInterface.getByInetAddress(
                InetAddress.getByName(addKeyResponse.peerIp)
            )
            return Result.success(
                VPNProtocolServerPeerInformation(
                    networkInterface = networkInterface.name,
                    gateway = addKeyResponse.serverVip
                )
            )
        } catch (throwable: Throwable) {
            return Result.failure(
                VPNProtocolError(
                    code = VPNProtocolErrorCode.NETWORK_INTERFACE_NAME_ERROR,
                    error = Error(throwable.message)
                )
            )
        }
    }
    // endregion
}
