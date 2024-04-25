package com.kape.vpnservicemanager.testutils

import android.content.Context
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import com.kape.vpnservicemanager.data.externals.ICache
import com.kape.vpnservicemanager.data.externals.ICacheProtocol
import com.kape.vpnservicemanager.data.externals.ICacheService
import com.kape.vpnservicemanager.data.externals.IConnectivity
import com.kape.vpnservicemanager.data.externals.IProtocol
import com.kape.vpnservicemanager.domain.datasources.IServiceGateway
import com.kape.vpnservicemanager.domain.datasources.IServiceGatewayProtocol
import com.kape.vpnservicemanager.domain.usecases.ClearCache
import com.kape.vpnservicemanager.domain.usecases.GetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.IClearCache
import com.kape.vpnservicemanager.domain.usecases.IGetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.IIsServiceCleared
import com.kape.vpnservicemanager.domain.usecases.IIsServicePresent
import com.kape.vpnservicemanager.domain.usecases.ISetProtocolConfiguration
import com.kape.vpnservicemanager.domain.usecases.ISetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.IStartConnection
import com.kape.vpnservicemanager.domain.usecases.IStartReconnectionHandler
import com.kape.vpnservicemanager.domain.usecases.IStopConnection
import com.kape.vpnservicemanager.domain.usecases.IsServiceCleared
import com.kape.vpnservicemanager.domain.usecases.IsServicePresent
import com.kape.vpnservicemanager.domain.usecases.SetProtocolConfiguration
import com.kape.vpnservicemanager.domain.usecases.SetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.StartConnection
import com.kape.vpnservicemanager.domain.usecases.StartReconnectionHandler
import com.kape.vpnservicemanager.domain.usecases.StopConnection

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

internal object GivenUsecase {

    fun clearCache(
        cache: ICache = GivenExternal.cache(),
    ): IClearCache =
        ClearCache(cache = cache)

    fun isServicePresent(
        cacheService: ICacheService = GivenExternal.cache(),
    ): IIsServicePresent =
        IsServicePresent(cacheService = cacheService)

    fun isServiceCleared(
        cacheService: ICacheService = GivenExternal.cache(),
    ): IIsServiceCleared =
        IsServiceCleared(cacheService = cacheService)

    fun setProtocolConfiguration(
        cacheProtocol: ICacheProtocol = GivenExternal.cache(),
    ): ISetProtocolConfiguration =
        SetProtocolConfiguration(cacheProtocol = cacheProtocol)

    fun startConnection(
        context: Context,
        serviceGateway: IServiceGateway = GivenGateway.serviceGateway(context = context),
    ): IStartConnection =
        StartConnection(serviceGateway = serviceGateway)

    fun startReconnectionHandler(
        context: Context,
        protocol: IProtocol = GivenExternal.protocol(context = context),
        connectivity: IConnectivity = GivenExternal.connectivity(),
        cacheService: ICacheService = GivenExternal.cache(),
        coroutineContext: ICoroutineContext = GivenExternal.coroutineContext(),
        serviceGatewayProtocol: IServiceGatewayProtocol = GivenGateway.serviceGateway(context = context),
    ): IStartReconnectionHandler =
        StartReconnectionHandler(
            protocol = protocol,
            connectivity = connectivity,
            cacheService = cacheService,
            serviceGatewayProtocol = serviceGatewayProtocol,
            coroutineContext = coroutineContext
        )

    fun stopConnection(
        context: Context,
        serviceGateway: IServiceGateway = GivenGateway.serviceGateway(context = context),
    ): IStopConnection =
        StopConnection(serviceGateway = serviceGateway)

    fun setServerPeerInformation(
        cacheProtocol: ICacheProtocol = GivenExternal.cache(),
    ): ISetServerPeerInformation =
        SetServerPeerInformation(
            cacheProtocol = cacheProtocol
        )

    fun getServerPeerInformation(
        cacheProtocol: ICacheProtocol = GivenExternal.cache(),
    ): IGetServerPeerInformation =
        GetServerPeerInformation(
            cacheProtocol = cacheProtocol
        )
}
