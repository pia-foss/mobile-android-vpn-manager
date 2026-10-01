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
import kotlinx.coroutines.CancellationException
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
        val serviceBinding = serviceConnection.createServiceBinding()
        context.bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        // The connection is registered from here on, whether or not `onServiceConnected` is ever
        // called, and must be unbound to release it.
        cache.setServiceBound()

        val result = try {
            // Bootstrapping and starting the connection happen in this coroutine, so they are
            // covered by the timeout and stop when a stop cancels the attempt.
            withTimeoutOrNull(timeMillis = START_SERVICE_TIMEOUT_MS) {
                val service = serviceBinding.await().getOrElse {
                    return@withTimeoutOrNull Result.failure(it)
                }
                serviceConnection.startServiceConnection(service = service)
            }
        } catch (exception: CancellationException) {
            releasePendingBinding()
            throw exception
        }

        return result ?: run {
            releasePendingBinding()
            Result.failure(
                VPNServiceManagerError(
                    code = VPNServiceManagerErrorCode.SERVICE_CONNECTION_TIMED_OUT,
                    error = Error("Service binding timed out")
                )
            )
        }
    }

    override suspend fun stopService(disconnectReason: DisconnectReason): Result<Unit> {
        // Unbind or stop will not trigger any indication to android.content.ServiceConnection.
        // Let's explicitly indicate the disconnection from it.
        val disconnectResult = serviceConnection.stopProtocolConnection(disconnectReason)
        unbindService()
        context.stopService(intent)
        return disconnectResult
    }
    // endregion

    // region private
    // Undoes a `bindService` whose service never made it into the cache, so the binding can't
    // deliver it later. Once the service is cached, the stop path owns tearing it down: it
    // routes through the protocol's stop controller for the Disconnecting -> Disconnected
    // sequence, then unbinds.
    private fun releasePendingBinding() {
        serviceConnection.clearServiceBinding()
        if (cache.getService().isSuccess) {
            return
        }
        unbindService()
        context.stopService(intent)
    }

    private fun unbindService() {
        if (!cache.isServiceBound()) {
            return
        }
        try {
            context.unbindService(serviceConnection)
        } catch (exception: IllegalArgumentException) {
            // Already unbound.
        }
        // As per the comment above. `unbindService` or `stopService` will not trigger any
        // indication to android.content.ServiceConnection. Which means we have to be
        // explicit about it being cleared after unbinding here.
        cache.clearServiceBound()
    }
    // endregion

    // region IServiceGatewayProtocol
    override suspend fun startProtocolReconnection(): Result<Unit> =
        serviceConnection.startProtocolReconnection()
    // endregion
}
