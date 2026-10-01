package com.kape.vpnservicemanager.data.externals

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation
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

internal interface IServiceConnection : android.content.ServiceConnection {

    /**
     * Prepares for a `bindService` call. The returned deferred completes with the bound service
     * once `onServiceConnected` is called.
     *
     * @return `CompletableDeferred<Result<Service>>`.
     */
    fun createServiceBinding(): CompletableDeferred<Result<Service>>

    /**
     * Drops the binding created by `createServiceBinding`, so a late `onServiceConnected` for it
     * is ignored.
     */
    fun clearServiceBinding()

    /**
     * Bootstraps the bound service and starts its connection, within the caller's coroutine so
     * the caller cancelling it stops the attempt.
     *
     * @param service `Service`.
     *
     * @return `Result<VPNServiceServerPeerInformation>`.
     */
    suspend fun startServiceConnection(service: Service): Result<VPNServiceServerPeerInformation>

    /**
     * @return `Result<Unit>`.
     */
    suspend fun startProtocolReconnection(): Result<Unit>

    /**
     * @return `Result<Unit>`.
     */
    suspend fun stopProtocolConnection(disconnectReason: DisconnectReason): Result<Unit>
}
