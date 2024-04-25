package com.kape.openvpn.presenters

import com.kape.openvpn.domain.controllers.IStartProcessController
import com.kape.openvpn.domain.controllers.IStopProcessController
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
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

internal class OpenVpn(
    private val startProcessController: IStartProcessController,
    private val stopProcessController: IStopProcessController,
    private val coroutineContext: ICoroutineContext,
) : OpenVpnAPI {

    private val moduleCoroutineContext: CoroutineContext =
        coroutineContext.getModuleCoroutineContext().getOrThrow()
    private val clientCoroutineContext: CoroutineContext =
        coroutineContext.getClientCoroutineContext().getOrThrow()

    // region OpenVpnAPI
    override fun start(
        commandLineParams: List<String>,
        openVpnProcessEventHandler: OpenVpnProcessEventHandler,
        callback: OpenVpnCallback,
    ) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = startProcessController(
                commandLineParams = commandLineParams,
                openVpnProcessEventHandler = openVpnProcessEventHandler
            )
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
    }

    override fun stop(callback: OpenVpnCallback) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = stopProcessController()
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
    }
    // endregion
}
