package com.kape.vpnmanager.data.externals

import com.kape.targetprovider.data.models.TargetProviderServer
import com.kape.targetprovider.data.models.TargetProviderServerList
import com.kape.targetprovider.presenters.TargetProviderAPI
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.presenters.VPNManagerError
import com.kape.vpnmanager.presenters.VPNManagerErrorCode
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

internal class TargetProvider(
    private val targetProviderApi: TargetProviderAPI,
) : ITargetProvider {

    // region ITargetProvider
    override suspend fun getOptimalServer(serverList: ServerList): Result<ServerList.Server> {
        val adaptedServerList = adaptServerList(serverList = serverList).getOrThrow()
        val deferred: CompletableDeferred<Result<ServerList.Server>> = CompletableDeferred()

        targetProviderApi.getOptimalServer(serverList = adaptedServerList) { result ->
            val targetProviderServer = result.getOrThrow()
            val adaptedServer = adaptServer(
                serverList = serverList,
                targetProviderServer = targetProviderServer
            ).getOrThrow()
            deferred.complete(Result.success(adaptedServer))
        }
        return deferred.await()
    }
    // endregion

    // region private
    private fun adaptServerList(serverList: ServerList): Result<TargetProviderServerList> {
        val servers = mutableListOf<TargetProviderServer>()
        serverList.servers.forEach {
            servers.add(TargetProviderServer(ip = it.ip, latency = it.latency))
        }

        return if (servers.isNotEmpty()) {
            Result.success(TargetProviderServerList(servers))
        } else {
            Result.failure(VPNManagerError(code = VPNManagerErrorCode.FAILED_SERVER_SELECTION))
        }
    }

    private fun adaptServer(
        serverList: ServerList,
        targetProviderServer: TargetProviderServer,
    ): Result<ServerList.Server> {
        val server = serverList.servers.firstOrNull { it.ip == targetProviderServer.ip }

        return if (server == null) {
            Result.failure(VPNManagerError(code = VPNManagerErrorCode.FAILED_SERVER_SELECTION))
        } else {
            Result.success(server)
        }
    }
    // endregion
}
