package com.kape.vpnmanager.testutils

import android.content.Context
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import com.kape.vpnmanager.data.externals.IConnectionEventAnnouncer
import com.kape.vpnmanager.data.externals.IPermissions
import com.kape.vpnmanager.data.externals.IServiceManager
import com.kape.vpnmanager.data.externals.ITargetProvider
import com.kape.vpnmanager.domain.datasources.ICacheDatasource
import com.kape.vpnmanager.usecases.AddConnectionListener
import com.kape.vpnmanager.usecases.GetServerList
import com.kape.vpnmanager.usecases.GrantPermissions
import com.kape.vpnmanager.usecases.IAddConnectionListener
import com.kape.vpnmanager.usecases.IGetServerList
import com.kape.vpnmanager.usecases.IGrantPermissions
import com.kape.vpnmanager.usecases.IRemoveConnectionListener
import com.kape.vpnmanager.usecases.ISetClientConfiguration
import com.kape.vpnmanager.usecases.ISetServer
import com.kape.vpnmanager.usecases.IStartConnection
import com.kape.vpnmanager.usecases.IStartIteratingConnection
import com.kape.vpnmanager.usecases.RemoveConnectionListener
import com.kape.vpnmanager.usecases.SetClientConfiguration
import com.kape.vpnmanager.usecases.SetServer
import com.kape.vpnmanager.usecases.StartConnection
import com.kape.vpnmanager.usecases.StartIteratingConnection

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

    fun addConnectionListener(
        cacheDatasource: ICacheDatasource = GivenGateway.cacheDatasource(),
    ): IAddConnectionListener =
        AddConnectionListener(
            cacheDatasource = cacheDatasource
        )

    fun removeConnectionListener(
        cacheDatasource: ICacheDatasource = GivenGateway.cacheDatasource(),
    ): IRemoveConnectionListener =
        RemoveConnectionListener(
            cacheDatasource = cacheDatasource
        )

    fun grantPermissions(
        context: Context,
        permissions: IPermissions = GivenExternal.permissions(context = context),
        cacheDatasource: ICacheDatasource = GivenGateway.cacheDatasource(),
    ): IGrantPermissions =
        GrantPermissions(
            permissions = permissions,
            cacheDatasource = cacheDatasource
        )

    fun setClientConfiguration(
        cacheDatasource: ICacheDatasource = GivenGateway.cacheDatasource(),
    ): ISetClientConfiguration =
        SetClientConfiguration(
            cacheDatasource = cacheDatasource
        )

    fun setServer(
        cacheDatasource: ICacheDatasource = GivenGateway.cacheDatasource(),
    ): ISetServer = SetServer(
        cacheDatasource = cacheDatasource
    )

    fun getServerList(
        cacheDatasource: ICacheDatasource = GivenGateway.cacheDatasource(),
    ): IGetServerList =
        GetServerList(
            cacheDatasource = cacheDatasource
        )

    fun startConnection(
        serviceManager: IServiceManager = GivenExternal.serviceManager(),
    ): IStartConnection = StartConnection(
        serviceManager = serviceManager
    )

    fun startIteratingConnection(
        targetProvider: ITargetProvider = GivenExternal.targetProvider(),
        setServer: ISetServer = GivenUsecase.setServer(),
        startConnection: IStartConnection = GivenUsecase.startConnection(),
        connectionEventAnnouncer: IConnectionEventAnnouncer = GivenExternal.connectionEventAnnouncer(),
        coroutineContext: ICoroutineContext = GivenExternal.coroutineContext(),
    ): IStartIteratingConnection =
        StartIteratingConnection(
            targetProvider = targetProvider,
            setServer = setServer,
            startConnection = startConnection,
            connectionEventAnnouncer = connectionEventAnnouncer,
            coroutineContext = coroutineContext
        )
}
