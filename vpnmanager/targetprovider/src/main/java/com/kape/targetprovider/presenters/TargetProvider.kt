package com.kape.targetprovider.presenters

import com.kape.targetprovider.data.externals.ICoroutineContext
import com.kape.targetprovider.data.models.TargetProviderServer
import com.kape.targetprovider.data.models.TargetProviderServerList
import com.kape.targetprovider.domain.controllers.IGetOptimalServerController
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

internal class TargetProvider(
    private val getOptimalServerController: IGetOptimalServerController,
    coroutineContext: ICoroutineContext,
) : TargetProviderAPI {

    private val moduleCoroutineContext: CoroutineContext =
        coroutineContext.getModuleCoroutineContext().getOrThrow()
    private val clientCoroutineContext: CoroutineContext =
        coroutineContext.getClientCoroutineContext().getOrThrow()

    // region TargetProviderAPI
    override fun getOptimalServer(
        serverList: TargetProviderServerList,
        callback: TargetProviderResultCallback<TargetProviderServer>,
    ) {
        CoroutineScope(moduleCoroutineContext).launch {
            val result = getOptimalServerController(serverList)
            launch(clientCoroutineContext) {
                callback(result)
            }
        }
    }
    // endregion
}
