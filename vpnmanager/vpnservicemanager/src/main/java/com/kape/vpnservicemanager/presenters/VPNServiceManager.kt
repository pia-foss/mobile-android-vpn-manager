package com.kape.vpnservicemanager.presenters

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import com.kape.vpnservicemanager.data.models.VPNServiceManagerConfiguration
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation
import com.kape.vpnservicemanager.domain.controllers.IStartConnectionController
import com.kape.vpnservicemanager.domain.controllers.IStopConnectionController
import com.kape.vpnservicemanager.domain.usecases.IGetVpnProtocolLogs
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

internal class VPNServiceManager(
    private val startConnectionController: IStartConnectionController,
    private val stopConnectionController: IStopConnectionController,
    private val getVpnProtocolLogsUseCase: IGetVpnProtocolLogs,
    private val coroutineContext: ICoroutineContext,
) : VPNServiceManagerAPI {

    private val moduleCoroutineContext: CoroutineContext =
        coroutineContext.getModuleCoroutineContext().getOrThrow()
    private val clientCoroutineContext: CoroutineContext =
        coroutineContext.getClientCoroutineContext().getOrThrow()

    // region VPNServiceManagerAPI
    override fun startConnection(
        protocolConfiguration: VPNServiceManagerConfiguration,
        callback: VPNServiceManagerResultCallback<VPNServiceServerPeerInformation>,
    ) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = startConnectionController(protocolConfiguration = protocolConfiguration)
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
    }

    override fun stopConnection(disconnectReason: DisconnectReason, callback: VPNServiceManagerCallback) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = stopConnectionController(disconnectReason)
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
    }

    override fun getVpnProtocolLogs(
        protocolTarget: VPNServiceManagerProtocolTarget,
        callback: VPNServiceManagerResultCallback<List<String>>,
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
