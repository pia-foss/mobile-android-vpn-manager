package com.kape.targetprovider.presenters

import com.kape.targetprovider.data.externals.Cache
import com.kape.targetprovider.data.externals.ICache
import com.kape.targetprovider.domain.controllers.GetOptimalServerController
import com.kape.targetprovider.domain.controllers.IGetOptimalServerController
import com.kape.targetprovider.domain.usecases.GetOptimalServer
import com.kape.targetprovider.domain.usecases.IGetOptimalServer
import com.kape.targetprovider.domain.usecases.ISetServerList
import com.kape.targetprovider.domain.usecases.SetServerList
import com.kape.vpnmanager.api.data.externals.CoroutineContext
import com.kape.vpnmanager.api.data.externals.ICoroutineContext

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

/**
 * Builder class responsible for creating an instance of an object conforming to
 * the `TargetProviderAPI` interface.
 */
public class TargetProviderBuilder {
    private var clientCoroutineContext: kotlin.coroutines.CoroutineContext? = null

    /**
     * Sets the coroutine context to use when invoking the API callbacks.
     *
     * @param clientCoroutineContext `CoroutineContext`.
     */
    fun setClientCoroutineContext(
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
    ): TargetProviderBuilder = apply {
        this.clientCoroutineContext = clientCoroutineContext
    }

    /**
     * @return `TargetProviderAPI`.
     */
    fun build(): TargetProviderAPI {
        val clientCoroutineContext = this.clientCoroutineContext
            ?: throw Exception("Client Coroutine Context missing.")

        return initializeModule(
            clientCoroutineContext = clientCoroutineContext
        )
    }

    // region private
    private fun initializeModule(
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
    ): TargetProviderAPI {
        return initializeExternals(clientCoroutineContext = clientCoroutineContext)
    }

    private fun initializeExternals(
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
    ): TargetProviderAPI {
        val cache: ICache = Cache()
        val coroutineContext: ICoroutineContext =
            CoroutineContext(
                clientCoroutineContext = clientCoroutineContext
            )

        return initializeUseCases(
            cache = cache,
            coroutineContext = coroutineContext
        )
    }

    private fun initializeUseCases(
        cache: ICache,
        coroutineContext: ICoroutineContext,
    ): TargetProviderAPI {
        val setServerList: ISetServerList = SetServerList(cache = cache)
        val getOptimalServer: IGetOptimalServer = GetOptimalServer(cache = cache)

        return initializeControllers(
            setServerList = setServerList,
            getOptimalServer = getOptimalServer,
            coroutineContext = coroutineContext
        )
    }

    private fun initializeControllers(
        setServerList: ISetServerList,
        getOptimalServer: IGetOptimalServer,
        coroutineContext: ICoroutineContext,
    ): TargetProviderAPI {
        val getOptimalServerController: IGetOptimalServerController = GetOptimalServerController(
            setServerList = setServerList,
            getOptimalServer = getOptimalServer
        )

        return TargetProvider(
            getOptimalServerController = getOptimalServerController,
            coroutineContext = coroutineContext
        )
    }
    // endregion
}
