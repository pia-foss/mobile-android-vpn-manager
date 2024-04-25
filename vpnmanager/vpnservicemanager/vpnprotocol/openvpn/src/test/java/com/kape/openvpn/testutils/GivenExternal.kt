package com.kape.openvpn.testutils

import android.content.Context
import com.kape.openvpn.data.externals.Cache
import com.kape.openvpn.data.externals.FilePath
import com.kape.openvpn.data.externals.ICache
import com.kape.openvpn.data.externals.IFilePath
import com.kape.openvpn.data.externals.IOpenVpnProcess
import com.kape.openvpn.data.externals.IOpenVpnProcessBuilder
import com.kape.openvpn.data.externals.IOpenVpnProcessSocket
import com.kape.openvpn.data.externals.OpenVpnProcess
import com.kape.openvpn.data.externals.OpenVpnProcessBuilder
import com.kape.openvpn.data.externals.OpenVpnProcessSocket
import com.kape.vpnmanager.api.data.externals.CoroutineContext
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import com.kape.vpnmanager.api.data.externals.IJob
import com.kape.vpnmanager.api.data.externals.Job
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
        CoroutineContext(
            clientCoroutineContext = clientCoroutineContext
        )

    fun cache(): ICache =
        Cache()

    fun filePath(
        context: Context,
    ): IFilePath =
        FilePath(context = context)

    fun openVpnProcessBuilder(): IOpenVpnProcessBuilder =
        OpenVpnProcessBuilder()

    fun openVpnProcess(
        context: Context,
        filePath: IFilePath = GivenExternal.filePath(context = context),
        openVpnProcessBuilder: IOpenVpnProcessBuilder = GivenExternal.openVpnProcessBuilder(),
    ): IOpenVpnProcess =
        OpenVpnProcess(filePath = filePath, openVpnProcessBuilder = openVpnProcessBuilder)

    fun openVpnProcessSocket(
        coroutineContext: ICoroutineContext = GivenExternal.coroutineContext(),
    ): IOpenVpnProcessSocket =
        OpenVpnProcessSocket(
            coroutineContext = coroutineContext
        )

    fun job(
        coroutineContext: ICoroutineContext = GivenExternal.coroutineContext(),
    ): IJob = Job(
        coroutineContext = coroutineContext
    )
}
