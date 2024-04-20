package com.kape.vpnmanager.data.externals

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

internal interface ICache {

    /**
     * @param state `State`.
     *
     * @return `Result<Unit>`.
     */
    fun setState(state: State): Result<Unit>

    /**
     * @return `Result<State>`.
     */
    fun getState(): Result<State>

    /**
     * @param connectionListeners `List<VPNManagerConnectionListener>`.
     *
     * @return `Result<Unit>`.
     */
    fun setConnectionListeners(connectionListeners: List<VPNManagerConnectionListener>): Result<Unit>

    /**
     * @return `Result<List<VPNManagerConnectionListener>>`.
     */
    fun getConnectionListeners(): Result<List<VPNManagerConnectionListener>>

    /**
     * @return `Result<VPNManagerProtocolByteCountDependency>`.
     */
    fun getProtocolByteCountDependency(): Result<VPNManagerProtocolByteCountDependency>

    /**
     * @return `Result<VPNManagerDebugLoggingDependency>`.
     */
    fun getDebugLoggingDependency(): Result<VPNManagerDebugLoggingDependency>
}
