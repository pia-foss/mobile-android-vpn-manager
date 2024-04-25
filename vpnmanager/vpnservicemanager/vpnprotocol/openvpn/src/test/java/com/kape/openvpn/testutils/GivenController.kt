package com.kape.openvpn.testutils

import android.content.Context
import com.kape.openvpn.domain.controllers.IStartProcessController
import com.kape.openvpn.domain.controllers.IStopProcessController
import com.kape.openvpn.domain.controllers.StartProcessController
import com.kape.openvpn.domain.controllers.StopProcessController
import com.kape.openvpn.domain.usecases.ICancelHoldReleaseJob
import com.kape.openvpn.domain.usecases.IClearCache
import com.kape.openvpn.domain.usecases.ICloseSocket
import com.kape.openvpn.domain.usecases.IIsProcessRunning
import com.kape.openvpn.domain.usecases.IIsProcessStopped
import com.kape.openvpn.domain.usecases.IStartOpenVpnOutputHandler
import com.kape.openvpn.domain.usecases.IStartProcess
import com.kape.openvpn.domain.usecases.IStartProcessOutputReader
import com.kape.openvpn.domain.usecases.IStopProcess

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

internal object GivenController {

    fun startProcessController(
        context: Context,
        isProcessStopped: IIsProcessStopped = GivenUsecase.isProcessStopped(),
        startOpenVpnOutputHandler: IStartOpenVpnOutputHandler = GivenUsecase.startOpenVpnOutputHandler(),
        startProcess: IStartProcess = GivenUsecase.startProcess(context = context),
        startProcessOutputReader: IStartProcessOutputReader = GivenUsecase.startProcessOutputReader(),
        clearCache: IClearCache = GivenUsecase.clearCache(),
    ): IStartProcessController = StartProcessController(
        isProcessStopped = isProcessStopped,
        startOpenVpnOutputHandler = startOpenVpnOutputHandler,
        startProcess = startProcess,
        startProcessOutputReader = startProcessOutputReader,
        clearCache = clearCache
    )

    fun stopProcessController(
        context: Context,
        cancelHoldReleaseJob: ICancelHoldReleaseJob = GivenUsecase.cancelHoldReleaseJob(),
        isProcessRunning: IIsProcessRunning = GivenUsecase.isProcessRunning(),
        closeSocket: ICloseSocket = GivenUsecase.closeSocket(),
        stopProcess: IStopProcess = GivenUsecase.stopProcess(context = context),
        clearCache: IClearCache = GivenUsecase.clearCache(),
    ): IStopProcessController =
        StopProcessController(
            cancelHoldReleaseJob = cancelHoldReleaseJob,
            isProcessRunning = isProcessRunning,
            closeSocket = closeSocket,
            stopProcess = stopProcess,
            clearCache = clearCache
        )
}
