package com.kape.openvpn.testutils

import android.content.Context
import com.kape.openvpn.data.externals.ICache
import com.kape.openvpn.data.externals.IOpenVpnProcess
import com.kape.openvpn.data.externals.IOpenVpnProcessSocket
import com.kape.openvpn.domain.usecases.CancelHoldReleaseJob
import com.kape.openvpn.domain.usecases.ClearCache
import com.kape.openvpn.domain.usecases.CloseSocket
import com.kape.openvpn.domain.usecases.HandleOpenVpnByteCountOutput
import com.kape.openvpn.domain.usecases.HandleOpenVpnHoldOutput
import com.kape.openvpn.domain.usecases.HandleOpenVpnManagementOutput
import com.kape.openvpn.domain.usecases.HandleOpenVpnNeedOkOutput
import com.kape.openvpn.domain.usecases.HandleOpenVpnPasswordOutput
import com.kape.openvpn.domain.usecases.HandleOpenVpnPushOutput
import com.kape.openvpn.domain.usecases.HandleOpenVpnStateOutput
import com.kape.openvpn.domain.usecases.ICancelHoldReleaseJob
import com.kape.openvpn.domain.usecases.IClearCache
import com.kape.openvpn.domain.usecases.ICloseSocket
import com.kape.openvpn.domain.usecases.IHandleOpenVpnByteCountOutput
import com.kape.openvpn.domain.usecases.IHandleOpenVpnHoldOutput
import com.kape.openvpn.domain.usecases.IHandleOpenVpnManagementOutput
import com.kape.openvpn.domain.usecases.IHandleOpenVpnNeedOkOutput
import com.kape.openvpn.domain.usecases.IHandleOpenVpnPasswordOutput
import com.kape.openvpn.domain.usecases.IHandleOpenVpnPushOutput
import com.kape.openvpn.domain.usecases.IHandleOpenVpnStateOutput
import com.kape.openvpn.domain.usecases.IIsProcessRunning
import com.kape.openvpn.domain.usecases.IIsProcessStopped
import com.kape.openvpn.domain.usecases.IStartOpenVpnOutputHandler
import com.kape.openvpn.domain.usecases.IStartProcess
import com.kape.openvpn.domain.usecases.IStartProcessOutputReader
import com.kape.openvpn.domain.usecases.IStopProcess
import com.kape.openvpn.domain.usecases.IsProcessRunning
import com.kape.openvpn.domain.usecases.IsProcessStopped
import com.kape.openvpn.domain.usecases.StartOpenVpnOutputHandler
import com.kape.openvpn.domain.usecases.StartProcess
import com.kape.openvpn.domain.usecases.StartProcessOutputReader
import com.kape.openvpn.domain.usecases.StopProcess
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import com.kape.vpnmanager.api.data.externals.IJob
import io.mockk.mockk

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

    fun handleOpenVpnHoldOutput(): IHandleOpenVpnHoldOutput =
        HandleOpenVpnHoldOutput(
            openVpnProcessSocket = GivenExternal.openVpnProcessSocket(),
            cache = GivenExternal.cache(),
            job = GivenExternal.job()
        )

    fun handleOpenVpnManagementOutput(): IHandleOpenVpnManagementOutput =
        HandleOpenVpnManagementOutput(
            openVpnProcessSocket = GivenExternal.openVpnProcessSocket()
        )

    fun handleOpenVpnNeedOkOutput(): IHandleOpenVpnNeedOkOutput =
        HandleOpenVpnNeedOkOutput(
            openVpnProcessSocket = GivenExternal.openVpnProcessSocket()
        )

    fun handleOpenVpnPasswordOutput(): IHandleOpenVpnPasswordOutput =
        HandleOpenVpnPasswordOutput(
            openVpnProcessSocket = GivenExternal.openVpnProcessSocket()
        )

    fun handleOpenVpnPushOutput(): IHandleOpenVpnPushOutput =
        HandleOpenVpnPushOutput()

    fun handleOpenVpnStateOutput(): IHandleOpenVpnStateOutput =
        HandleOpenVpnStateOutput()

    fun handleOpenVpnByteCountOutput(): IHandleOpenVpnByteCountOutput =
        HandleOpenVpnByteCountOutput()

    fun startOpenVpnOutputHandler(): IStartOpenVpnOutputHandler =
        StartOpenVpnOutputHandler(
            cache = GivenExternal.cache(),
            handleOpenVpnManagementOutput = handleOpenVpnManagementOutput(),
            handleOpenVpnPasswordOutput = handleOpenVpnPasswordOutput(),
            handleOpenVpnNeedOkOutput = handleOpenVpnNeedOkOutput(),
            handleOpenVpnHoldOutput = handleOpenVpnHoldOutput(),
            handleOpenVpnPushOutput = handleOpenVpnPushOutput(),
            handleOpenVpnStateOutput = handleOpenVpnStateOutput(),
            handleOpenVpnByteCountOutput = handleOpenVpnByteCountOutput(),
            handleOpenVpnMtuTestResultOutput = mockk(relaxed = true)
        )

    fun startProcess(
        context: Context,
        cache: ICache = GivenExternal.cache(),
        process: IOpenVpnProcess = GivenExternal.openVpnProcess(context = context),
    ): IStartProcess =
        StartProcess(
            cache = cache,
            process = process
        )

    fun stopProcess(
        context: Context,
        cache: ICache = GivenExternal.cache(),
        process: IOpenVpnProcess = GivenExternal.openVpnProcess(context = context),
    ): IStopProcess =
        StopProcess(
            cache = cache,
            process = process
        )

    fun clearCache(
        cache: ICache = GivenExternal.cache(),
    ): IClearCache =
        ClearCache(
            cache = cache
        )

    fun closeSocket(
        openVpnProcessSocket: IOpenVpnProcessSocket = GivenExternal.openVpnProcessSocket(),
    ): ICloseSocket =
        CloseSocket(
            openVpnProcessSocket = openVpnProcessSocket
        )

    fun isProcessRunning(
        cache: ICache = GivenExternal.cache(),
    ): IIsProcessRunning =
        IsProcessRunning(
            cache = cache
        )

    fun isProcessStopped(
        cache: ICache = GivenExternal.cache(),
    ): IIsProcessStopped =
        IsProcessStopped(
            cache = cache
        )

    fun startProcessOutputReader(
        cache: ICache = GivenExternal.cache(),
        coroutineContext: ICoroutineContext = GivenExternal.coroutineContext(),
    ): IStartProcessOutputReader =
        StartProcessOutputReader(
            cache = cache,
            coroutineContext = coroutineContext
        )

    fun cancelHoldReleaseJob(
        job: IJob = GivenExternal.job(),
    ): ICancelHoldReleaseJob =
        CancelHoldReleaseJob(
            job = job
        )
}
