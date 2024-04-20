package com.kape.vpnprotocol.presenters

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnprotocol.data.externals.common.ICoroutineContext
import com.kape.vpnprotocol.data.models.VPNProtocolConfiguration
import com.kape.vpnprotocol.data.models.VPNProtocolServer
import com.kape.vpnprotocol.data.models.VPNProtocolServerPeerInformation
import com.kape.vpnprotocol.domain.controllers.common.IStartConnectionController
import com.kape.vpnprotocol.domain.controllers.common.IStartReconnectionController
import com.kape.vpnprotocol.domain.controllers.common.IStopConnectionController
import com.kape.vpnprotocol.domain.usecases.common.IGetTargetServer
import com.kape.vpnprotocol.domain.usecases.common.IGetVpnProtocolLogs
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
    private val coroutineContext: ICoroutineContext,
) : VPNProtocolAPI {

    private val moduleCoroutineContext: CoroutineContext =
        coroutineContext.getModuleCoroutineContext().getOrThrow()
    private val clientCoroutineContext: CoroutineContext =
        coroutineContext.getClientCoroutineContext().getOrThrow()

    // region VPNProtocolAPI
    override fun startConnection(
        vpnService: VPNProtocolService,
        protocolConfiguration: VPNProtocolConfiguration,
        serviceConfigurationFileDescriptorProvider: ServiceConfigurationFileDescriptorProvider,
        callback: VPNProtocolResultCallback<VPNProtocolServerPeerInformation>,
    ) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = startConnectionController(
                vpnService = vpnService,
                protocolConfiguration = protocolConfiguration,
                serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProvider
            )
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
    }

    override fun startReconnection(callback: VPNProtocolCallback) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = startReconnectionController()
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
    }

    override fun stopConnection(
        protocolTarget: VPNProtocolTarget,
        disconnectReason: DisconnectReason,
        callback: VPNProtocolCallback,
    ) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = stopConnectionController(
                protocolTarget = protocolTarget,
                disconnectReason = disconnectReason
            )
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
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
    // endregion
}
