package com.kape.vpnservicemanager.data.externals

import android.content.ComponentName
import android.os.IBinder
import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

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
    private val cache: ICache,
    private val subnet: ISubnet,
    private val protocol: IProtocol,
    private val coroutineContext: ICoroutineContext,
) : IServiceConnection {

    private lateinit var serviceConnectionDeferredTimeout: CompletableDeferred<Result<VPNServiceServerPeerInformation>>
    private val moduleCoroutineContext: CoroutineContext =
        coroutineContext.getModuleCoroutineContext().getOrThrow()

    // region IServiceConnection
    override suspend fun setServiceConnectionTimeout(
        serviceConnectionDeferredTimeout: CompletableDeferred<Result<VPNServiceServerPeerInformation>>,
    ): Result<Unit> {
        this.serviceConnectionDeferredTimeout = serviceConnectionDeferredTimeout
        return Result.success(Unit)
    }

    override suspend fun startProtocolReconnection(): Result<Unit> =
        cache.getService().mapCatching {
            it.startReconnection().getOrThrow()
        }

    override suspend fun stopProtocolConnection(disconnectReason: DisconnectReason): Result<Unit> =
        cache.getService().mapCatching {
            it.stopConnection(disconnectReason).getOrThrow()
        }

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        cache.setServiceBound()
        val scope = CoroutineScope(moduleCoroutineContext)
        scope.launch {
            val binder = service as Service.ServiceBinder
            serviceConnectionDeferredTimeout.complete(
                cache.setService(binder.getService())
                    .mapCatching {
                        cache.getService().getOrThrow()
                    }
                    .mapCatching {
                        it.bootstrap(protocol = protocol, subnet = subnet, cache = cache).getOrThrow()
                        it.startConnection().getOrThrow()
                    }
            )
        }
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
