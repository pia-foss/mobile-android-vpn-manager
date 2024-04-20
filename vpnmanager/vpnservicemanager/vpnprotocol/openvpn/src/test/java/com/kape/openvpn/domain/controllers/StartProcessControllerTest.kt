/*
 *
 *  *  Copyright (c) "2023" Private Internet Access, Inc.
 *  *
 *  *  This file is part of the Private Internet Access Android Client.
 *  *
 *  *  The Private Internet Access Android Client is free software: you can redistribute it and/or
 *  *  modify it under the terms of the GNU General Public License as published by the Free
 *  *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  *  The Private Internet Access Android Client is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  *  details.
 *  *
 *  *  You should have received a copy of the GNU General Public License along with the Private
 *  *  Internet Access Android Client.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.kape.openvpn.domain.controllers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kape.openvpn.domain.usecases.IClearCache
import com.kape.openvpn.domain.usecases.IIsProcessStopped
import com.kape.openvpn.domain.usecases.IStartOpenVpnOutputHandler
import com.kape.openvpn.domain.usecases.IStartProcess
import com.kape.openvpn.domain.usecases.IStartProcessOutputReader
import com.kape.openvpn.presenters.OpenVpnProcessEventHandler
import com.kape.openvpn.testutils.GivenUsecase
import com.kape.openvpn.testutils.mocks.ClearCacheMock
import com.kape.openvpn.testutils.mocks.IsProcessStoppedMock
import com.kape.openvpn.testutils.mocks.OpenVpnProcessEventHandlerMock
import com.kape.openvpn.testutils.mocks.StartOpenVpnOutputHandlerMock
import com.kape.openvpn.testutils.mocks.StartProcessMock
import com.kape.openvpn.testutils.mocks.StartProcessOutputReaderMock
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

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

@RunWith(RobolectricTestRunner::class)
internal class StartProcessControllerTest {

    @Test
    fun `must fail if the process is running already`() = runBlocking {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val processEventHandlerMock: OpenVpnProcessEventHandler = OpenVpnProcessEventHandlerMock()
        val isProcessStoppedMock: IIsProcessStopped = IsProcessStoppedMock(succeed = false)
        val startOpenVpnOutputHandler: IStartOpenVpnOutputHandler = GivenUsecase.startOpenVpnOutputHandler()
        val startProcess: IStartProcess = GivenUsecase.startProcess(context = context)
        val startProcessOutputReader: IStartProcessOutputReader = GivenUsecase.startProcessOutputReader()
        val clearCacheMock: IClearCache = ClearCacheMock(succeed = true)
        val startProcessController: IStartProcessController = StartProcessController(
            isProcessStopped = isProcessStoppedMock,
            startOpenVpnOutputHandler = startOpenVpnOutputHandler,
            startProcess = startProcess,
            startProcessOutputReader = startProcessOutputReader,
            clearCache = clearCacheMock
        )

        // when
        val result = startProcessController(
            commandLineParams = emptyList(),
            openVpnProcessEventHandler = processEventHandlerMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `must not clear cache if fails due to the process running already`() = runBlocking {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val processEventHandlerMock: OpenVpnProcessEventHandler = OpenVpnProcessEventHandlerMock()
        val isProcessStoppedMock: IIsProcessStopped = IsProcessStoppedMock(succeed = false)
        val startOpenVpnOutputHandler: IStartOpenVpnOutputHandler = GivenUsecase.startOpenVpnOutputHandler()
        val startProcess: IStartProcess = GivenUsecase.startProcess(context = context)
        val startProcessOutputReader: IStartProcessOutputReader = GivenUsecase.startProcessOutputReader()
        val clearCacheMock: IClearCache = ClearCacheMock(succeed = true)
        val startProcessController: IStartProcessController = StartProcessController(
            isProcessStopped = isProcessStoppedMock,
            startOpenVpnOutputHandler = startOpenVpnOutputHandler,
            startProcess = startProcess,
            startProcessOutputReader = startProcessOutputReader,
            clearCache = clearCacheMock
        )

        // when
        val result = startProcessController(
            commandLineParams = emptyList(),
            openVpnProcessEventHandler = processEventHandlerMock
        )

        // then
        assert(result.isFailure)
        assert((clearCacheMock as ClearCacheMock).invocationsCounter == 0)
    }

    @Test
    fun `must fail if the output handler fail to start`() = runBlocking {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val processEventHandlerMock: OpenVpnProcessEventHandler = OpenVpnProcessEventHandlerMock()
        val isProcessStoppedMock: IIsProcessStopped = IsProcessStoppedMock(succeed = true)
        val startOpenVpnOutputHandlerMock: IStartOpenVpnOutputHandler = StartOpenVpnOutputHandlerMock(succeed = false)
        val startProcess: IStartProcess = GivenUsecase.startProcess(context = context)
        val startProcessOutputReader: IStartProcessOutputReader = GivenUsecase.startProcessOutputReader()
        val clearCacheMock: IClearCache = ClearCacheMock(succeed = true)
        val startProcessController: IStartProcessController = StartProcessController(
            isProcessStopped = isProcessStoppedMock,
            startOpenVpnOutputHandler = startOpenVpnOutputHandlerMock,
            startProcess = startProcess,
            startProcessOutputReader = startProcessOutputReader,
            clearCache = clearCacheMock
        )

        // when
        val result = startProcessController(
            commandLineParams = emptyList(),
            openVpnProcessEventHandler = processEventHandlerMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `must clear cache if the output handler fail to start`() = runBlocking {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val processEventHandlerMock: OpenVpnProcessEventHandler = OpenVpnProcessEventHandlerMock()
        val isProcessStoppedMock: IIsProcessStopped = IsProcessStoppedMock(succeed = true)
        val startOpenVpnOutputHandlerMock: IStartOpenVpnOutputHandler = StartOpenVpnOutputHandlerMock(succeed = false)
        val startProcess: IStartProcess = GivenUsecase.startProcess(context = context)
        val startProcessOutputReader: IStartProcessOutputReader = GivenUsecase.startProcessOutputReader()
        val clearCacheMock: IClearCache = ClearCacheMock(succeed = true)
        val startProcessController: IStartProcessController = StartProcessController(
            isProcessStopped = isProcessStoppedMock,
            startOpenVpnOutputHandler = startOpenVpnOutputHandlerMock,
            startProcess = startProcess,
            startProcessOutputReader = startProcessOutputReader,
            clearCache = clearCacheMock
        )

        // when
        val result = startProcessController(
            commandLineParams = emptyList(),
            openVpnProcessEventHandler = processEventHandlerMock
        )

        // then
        assert(result.isFailure)
        assert((clearCacheMock as ClearCacheMock).invocationsCounter == 1)
    }

    @Test
    fun `must fail if the process fail to start`() = runBlocking {
        // given
        val processEventHandlerMock: OpenVpnProcessEventHandler = OpenVpnProcessEventHandlerMock()
        val isProcessStoppedMock: IIsProcessStopped = IsProcessStoppedMock(succeed = true)
        val startOpenVpnOutputHandlerMock: IStartOpenVpnOutputHandler = StartOpenVpnOutputHandlerMock(succeed = true)
        val startProcessMock: IStartProcess = StartProcessMock(succeed = false)
        val startProcessOutputReader: IStartProcessOutputReader = GivenUsecase.startProcessOutputReader()
        val clearCacheMock: IClearCache = ClearCacheMock(succeed = true)
        val startProcessController: IStartProcessController = StartProcessController(
            isProcessStopped = isProcessStoppedMock,
            startOpenVpnOutputHandler = startOpenVpnOutputHandlerMock,
            startProcess = startProcessMock,
            startProcessOutputReader = startProcessOutputReader,
            clearCache = clearCacheMock
        )

        // when
        val result = startProcessController(
            commandLineParams = emptyList(),
            openVpnProcessEventHandler = processEventHandlerMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `must clear cache if the process fail to start`() = runBlocking {
        // given
        val processEventHandlerMock: OpenVpnProcessEventHandler = OpenVpnProcessEventHandlerMock()
        val isProcessStoppedMock: IIsProcessStopped = IsProcessStoppedMock(succeed = true)
        val startOpenVpnOutputHandlerMock: IStartOpenVpnOutputHandler = StartOpenVpnOutputHandlerMock(succeed = true)
        val startProcessMock: IStartProcess = StartProcessMock(succeed = false)
        val startProcessOutputReader: IStartProcessOutputReader = GivenUsecase.startProcessOutputReader()
        val clearCacheMock: IClearCache = ClearCacheMock(succeed = true)
        val startProcessController: IStartProcessController = StartProcessController(
            isProcessStopped = isProcessStoppedMock,
            startOpenVpnOutputHandler = startOpenVpnOutputHandlerMock,
            startProcess = startProcessMock,
            startProcessOutputReader = startProcessOutputReader,
            clearCache = clearCacheMock
        )

        // when
        val result = startProcessController(
            commandLineParams = emptyList(),
            openVpnProcessEventHandler = processEventHandlerMock
        )

        // then
        assert(result.isFailure)
        assert((clearCacheMock as ClearCacheMock).invocationsCounter == 1)
    }

    @Test
    fun `must fail if the process's reader fail to start`() = runBlocking {
        // given
        val processEventHandlerMock: OpenVpnProcessEventHandler = OpenVpnProcessEventHandlerMock()
        val isProcessStoppedMock: IIsProcessStopped = IsProcessStoppedMock(succeed = true)
        val startOpenVpnOutputHandlerMock: IStartOpenVpnOutputHandler = StartOpenVpnOutputHandlerMock(succeed = true)
        val startProcessMock: IStartProcess = StartProcessMock(succeed = true)
        val startProcessOutputReaderMock: IStartProcessOutputReader = StartProcessOutputReaderMock(succeed = false)
        val clearCacheMock: IClearCache = ClearCacheMock(succeed = true)
        val startProcessController: IStartProcessController = StartProcessController(
            isProcessStopped = isProcessStoppedMock,
            startOpenVpnOutputHandler = startOpenVpnOutputHandlerMock,
            startProcess = startProcessMock,
            startProcessOutputReader = startProcessOutputReaderMock,
            clearCache = clearCacheMock
        )

        // when
        val result = startProcessController(
            commandLineParams = emptyList(),
            openVpnProcessEventHandler = processEventHandlerMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `must clear cache if the process's reader fail to start`() = runBlocking {
        // given
        val processEventHandlerMock: OpenVpnProcessEventHandler = OpenVpnProcessEventHandlerMock()
        val isProcessStoppedMock: IIsProcessStopped = IsProcessStoppedMock(succeed = true)
        val startOpenVpnOutputHandlerMock: IStartOpenVpnOutputHandler = StartOpenVpnOutputHandlerMock(succeed = true)
        val startProcessMock: IStartProcess = StartProcessMock(succeed = true)
        val startProcessOutputReaderMock: IStartProcessOutputReader = StartProcessOutputReaderMock(succeed = false)
        val clearCacheMock: IClearCache = ClearCacheMock(succeed = true)
        val startProcessController: IStartProcessController = StartProcessController(
            isProcessStopped = isProcessStoppedMock,
            startOpenVpnOutputHandler = startOpenVpnOutputHandlerMock,
            startProcess = startProcessMock,
            startProcessOutputReader = startProcessOutputReaderMock,
            clearCache = clearCacheMock
        )

        // when
        val result = startProcessController(
            commandLineParams = emptyList(),
            openVpnProcessEventHandler = processEventHandlerMock
        )

        // then
        assert(result.isFailure)
        assert((clearCacheMock as ClearCacheMock).invocationsCounter == 1)
    }

    @Test
    fun `must succeed if all use-cases succeed`() = runBlocking {
        // given
        val processEventHandlerMock: OpenVpnProcessEventHandler = OpenVpnProcessEventHandlerMock()
        val isProcessStoppedMock: IIsProcessStopped = IsProcessStoppedMock(succeed = true)
        val startOpenVpnOutputHandlerMock: IStartOpenVpnOutputHandler = StartOpenVpnOutputHandlerMock(succeed = true)
        val startProcessMock: IStartProcess = StartProcessMock(succeed = true)
        val startProcessOutputReaderMock: IStartProcessOutputReader = StartProcessOutputReaderMock(succeed = true)
        val clearCacheMock: IClearCache = ClearCacheMock(succeed = true)
        val startProcessController: IStartProcessController = StartProcessController(
            isProcessStopped = isProcessStoppedMock,
            startOpenVpnOutputHandler = startOpenVpnOutputHandlerMock,
            startProcess = startProcessMock,
            startProcessOutputReader = startProcessOutputReaderMock,
            clearCache = clearCacheMock
        )

        // when
        val result = startProcessController(
            commandLineParams = emptyList(),
            openVpnProcessEventHandler = processEventHandlerMock
        )

        // then
        assert(result.isSuccess)
    }

    @Test
    fun `must not clear cache if all use-cases succeed`() = runBlocking {
        // given
        val processEventHandlerMock: OpenVpnProcessEventHandler = OpenVpnProcessEventHandlerMock()
        val isProcessStoppedMock: IIsProcessStopped = IsProcessStoppedMock(succeed = true)
        val startOpenVpnOutputHandlerMock: IStartOpenVpnOutputHandler = StartOpenVpnOutputHandlerMock(succeed = true)
        val startProcessMock: IStartProcess = StartProcessMock(succeed = true)
        val startProcessOutputReaderMock: IStartProcessOutputReader = StartProcessOutputReaderMock(succeed = true)
        val clearCacheMock: IClearCache = ClearCacheMock(succeed = true)
        val startProcessController: IStartProcessController = StartProcessController(
            isProcessStopped = isProcessStoppedMock,
            startOpenVpnOutputHandler = startOpenVpnOutputHandlerMock,
            startProcess = startProcessMock,
            startProcessOutputReader = startProcessOutputReaderMock,
            clearCache = clearCacheMock
        )

        // when
        val result = startProcessController(
            commandLineParams = emptyList(),
            openVpnProcessEventHandler = processEventHandlerMock
        )

        // then
        assert(result.isSuccess)
        assert((clearCacheMock as ClearCacheMock).invocationsCounter == 0)
    }
}
