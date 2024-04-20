package com.kape.vpnservicemanager.testutils

import android.content.Context
import com.kape.vpnprotocol.presenters.VPNProtocolBuilder
import com.kape.vpnprotocol.presenters.VPNProtocolConnectivityStatusChangeCallback
import com.kape.vpnservicemanager.data.externals.Cache
import com.kape.vpnservicemanager.data.externals.ConnectionEventCallback
import com.kape.vpnservicemanager.data.externals.Connectivity
import com.kape.vpnservicemanager.data.externals.ICache
import com.kape.vpnservicemanager.data.externals.IConnectionEventCallback
import com.kape.vpnservicemanager.data.externals.IConnectivity
import com.kape.vpnservicemanager.data.externals.ICoroutineContext
import com.kape.vpnservicemanager.data.externals.IProtocol
import com.kape.vpnservicemanager.data.externals.IService
import com.kape.vpnservicemanager.data.externals.IServiceConnection
import com.kape.vpnservicemanager.data.externals.ISubnet
import com.kape.vpnservicemanager.data.externals.Protocol
import com.kape.vpnservicemanager.data.externals.Service
import com.kape.vpnservicemanager.data.externals.ServiceConnection
import com.kape.vpnservicemanager.data.externals.Subnet
import com.kape.vpnservicemanager.presenters.VPNServiceConnectivityStatusChangeCallback
import com.kape.vpnservicemanager.testutils.mocks.VPNProtocolConnectivityStatusChangeCallbackMock
import com.kape.vpnservicemanager.testutils.mocks.VPNServiceConnectivityStatusChangeCallbackMock
import kotlinx.coroutines.Dispatchers

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

    fun coroutineContext(
        clientCoroutineContext: kotlin.coroutines.CoroutineContext = Dispatchers.Main,
    ): ICoroutineContext =
        com.kape.vpnservicemanager.data.externals.CoroutineContext(
            clientCoroutineContext = clientCoroutineContext
        )

    fun serviceConnection(
        context: Context,
        coroutineContext: ICoroutineContext = GivenExternal.coroutineContext(),
        protocol: IProtocol = protocol(context = context),
        subnet: ISubnet = subnet(),
        cache: ICache = cache(),
    ): IServiceConnection =
        ServiceConnection(
            cache = cache,
            subnet = subnet,
            protocol = protocol,
            coroutineContext = coroutineContext
        )

    fun protocol(
        context: Context,
        connectivityStatusChangeCallback: VPNProtocolConnectivityStatusChangeCallback =
            VPNProtocolConnectivityStatusChangeCallbackMock(),
    ): IProtocol =
        Protocol(
            cache = cache(),
            vpnProtocolApi = VPNProtocolBuilder()
                .setContext(context = context)
                .setConnectivityStatusChangeCallback(
                    connectivityStatusChangeCallback = connectivityStatusChangeCallback
                )
                .build()
        )

    fun connectionEventCallback(
        connectivityStatusChangeCallback: VPNServiceConnectivityStatusChangeCallback =
            VPNServiceConnectivityStatusChangeCallbackMock(),
    ): IConnectionEventCallback =
        ConnectionEventCallback(connectivityStatusChangeCallback = connectivityStatusChangeCallback)

    fun cache(): ICache =
        Cache()

    fun service(): IService =
        Service()

    fun connectivity(): IConnectivity =
        Connectivity()

    fun subnet(): ISubnet =
        Subnet()
}
