package com.kape.vpnservicemanager.domain.usecases

import com.kape.vpnservicemanager.data.externals.ICacheService
import com.kape.vpnservicemanager.data.externals.IConnectivity
import com.kape.vpnservicemanager.data.externals.ICoroutineContext
import com.kape.vpnservicemanager.data.externals.IProtocol
import com.kape.vpnservicemanager.domain.datasources.IServiceGatewayProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

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

internal class StartReconnectionHandler(
    private val protocol: IProtocol,
    private val connectivity: IConnectivity,
    private val cacheService: ICacheService,
    private val serviceGatewayProtocol: IServiceGatewayProtocol,
    private val coroutineContext: ICoroutineContext,
) : IStartReconnectionHandler {

    companion object {
        private const val RECONNECTION_INTERVAL_MS = 4000L
    }

    // region IStartReconnectionHandler
    override suspend fun invoke(): Result<Unit> {
        val moduleCoroutineContext = coroutineContext.getModuleCoroutineContext().getOrElse {
            return Result.failure(it)
        }

        var knownReconnectionResult: Result<Unit> = Result.success(Unit)
        val scope = CoroutineScope(moduleCoroutineContext)
        scope.async {
            while (cacheService.getService().isSuccess) {
                pingCurrentServer().fold(
                    onSuccess = {
                        if (knownReconnectionResult.isFailure) {
                            knownReconnectionResult = serviceGatewayProtocol.startProtocolReconnection()
                        }
                    },
                    onFailure = {
                        knownReconnectionResult = serviceGatewayProtocol.startProtocolReconnection()
                    }
                )
                delay(RECONNECTION_INTERVAL_MS)
            }
        }
        return Result.success(Unit)
    }
    // endregion

    // region private
    private suspend fun pingCurrentServer(): Result<Unit> {
        val targetServer = protocol.getTargetServer().getOrElse {
            return Result.failure(it)
        }
        return connectivity.isNetworkReachable(host = targetServer.ip)
    }
    // endregion
}
