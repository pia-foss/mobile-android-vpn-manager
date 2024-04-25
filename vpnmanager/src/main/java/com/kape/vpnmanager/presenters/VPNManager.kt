package com.kape.vpnmanager.presenters

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.ServerPeerInformation
import com.kape.vpnmanager.domain.controllers.IStartConnectionController
import com.kape.vpnmanager.usecases.IAddConnectionListener
import com.kape.vpnmanager.usecases.IGetVpnProtocolLogs
import com.kape.vpnmanager.usecases.IRemoveConnectionListener
import com.kape.vpnmanager.usecases.IStopConnection
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

internal class VPNManager(
    private val startConnectionController: IStartConnectionController,
    private val stopConnectionUseCase: IStopConnection,
    private val addConnectionListenerUseCase: IAddConnectionListener,
    private val removeConnectionListenerUseCase: IRemoveConnectionListener,
    private val getVpnProtocolLogsUseCase: IGetVpnProtocolLogs,
    private val coroutineContext: ICoroutineContext,
) : VPNManagerAPI {

    private val moduleCoroutineContext: CoroutineContext =
        coroutineContext.getModuleCoroutineContext().getOrThrow()
    private val clientCoroutineContext: CoroutineContext =
        coroutineContext.getClientCoroutineContext().getOrThrow()

    // region VPNManagerAPI
    override fun startConnection(
        clientConfiguration: ClientConfiguration,
        callback: VPNManagerResultCallback<ServerPeerInformation>,
    ) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = startConnectionController(clientConfiguration)
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
    }

    override fun stopConnection(callback: VPNManagerCallback) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = stopConnectionUseCase(DisconnectReason.CLIENT_INITIATED)
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
    }

    override fun addConnectionListener(
        listener: VPNManagerConnectionListener,
        callback: VPNManagerCallback,
    ) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = addConnectionListenerUseCase(listener)
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
    }

    override fun removeConnectionListener(
        listener: VPNManagerConnectionListener,
        callback: VPNManagerCallback,
    ) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = removeConnectionListenerUseCase(listener)
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
    }

    override fun getVpnProtocolLogs(
        protocolTarget: VPNManagerProtocolTarget,
        callback: VPNManagerResultCallback<List<String>>,
    ) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = getVpnProtocolLogsUseCase(protocolTarget = protocolTarget)
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
    }
    // endregion
}
