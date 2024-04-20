package com.kape.vpnprotocol.domain.usecases.openvpn

import com.kape.vpnprotocol.data.externals.openvpn.ICacheOpenVpn
import com.kape.vpnprotocol.presenters.VPNProtocolError
import com.kape.vpnprotocol.presenters.VPNProtocolErrorCode
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.Error

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

internal class WaitForOpenVpnProcessConnectedDeferrable(
    private val cacheOpenVpn: ICacheOpenVpn,
) : IWaitForOpenVpnProcessConnectedDeferrable {

    companion object {
        private const val OPENVPN_PROCESS_CONNECTED_TIMEOUT_MS = 10000L
    }

    // region IWaitForOpenVpnProcessConnectedDeferrable
    override suspend fun invoke(): Result<Unit> {
        val deferrable = cacheOpenVpn.getOpenVpnProcessConnectedDeferrable().getOrElse {
            return Result.failure(it)
        }

        return withTimeoutOrNull(timeMillis = OPENVPN_PROCESS_CONNECTED_TIMEOUT_MS) {
            deferrable.await()
        }?.let {
            Result.success(Unit)
        } ?: Result.failure(
            VPNProtocolError(
                code = VPNProtocolErrorCode.CONNECTION_FAILURE,
                error = Error("OpenVpn connection timed out")
            )
        )
    }
    // endregion
}
