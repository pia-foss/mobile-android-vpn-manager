package com.kape.vpnprotocol.domain.usecases.wireguard

import com.kape.vpnprotocol.data.externals.common.ICacheService
import com.kape.vpnprotocol.data.externals.wireguard.ICacheWireguard
import com.kape.vpnprotocol.data.externals.wireguard.IWireguard
import com.kape.vpnprotocol.presenters.VPNProtocolError
import com.kape.vpnprotocol.presenters.VPNProtocolErrorCode
import com.kape.vpnprotocol.presenters.VPNProtocolService

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

internal class ProtectWireguardTunnelSocket(
    private val cacheWireguard: ICacheWireguard,
    private val cacheService: ICacheService,
    private val wireguard: IWireguard,
) : IProtectWireguardTunnelSocket {

    // region IProtectWireguardTunnelSocket
    override suspend fun invoke(): Result<Unit> {
        val vpnService = cacheService.getVpnProtocolService().getOrElse {
            return Result.failure(it)
        }
        val tunnelHandle = cacheWireguard.getWireguardTunnelHandle().getOrElse {
            return Result.failure(it)
        }
        val socketV4 = wireguard.socketV4(tunnelHandle = tunnelHandle).getOrElse {
            return Result.failure(it)
        }
        val socketV6 = wireguard.socketV6(tunnelHandle = tunnelHandle).getOrElse {
            return Result.failure(it)
        }
        protect(vpnService, socketV4).getOrElse {
            return Result.failure(it)
        }
        protect(vpnService, socketV6).getOrElse {
            return Result.failure(it)
        }
        return Result.success(Unit)
    }
    // endregion

    // region private
    private fun protect(vpnService: VPNProtocolService, socket: Int): Result<Unit> {
        val isProtected = vpnService.serviceProtect(socket).getOrElse {
            return Result.failure(it)
        }
        if (!isProtected) {
            return Result.failure(VPNProtocolError(code = VPNProtocolErrorCode.PROTOCOL_SERVICE_ERROR))
        }
        return Result.success(Unit)
    }
    // endregion
}
