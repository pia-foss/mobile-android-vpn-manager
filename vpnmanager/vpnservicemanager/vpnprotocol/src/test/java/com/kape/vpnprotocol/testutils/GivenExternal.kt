package com.kape.vpnprotocol.testutils

import android.content.Context
import com.kape.openvpn.presenters.OpenVpnAPI
import com.kape.vpnprotocol.data.externals.common.Cache
import com.kape.vpnprotocol.data.externals.common.Connectivity
import com.kape.vpnprotocol.data.externals.common.File
import com.kape.vpnprotocol.data.externals.common.IConnectivity
import com.kape.vpnprotocol.data.externals.common.ICoroutineContext
import com.kape.vpnprotocol.data.externals.common.IFile
import com.kape.vpnprotocol.data.externals.common.IJob
import com.kape.vpnprotocol.data.externals.common.ILogsProcessor
import com.kape.vpnprotocol.data.externals.common.INetworkClient
import com.kape.vpnprotocol.data.externals.common.IProcess
import com.kape.vpnprotocol.data.externals.common.ISerializer
import com.kape.vpnprotocol.data.externals.common.Job
import com.kape.vpnprotocol.data.externals.common.LogsProcessor
import com.kape.vpnprotocol.data.externals.common.NetworkClient
import com.kape.vpnprotocol.data.externals.common.Process
import com.kape.vpnprotocol.data.externals.common.Serializer
import com.kape.vpnprotocol.data.externals.openvpn.IOpenVpn
import com.kape.vpnprotocol.data.externals.openvpn.OpenVpn
import com.kape.vpnprotocol.data.externals.wireguard.IWireguard
import com.kape.vpnprotocol.data.externals.wireguard.IWireguardKeyPair
import com.kape.vpnprotocol.data.externals.wireguard.Wireguard
import com.kape.vpnprotocol.data.externals.wireguard.WireguardKeyPair
import com.kape.vpnprotocol.presenters.VPNProtocolByteCountDependency
import com.kape.vpnprotocol.presenters.VPNProtocolConnectivityStatusChangeCallback
import com.kape.vpnprotocol.testutils.mocks.OpenVpnApiMock
import com.kape.vpnprotocol.testutils.mocks.VPNProtocolByteCountDependencyMock
import com.kape.vpnprotocol.testutils.mocks.VPNProtocolConnectivityStatusChangeCallbackMock
import com.kape.vpnprotocol.testutils.mocks.WireguardApiMock
import com.kape.wireguard.presenters.WireguardAPI
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
        context: Context,
        coroutineContext: ICoroutineContext = GivenExternal.coroutineContext(),
        protocolByteCountDependency: VPNProtocolByteCountDependency =
            VPNProtocolByteCountDependencyMock(),
        connectivityStatusChangeCallback: VPNProtocolConnectivityStatusChangeCallback =
            VPNProtocolConnectivityStatusChangeCallbackMock(),
    ) = Cache(
        context = context,
        coroutineContext = coroutineContext,
        protocolByteCountDependency = protocolByteCountDependency,
        connectivityStatusChangeCallback = connectivityStatusChangeCallback
    )

    fun file(
        context: Context,
    ): IFile =
        File(
            context = context
        )

    fun serializer(): ISerializer =
        Serializer()

    fun networkClient(): INetworkClient =
        NetworkClient()

    fun connectivity(): IConnectivity =
        Connectivity()

    fun openVpn(
        openVpnApi: OpenVpnAPI = OpenVpnApiMock(),
    ): IOpenVpn =
        OpenVpn(
            openVpnApi = openVpnApi
        )

    fun wireguard(
        wireguardApi: WireguardAPI = WireguardApiMock(),
    ): IWireguard =
        Wireguard(
            wireguardApi = wireguardApi
        )

    fun wireguardKeyPair(): IWireguardKeyPair =
        WireguardKeyPair()

    fun process(): IProcess =
        Process()

    fun logsProcessor(): ILogsProcessor =
        LogsProcessor()

    fun coroutineContext(): ICoroutineContext =
        object : ICoroutineContext {
            override fun getClientCoroutineContext(): Result<CoroutineContext> =
                Result.success(Dispatchers.Main)

            override fun getModuleCoroutineContext(): Result<CoroutineContext> =
                Result.success(Dispatchers.Main)
        }

    fun job(
        coroutineContext: ICoroutineContext = GivenExternal.coroutineContext(),
    ): IJob =
        Job(
            coroutineContext = coroutineContext
        )
}
