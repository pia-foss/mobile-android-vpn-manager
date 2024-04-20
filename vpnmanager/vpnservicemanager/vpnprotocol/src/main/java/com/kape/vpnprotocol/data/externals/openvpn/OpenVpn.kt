package com.kape.vpnprotocol.data.externals.openvpn

import com.kape.openvpn.presenters.OpenVpnAPI
import com.kape.openvpn.presenters.OpenVpnProcessEventHandler
import kotlinx.coroutines.CompletableDeferred

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

internal class OpenVpn(
    private val openVpnApi: OpenVpnAPI,
) : IOpenVpn {

    // region IOpenVpn
    override suspend fun start(
        commandLineParams: List<String>,
        openVpnProcessEventHandler: OpenVpnProcessEventHandler,
    ): Result<Unit> {
        val deferred: CompletableDeferred<Result<Unit>> = CompletableDeferred()
        openVpnApi.start(
            commandLineParams = commandLineParams,
            openVpnProcessEventHandler = openVpnProcessEventHandler
        ) { result ->
            deferred.complete(result)
        }
        return deferred.await()
    }

    override suspend fun stop(): Result<Unit> {
        val deferred: CompletableDeferred<Result<Unit>> = CompletableDeferred()
        openVpnApi.stop { result ->
            deferred.complete(result)
        }
        return deferred.await()
    }
    // endregion
}
