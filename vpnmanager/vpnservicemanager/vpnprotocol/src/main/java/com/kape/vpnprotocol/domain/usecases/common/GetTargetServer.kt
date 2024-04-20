/*
 *
 *  *  Copyright (c) "2023" Private Internet Access, Inc.
 *  *
 *  *  This file is part of the Private Internet Access Android Client.
 *  *
 *  *  The Private Internet Access Android Client is free software: you can redistribute it and/or
 *  *  modify it under the terms of the GNU General Public License as published by the Free
 *  *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  *  The Private Internet Access Android Client is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  *  details.
 *  *
 *  *  You should have received a copy of the GNU General Public License along with the Private
 *  *  Internet Access Android Client.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.kape.vpnprotocol.domain.usecases.common

import com.kape.vpnprotocol.data.externals.common.ICacheProtocol
import com.kape.vpnprotocol.data.models.VPNProtocolServer
import com.kape.vpnprotocol.presenters.VPNProtocolTarget

internal class GetTargetServer(
    private val cacheProtocol: ICacheProtocol,
) : IGetTargetServer {

    // region IGetTargetServer
    override suspend fun invoke(): Result<VPNProtocolServer> {
        val protocolConfiguration = cacheProtocol.getProtocolConfiguration().getOrElse {
            return Result.failure(it)
        }

        return when (protocolConfiguration.protocolTarget) {
            VPNProtocolTarget.OPENVPN ->
                Result.success(protocolConfiguration.openVpnClientConfiguration.server)
            VPNProtocolTarget.WIREGUARD ->
                Result.success(protocolConfiguration.wireguardClientConfiguration.server)
        }
    }
    // endregion
}
