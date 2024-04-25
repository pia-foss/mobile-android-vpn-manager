package com.kape.vpnmanager.testutils

import android.content.Context
import com.kape.targetprovider.presenters.TargetProviderAPI
import com.kape.targetprovider.presenters.TargetProviderBuilder
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import com.kape.vpnmanager.data.externals.Cache
import com.kape.vpnmanager.data.externals.ConnectionEventAnnouncer
import com.kape.vpnmanager.data.externals.ICache
import com.kape.vpnmanager.data.externals.IConnectionEventAnnouncer
import com.kape.vpnmanager.data.externals.IPermissions
import com.kape.vpnmanager.data.externals.IServiceManager
import com.kape.vpnmanager.data.externals.ITargetProvider
import com.kape.vpnmanager.data.externals.Permissions
import com.kape.vpnmanager.data.externals.ServiceManager
import com.kape.vpnmanager.data.externals.TargetProvider
import com.kape.vpnmanager.presenters.VPNManagerDebugLoggingDependency
import com.kape.vpnmanager.presenters.VPNManagerPermissionsDependency
import com.kape.vpnmanager.presenters.VPNManagerProtocolByteCountDependency
import com.kape.vpnmanager.testutils.mocks.VPNServiceManagerApiMock
import com.kape.vpnservicemanager.presenters.VPNServiceManagerAPI
import kotlinx.coroutines.Dispatchers
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

internal object GivenExternal {

    fun cache(
        protocolLoggingDependency: VPNManagerProtocolByteCountDependency = GivenClientDependency.protocolByteCountDependency(),
        debugLoggingDependency: VPNManagerDebugLoggingDependency = GivenClientDependency.debugLoggingDependency(),
    ): ICache =
        Cache(
            protocolByteCountDependency = protocolLoggingDependency,
            debugLoggingDependency = debugLoggingDependency
        )

    fun permissions(
        context: Context,
        coroutineContext: ICoroutineContext = GivenExternal.coroutineContext(),
        permissionsDependency: VPNManagerPermissionsDependency = GivenClientDependency.permissionsDependency(),
    ): IPermissions =
        Permissions(
            context = context,
            coroutineContext = coroutineContext,
            permissionsDependency = permissionsDependency
        )

    fun targetProvider(
        targetProviderAPI: TargetProviderAPI =
            TargetProviderBuilder()
                .setClientCoroutineContext(Dispatchers.Main)
                .build(),
    ): ITargetProvider =
        TargetProvider(targetProviderApi = targetProviderAPI)

    fun serviceManager(
        cache: ICache = GivenExternal.cache(),
        serviceManagerApi: VPNServiceManagerAPI = VPNServiceManagerApiMock(),
    ): IServiceManager =
        ServiceManager(
            cache = cache,
            serviceManagerApi = serviceManagerApi
        )

    fun connectionEventAnnouncer(
        cache: ICache = GivenExternal.cache(),
    ): IConnectionEventAnnouncer =
        ConnectionEventAnnouncer(cache = cache)

    fun coroutineContext(): ICoroutineContext =
        object : ICoroutineContext {
            override fun getClientCoroutineContext(): Result<CoroutineContext> =
                Result.success(Dispatchers.Main)

            override fun getModuleCoroutineContext(): Result<CoroutineContext> =
                Result.success(Dispatchers.Main)
        }
}
