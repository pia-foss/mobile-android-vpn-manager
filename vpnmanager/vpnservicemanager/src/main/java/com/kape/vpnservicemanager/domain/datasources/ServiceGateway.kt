package com.kape.vpnservicemanager.domain.datasources

import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnservicemanager.data.externals.ICacheService
import com.kape.vpnservicemanager.data.externals.IServiceConnection
import com.kape.vpnservicemanager.data.externals.Service
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation
import com.kape.vpnservicemanager.presenters.VPNServiceManagerError
import com.kape.vpnservicemanager.presenters.VPNServiceManagerErrorCode
import kotlinx.coroutines.CompletableDeferred
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

internal class ServiceGateway(
    private val context: Context,
    private val cache: ICacheService,
    private val serviceConnection: IServiceConnection,
) : IServiceGateway, IServiceGatewayProtocol {

    companion object {
        private const val START_SERVICE_TIMEOUT_MS = 10000L
    }

    private val intent = Intent(context, Service::class.java)

    // region IServiceGateway
    override suspend fun startService(): Result<VPNServiceServerPeerInformation> {
        val deferred: CompletableDeferred<Result<VPNServiceServerPeerInformation>> = CompletableDeferred()
        serviceConnection.setServiceConnectionTimeout(serviceConnectionDeferredTimeout = deferred)
        context.bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        return withTimeoutOrNull(timeMillis = START_SERVICE_TIMEOUT_MS) {
            deferred.await()
        } ?: Result.failure(
            VPNServiceManagerError(
                code = VPNServiceManagerErrorCode.SERVICE_CONNECTION_TIMED_OUT,
                error = Error("Service binding timed out")
            )
        )
    }

    override suspend fun stopService(disconnectReason: DisconnectReason): Result<Unit> {
        // Unbind or stop will not trigger any indication to android.content.ServiceConnection.
        // Let's explicitly indicate the disconnection from it.
        val disconnectResult = serviceConnection.stopProtocolConnection(disconnectReason)
        if (cache.isServiceBound()) {
            context.unbindService(serviceConnection)
            // As per the comment above. `unbindService` or `stopService` will not trigger any
            // indication to android.content.ServiceConnection. Which means we have to be
            // explicit about it being cleared after unbinding here.
            cache.clearServiceBound()
        }
        context.stopService(intent)
        return disconnectResult
    }
    // endregion

    // region IServiceGatewayProtocol
    override suspend fun startProtocolReconnection(): Result<Unit> =
        serviceConnection.startProtocolReconnection()
    // endregion
}
