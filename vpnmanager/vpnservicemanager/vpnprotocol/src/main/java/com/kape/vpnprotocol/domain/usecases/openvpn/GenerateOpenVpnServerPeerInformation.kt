package com.kape.vpnprotocol.domain.usecases.openvpn

import com.kape.vpnprotocol.data.externals.openvpn.ICacheOpenVpn
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

internal class GenerateOpenVpnServerPeerInformation(
    private val cacheOpenVpn: ICacheOpenVpn,
) : IGenerateOpenVpnServerPeerInformation {

    // region IGenerateOpenVpnServerPeerInformation
    override suspend fun invoke(): Result<VPNProtocolServerPeerInformation> {
        val openVpnServerPeerInformation =
            cacheOpenVpn.getOpenVpnServerPeerInformation().getOrElse {
                return Result.failure(it)
            }

        try {
            val networkInterface = NetworkInterface.getByInetAddress(
                InetAddress.getByName(openVpnServerPeerInformation.address)
            )
            return Result.success(
                VPNProtocolServerPeerInformation(
                    networkInterface = networkInterface.name,
                    gateway = openVpnServerPeerInformation.gateway
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
