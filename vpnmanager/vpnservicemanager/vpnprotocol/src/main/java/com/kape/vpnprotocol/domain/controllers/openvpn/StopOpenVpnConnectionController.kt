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

package com.kape.vpnprotocol.domain.controllers.openvpn

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnprotocol.domain.usecases.common.IClearCache
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.openvpn.IStopOpenVpnProcess

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

internal class StopOpenVpnConnectionController(
    private val reportConnectivityStatus: IReportConnectivityStatus,
    private val stopOpenVpnProcess: IStopOpenVpnProcess,
    private val clearCache: IClearCache,
) : IStopOpenVpnConnectionController {

    // region IStopOpenVpnConnectionController
    override suspend fun invoke(disconnectReason: DisconnectReason): Result<Unit> {
        // Best-effort cleanup. stopOpenVpnProcess() may fail when stop is invoked from
        // the StartConnectionController failure path before the OpenVPN process was ever
        // started. Regardless of cleanup outcome we must still report the canonical
        // Disconnecting -> Disconnected sequence so clients always see a terminal state.
        val cleanupResult = reportConnectivityStatus(
            connectivityStatus = VPNManagerConnectionStatus.Disconnecting
        )
            .mapCatching {
                stopOpenVpnProcess().getOrThrow()
            }

        // Always emit Disconnected, even if the cleanup chain failed.
        reportConnectivityStatus(
            connectivityStatus = VPNManagerConnectionStatus.Disconnected(disconnectReason)
        )

        // Always clear cache. If cleanup succeeded, surface a clearCache() failure as
        // the result; if cleanup already failed, propagate the original cleanup failure
        // and ignore any clearCache() error (best-effort).
        val clearResult = clearCache()
        return cleanupResult.fold(
            onSuccess = { clearResult },
            onFailure = { Result.failure(it) }
        )
    }
    // endregion
}
