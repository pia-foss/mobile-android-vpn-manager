package com.kape.openvpn.presenters

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
import com.kape.openvpn.domain.controllers.IStartProcessController
import com.kape.openvpn.domain.controllers.IStopProcessController
import com.kape.openvpn.domain.controllers.StopProcessController
import com.kape.openvpn.domain.usecases.CancelHoldReleaseJob
import com.kape.openvpn.domain.usecases.ClearCache
import com.kape.openvpn.domain.usecases.CloseSocket
import com.kape.openvpn.domain.usecases.HandleOpenVpnByteCountOutput
import com.kape.openvpn.domain.usecases.HandleOpenVpnHoldOutput
import com.kape.openvpn.domain.usecases.HandleOpenVpnManagementOutput
import com.kape.openvpn.domain.usecases.HandleOpenVpnMtuTestResultOutput
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
import com.kape.openvpn.domain.usecases.IHandleOpenVpnMtuTestResultOutput
import com.kape.openvpn.domain.usecases.IHandleOpenVpnNeedOkOutput
import com.kape.openvpn.domain.usecases.IHandleOpenVpnPasswordOutput
import com.kape.openvpn.domain.usecases.IHandleOpenVpnPushOutput
import com.kape.openvpn.domain.usecases.IHandleOpenVpnStateOutput
import com.kape.openvpn.domain.usecases.IIsProcessRunning
import com.kape.openvpn.domain.usecases.IIsProcessStopped
import com.kape.openvpn.domain.usecases.IOpenVpnMtuTestResultAnnouncer
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
import com.kape.vpnmanager.api.data.externals.CoroutineContext
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import com.kape.vpnmanager.api.data.externals.IJob
import com.kape.vpnmanager.api.data.externals.Job

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
 * the `OpenVpnAPI` interface.
 */
public class OpenVpnBuilder {
    private var context: Context? = null
    private var clientCoroutineContext: kotlin.coroutines.CoroutineContext? = null
    private var openVpnMtuTestResultAnnouncer: IOpenVpnMtuTestResultAnnouncer? = null

    /**
     * It sets the context to be used within the module. Whether for checking permissions or
     * for persistence purposes.
     *
     * @param context `Context`.
     */
    fun setContext(context: Context): OpenVpnBuilder = apply {
        this.context = context
    }

    /**
     * Sets the coroutine context to use when invoking the API callbacks.
     *
     * @param clientCoroutineContext `CoroutineContext`.
     */
    fun setClientCoroutineContext(
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
    ): OpenVpnBuilder = apply {
        this.clientCoroutineContext = clientCoroutineContext
    }

    /**
     * Sets the MTU test result announcer which will be invoked whenever there's an MTU test result available
     *
     * @param openVpnMtuTestResultAnnouncer `IOpenVpnMtuTestResultAnnouncer`.
     */
    fun setOpenVpnMtuTestResultAnnouncer(
        openVpnMtuTestResultAnnouncer: IOpenVpnMtuTestResultAnnouncer,
    ): OpenVpnBuilder = apply {
        this.openVpnMtuTestResultAnnouncer = openVpnMtuTestResultAnnouncer
    }

    /**
     * @return `OpenVpnAPI`.
     */
    fun build(): OpenVpnAPI {
        val context = this.context
            ?: throw Exception("Context dependency missing.")
        val clientCoroutineContext = this.clientCoroutineContext
            ?: throw Exception("Client Coroutine Context missing.")
        val openVpnMtuTestResultAnnouncer = this.openVpnMtuTestResultAnnouncer
            ?: throw Exception("OpenVpnMtuTestResultAnnouncer missing.")

        return initializeModule(
            context = context,
            clientCoroutineContext = clientCoroutineContext,
            openVpnMtuTestResultAnnouncer = openVpnMtuTestResultAnnouncer
        )
    }

    // region private
    private fun initializeModule(
        context: Context,
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
        openVpnMtuTestResultAnnouncer: IOpenVpnMtuTestResultAnnouncer,
    ): OpenVpnAPI {
        return initializeExternals(
            context = context,
            clientCoroutineContext = clientCoroutineContext,
            openVpnMtuTestResultAnnouncer = openVpnMtuTestResultAnnouncer
        )
    }

    private fun initializeExternals(
        context: Context,
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
        openVpnMtuTestResultAnnouncer: IOpenVpnMtuTestResultAnnouncer,
    ): OpenVpnAPI {
        val cache: ICache = Cache()
        val openVpnProcessBuilder: IOpenVpnProcessBuilder = OpenVpnProcessBuilder()
        val coroutineContext: ICoroutineContext = CoroutineContext(
            clientCoroutineContext = clientCoroutineContext
        )
        val job: IJob = Job(
            coroutineContext = coroutineContext
        )
        val openVpnProcessSocket: IOpenVpnProcessSocket = OpenVpnProcessSocket(
            coroutineContext = coroutineContext
        )
        val filePath: IFilePath = FilePath(
            context = context
        )
        val openVpnProcess: IOpenVpnProcess = OpenVpnProcess(
            filePath = filePath,
            openVpnProcessBuilder = openVpnProcessBuilder
        )
        return initializeUseCases(
            cache = cache,
            job = job,
            openVpnProcess = openVpnProcess,
            openVpnProcessSocket = openVpnProcessSocket,
            openVpnMtuTestResultAnnouncer = openVpnMtuTestResultAnnouncer,
            coroutineContext = coroutineContext
        )
    }

