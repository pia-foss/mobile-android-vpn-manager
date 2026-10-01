package com.kape.vpnservicemanager.data.externals

import android.content.ComponentName
import android.content.Context
import android.os.IBinder
import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnmanager.api.data.externals.ConnectionOperationQueue
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation
import com.kape.vpnservicemanager.presenters.VPNServiceManagerError
import com.kape.vpnservicemanager.presenters.VPNServiceManagerErrorCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

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

internal class ServiceConnection(
    private val context: Context,
    private val cache: ICache,
    private val subnet: ISubnet,
    private val protocol: IProtocol,
    private val coroutineContext: ICoroutineContext,
    private val connectionOperationQueue: ConnectionOperationQueue,
) : IServiceConnection {

    // Written on the module coroutine context, completed from `onServiceConnected` on the main thread.
    @Volatile
    private var serviceBinding: CompletableDeferred<Result<Service>>? = null

    // region IServiceConnection
    override fun createServiceBinding(): CompletableDeferred<Result<Service>> =
        CompletableDeferred<Result<Service>>().also {
            serviceBinding = it
        }

    override fun clearServiceBinding() {
        serviceBinding = null
    }

    override suspend fun startServiceConnection(
        service: Service,
    ): Result<VPNServiceServerPeerInformation> {
        // Last check before touching any state: the attempt may have been cancelled while the
        // binding was pending.
        currentCoroutineContext().ensureActive()
        cache.setService(service).getOrElse {
            return Result.failure(it)
        }
        service.bootstrap(
            protocol = protocol,
            subnet = subnet,
            cache = cache,
            onServiceRevoked = ::onServiceRevoked
        ).getOrElse {
            return Result.failure(it)
        }
        return service.startConnection()
    }

    override suspend fun startProtocolReconnection(): Result<Unit> =
        cache.getService().mapCatching {
            it.startReconnection().getOrThrow()
        }

    override suspend fun stopProtocolConnection(disconnectReason: DisconnectReason): Result<Unit> =
        cache.getService().mapCatching {
            it.stopConnection(disconnectReason).getOrThrow()
        }

    // Called on the main thread. It only hands the service over: the connection is started by the
    // coroutine waiting on the binding, so a stop cancelling that coroutine also stops the attempt.
    override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
        val binder = service as? Service.ServiceBinder
        serviceBinding?.complete(
            binder?.let {
                Result.success(it.getService())
            } ?: Result.failure(
                VPNServiceManagerError(
                    code = VPNServiceManagerErrorCode.SERVICE_NOT_READY,
                    error = Error("Unexpected binder on service connected")
                )
            )
        )
    }

    // Called on the main thread. Revocation is ordered with the client's start and stop requests,
    // and supersedes a start still in flight like a stop does.
    private fun onServiceRevoked() {
        connectionOperationQueue.stop(
            operation = {
                cache.getService()
                    .mapCatching {
                        stopConnection(DisconnectReason.CONFIGURATION_ERROR).getOrThrow()
                    }
                    .mapCatching {
                        cache.clear().getOrThrow()
                    }
            },
            onResult = { }
        )
    }

    private suspend fun stopConnection(disconnectReason: DisconnectReason): Result<Unit> {
        val disconnectResult = stopProtocolConnection(disconnectReason)
        if (cache.isServiceBound()) {
            try {
                context.unbindService(this)
            } catch (exception: IllegalArgumentException) {
                // Already unbound.
            }
            cache.clearServiceBound()
        }
        return disconnectResult
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        // Do nothing as we want the service running regardless of the process.

        // From the documentation:
        // Called when a connection to the Service has been lost.
        // This typically happens when the process hosting the service has crashed or been killed.
        // This does not remove the ServiceConnection itself -- this binding to the service will
        // remain active, and you will receive a call to onServiceConnected(ComponentName, IBinder)
        // when the Service is next running.
    }
    // endregion
}
