package com.kape.vpnmanager.data.externals

import com.kape.vpnmanager.data.models.Configuration
import com.kape.vpnmanager.data.models.State
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.kape.vpnmanager.presenters.VPNManagerDebugLoggingDependency
import com.kape.vpnmanager.presenters.VPNManagerProtocolByteCountDependency

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

internal class Cache(
    private val protocolByteCountDependency: VPNManagerProtocolByteCountDependency,
    private val debugLoggingDependency: VPNManagerDebugLoggingDependency,
) : ICache {

    private var connectionListeners: List<VPNManagerConnectionListener> = listOf()
    private var state = State(
        configuration = Configuration(null, null),
        hasRequiredPermissionsGranted = false
    )

    override fun setState(state: State): Result<Unit> {
        this.state = state
        return Result.success(Unit)
    }

    override fun getState(): Result<State> =
        Result.success(state)

    override fun setConnectionListeners(
        connectionListeners: List<VPNManagerConnectionListener>,
    ): Result<Unit> {
        this.connectionListeners = connectionListeners
        return Result.success(Unit)
    }

    override fun getConnectionListeners(): Result<List<VPNManagerConnectionListener>> =
        Result.success(connectionListeners)

    override fun getProtocolByteCountDependency(): Result<VPNManagerProtocolByteCountDependency> =
        Result.success(protocolByteCountDependency)

    override fun getDebugLoggingDependency(): Result<VPNManagerDebugLoggingDependency> =
        Result.success(debugLoggingDependency)
}