    private fun initializeUseCases(
        cache: ICache,
        job: IJob,
        openVpnProcess: IOpenVpnProcess,
        openVpnProcessSocket: IOpenVpnProcessSocket,
        openVpnMtuTestResultAnnouncer: IOpenVpnMtuTestResultAnnouncer,
        coroutineContext: ICoroutineContext,
    ): OpenVpnAPI {
        val handleOpenVpnManagementOutput: IHandleOpenVpnManagementOutput =
            HandleOpenVpnManagementOutput(
                openVpnProcessSocket = openVpnProcessSocket
            )
        val handleOpenVpnPasswordOutput: IHandleOpenVpnPasswordOutput =
            HandleOpenVpnPasswordOutput(
                openVpnProcessSocket = openVpnProcessSocket
            )
        val handleOpenVpnNeedOkOutput: IHandleOpenVpnNeedOkOutput =
            HandleOpenVpnNeedOkOutput(
                openVpnProcessSocket = openVpnProcessSocket
            )
        val handleOpenVpnHoldOutput: IHandleOpenVpnHoldOutput =
            HandleOpenVpnHoldOutput(
                openVpnProcessSocket = openVpnProcessSocket,
                cache = cache,
                job = job
            )
        val handleOpenVpnPushOutput: IHandleOpenVpnPushOutput = HandleOpenVpnPushOutput()
        val handleOpenVpnStateOutput: IHandleOpenVpnStateOutput = HandleOpenVpnStateOutput()
        val handleOpenVpnByteCountOutput: IHandleOpenVpnByteCountOutput = HandleOpenVpnByteCountOutput()
        val handleOpenVpnMtuTestResultOutput: IHandleOpenVpnMtuTestResultOutput =
            HandleOpenVpnMtuTestResultOutput(
                openVpnMtuTestResultAnnouncer = openVpnMtuTestResultAnnouncer
            )
        val startOpenVpnOutputHandler: IStartOpenVpnOutputHandler = StartOpenVpnOutputHandler(
            cache = cache,
            handleOpenVpnManagementOutput = handleOpenVpnManagementOutput,
            handleOpenVpnPasswordOutput = handleOpenVpnPasswordOutput,
            handleOpenVpnNeedOkOutput = handleOpenVpnNeedOkOutput,
            handleOpenVpnHoldOutput = handleOpenVpnHoldOutput,
            handleOpenVpnPushOutput = handleOpenVpnPushOutput,
            handleOpenVpnStateOutput = handleOpenVpnStateOutput,
            handleOpenVpnByteCountOutput = handleOpenVpnByteCountOutput,
            handleOpenVpnMtuTestResultOutput = handleOpenVpnMtuTestResultOutput
        )
        val stopProcess: IStopProcess = StopProcess(
            cache = cache,
            process = openVpnProcess
        )
        val startProcess: IStartProcess = StartProcess(
            cache = cache,
            process = openVpnProcess
        )
        val startProcessOutputReader: IStartProcessOutputReader = StartProcessOutputReader(
            cache = cache,
            coroutineContext = coroutineContext
        )
        val isProcessRunning: IIsProcessRunning = IsProcessRunning(
            cache = cache
        )
        val isProcessStopped: IIsProcessStopped = IsProcessStopped(
            cache = cache
        )
        val cancelHoldReleaseJob: ICancelHoldReleaseJob = CancelHoldReleaseJob(
            job = job
        )
        val clearCache: IClearCache = ClearCache(
            cache = cache
        )
        val closeSocket: ICloseSocket = CloseSocket(
            openVpnProcessSocket = openVpnProcessSocket
        )
        return initializeControllers(
            isProcessStopped = isProcessStopped,
            isProcessRunning = isProcessRunning,
            cancelHoldReleaseJob = cancelHoldReleaseJob,
            startOpenVpnOutputHandler = startOpenVpnOutputHandler,
            startProcess = startProcess,
            stopProcess = stopProcess,
            startProcessOutputReader = startProcessOutputReader,
            clearCache = clearCache,
            closeSocket = closeSocket,
            coroutineContext = coroutineContext
        )
    }

    private fun initializeControllers(
        isProcessStopped: IIsProcessStopped,
        isProcessRunning: IIsProcessRunning,
        cancelHoldReleaseJob: ICancelHoldReleaseJob,
        startOpenVpnOutputHandler: IStartOpenVpnOutputHandler,
        startProcess: IStartProcess,
        stopProcess: IStopProcess,
        startProcessOutputReader: IStartProcessOutputReader,
        clearCache: IClearCache,
        closeSocket: ICloseSocket,
        coroutineContext: ICoroutineContext,
    ): OpenVpnAPI {
        val startProcessController: IStartProcessController =
            com.kape.openvpn.domain.controllers.StartProcessController(
                isProcessStopped = isProcessStopped,
                startOpenVpnOutputHandler = startOpenVpnOutputHandler,
                startProcess = startProcess,
                startProcessOutputReader = startProcessOutputReader,
                clearCache = clearCache
            )
        val stopProcessController: IStopProcessController = StopProcessController(
            cancelHoldReleaseJob = cancelHoldReleaseJob,
            isProcessRunning = isProcessRunning,
            closeSocket = closeSocket,
            stopProcess = stopProcess,
            clearCache = clearCache
        )
        return OpenVpn(
            startProcessController = startProcessController,
            stopProcessController = stopProcessController,
            coroutineContext = coroutineContext
        )
    }
    // endregion
}
