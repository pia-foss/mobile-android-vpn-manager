package com.kape.vpnprotocol.presenters

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnmanager.api.data.externals.ConnectionOperationQueue
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import com.kape.vpnprotocol.data.models.VPNProtocolConfiguration
import com.kape.vpnprotocol.data.models.VPNProtocolServer
import com.kape.vpnprotocol.data.models.VPNProtocolServerPeerInformation
import com.kape.vpnprotocol.domain.controllers.common.IStartConnectionController
import com.kape.vpnprotocol.domain.controllers.common.IStartReconnectionController
import com.kape.vpnprotocol.domain.controllers.common.IStopConnectionController
import com.kape.vpnprotocol.domain.usecases.common.IGetTargetServer
import com.kape.vpnprotocol.domain.usecases.common.IGetVpnProtocolLogs
import com.kape.vpnprotocol.domain.usecases.common.IUpdateServerList
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

internal class VPNProtocol(
    private val startConnectionController: IStartConnectionController,
    private val startReconnectionController: IStartReconnectionController,
    private val stopConnectionController: IStopConnectionController,
    private val getVpnProtocolLogs: IGetVpnProtocolLogs,
    private val getTargetServer: IGetTargetServer,
    private val updateServerListUseCase: IUpdateServerList,
    private val coroutineContext: ICoroutineContext,
) : VPNProtocolAPI {

    private val moduleCoroutineContext: CoroutineContext =
        coroutineContext.getModuleCoroutineContext().getOrThrow()
    private val clientCoroutineContext: CoroutineContext =
        coroutineContext.getClientCoroutineContext().getOrThrow()
    private val connectionOperationQueue = ConnectionOperationQueue(moduleCoroutineContext)

    companion object {
        internal const val CONNECTION_CANCELLED_MESSAGE =
            "Connection attempt cancelled by stopConnection"
    }

    // region VPNProtocolAPI
    override fun startConnection(
        vpnService: VPNProtocolService,
        protocolConfiguration: VPNProtocolConfiguration,
        serviceConfigurationFileDescriptorProvider: ServiceConfigurationFileDescriptorProvider,
        callback: VPNProtocolResultCallback<VPNProtocolServerPeerInformation>,
    ) {
        connectionOperationQueue.start(
            operation = {
                startConnectionController(
                    vpnService = vpnService,
                    protocolConfiguration = protocolConfiguration,
                    serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProvider
                )
            },
            cancelledResult = { cancelledResult() },
            onResult = { result ->
                CoroutineScope(clientCoroutineContext).launch {
                    callback(result)
                }
            }
        )
    }

    // A reconnection rebuilds the tunnel, so it is ordered with, and superseded by, a stop the
    // same way a start is.
    override fun startReconnection(callback: VPNProtocolCallback) {
        connectionOperationQueue.start(
            operation = { startReconnectionController() },
            cancelledResult = { cancelledResult() },
            onResult = { result ->
                CoroutineScope(clientCoroutineContext).launch {
                    callback(result)
                }
            }
        )
    }

    override fun stopConnection(
        protocolTarget: VPNProtocolTarget,
        disconnectReason: DisconnectReason,
        callback: VPNProtocolCallback,
    ) {
        connectionOperationQueue.stop(
            operation = {
                stopConnectionController(
                    protocolTarget = protocolTarget,
                    disconnectReason = disconnectReason
                )
            },
            // Nothing to tear down when no connection was started since the last stop. This is
            // the case when the service layer is stopped before it got to start the protocol.
            skipWhenIdle = true,
            onResult = { result ->
                CoroutineScope(clientCoroutineContext).launch {
                    callback(result)
                }
            }
        )
    }

    override fun getVpnProtocolLogs(
        protocolTarget: VPNProtocolTarget,
        callback: VPNProtocolResultCallback<List<String>>,
    ) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = getVpnProtocolLogs(protocolTarget = protocolTarget)
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
    }

    override fun getTargetServer(callback: VPNProtocolResultCallback<VPNProtocolServer>) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = getTargetServer()
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
    }

    override fun updateServerList(servers: List<VPNProtocolServer>, callback: VPNProtocolCallback) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = updateServerListUseCase(servers = servers)
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
    }
    // endregion

    // region private
    private fun <T> cancelledResult(): Result<T> =
        Result.failure(
            VPNProtocolError(
                code = VPNProtocolErrorCode.CONNECTION_FAILURE,
                error = Error(CONNECTION_CANCELLED_MESSAGE)
            )
        )
    // endregion
}
