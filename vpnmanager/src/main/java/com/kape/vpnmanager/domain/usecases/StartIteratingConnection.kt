package com.kape.vpnmanager.usecases

import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import com.kape.vpnmanager.data.externals.IConnectionEventAnnouncer
import com.kape.vpnmanager.data.externals.ITargetProvider
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.data.models.ServerPeerInformation
import com.kape.vpnmanager.presenters.VPNManagerError
import com.kape.vpnmanager.presenters.VPNManagerErrorCode
import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

internal class StartIteratingConnection(
    private val targetProvider: ITargetProvider,
    private val setServer: ISetServer,
    private val startConnection: IStartConnection,
    private val connectionEventAnnouncer: IConnectionEventAnnouncer,
    private val coroutineContext: ICoroutineContext,
) : IStartIteratingConnection {

    // region IStartIteratingConnection
    override suspend operator fun invoke(serverList: ServerList, vpnProtocol: VPNManagerProtocolTarget): Result<ServerPeerInformation> {
        val mutableServerList = serverList.servers.toMutableList()
        var result: Result<ServerPeerInformation> =
            Result.failure(VPNManagerError(code = VPNManagerErrorCode.FAILED_SERVER_SELECTION))
        if (mutableServerList.isEmpty()) {
            return result
        }

        val clientCoroutineContext = coroutineContext.getClientCoroutineContext().getOrThrow()

        while (mutableServerList.isNotEmpty()) {
            val updatedServerList = serverList.copy(servers = mutableServerList)
            val server = targetProvider.getOptimalServer(serverList = updatedServerList).getOrThrow()
            setServer(server = server).getOrThrow()
            result = startConnection()

            result.fold(
                onSuccess = {
                    CoroutineScope(clientCoroutineContext).launch {
                        connectionEventAnnouncer.handleServerConnectAttemptSucceeded(
                            serverIp = server.ip,
                            transportMode = server.transport,
                            vpnProtocol = vpnProtocol
                        )
                    }
                    return result
                },
                onFailure = {
                    CoroutineScope(clientCoroutineContext).launch {
                        connectionEventAnnouncer.handleServerConnectAttemptFailed(
                            serverIp = server.ip,
                            transportMode = server.transport,
                            vpnProtocol = vpnProtocol,
                            throwable = it
                        )
                    }
                    mutableServerList.remove(server)
                }
            )
        }

        return result
    }
    // endregion
}
