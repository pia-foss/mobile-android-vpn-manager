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
import com.kape.openvpn.domain.usecases.ICloseSocket
import com.kape.openvpn.domain.usecases.IIsProcessRunning
import com.kape.openvpn.domain.usecases.IStopProcess
import com.kape.openvpn.testutils.GivenUsecase
import com.kape.openvpn.testutils.mocks.ClearCacheMock
import com.kape.openvpn.testutils.mocks.CloseSocketMock
import com.kape.openvpn.testutils.mocks.IsProcessRunningMock
import com.kape.openvpn.testutils.mocks.StopProcessMock
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
internal class StopProcessControllerTest {

    @Test
    fun `must fail if the process is stopped already`() = runBlocking {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val isProcessRunningMock: IIsProcessRunning = IsProcessRunningMock(succeed = false)
        val closeSocketMock: ICloseSocket = CloseSocketMock(succeed = true)
        val stopProcess: IStopProcess = GivenUsecase.stopProcess(context = context)
        val clearCache: IClearCache = ClearCacheMock(succeed = true)
        val stopProcessController: IStopProcessController = StopProcessController(
            isProcessRunning = isProcessRunningMock,
            closeSocket = closeSocketMock,
            stopProcess = stopProcess,
            clearCache = clearCache
        )

        // when
        val result = stopProcessController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `must fail if the socket failed to close`() = runBlocking {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val isProcessRunningMock: IIsProcessRunning = IsProcessRunningMock(succeed = false)
        val closeSocketMock: ICloseSocket = CloseSocketMock(succeed = false)
        val stopProcess: IStopProcess = GivenUsecase.stopProcess(context = context)
        val clearCache: IClearCache = ClearCacheMock(succeed = true)
        val stopProcessController: IStopProcessController = StopProcessController(
            isProcessRunning = isProcessRunningMock,
            closeSocket = closeSocketMock,
            stopProcess = stopProcess,
            clearCache = clearCache
        )

        // when
        val result = stopProcessController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `must fail if the process failed to stop`() = runBlocking {
        // given
        val isProcessRunningMock: IIsProcessRunning = IsProcessRunningMock(succeed = true)
        val closeSocketMock: ICloseSocket = CloseSocketMock(succeed = true)
        val stopProcessMock: IStopProcess = StopProcessMock(succeed = false)
        val clearCache: IClearCache = ClearCacheMock(succeed = true)
        val stopProcessController: IStopProcessController = StopProcessController(
            isProcessRunning = isProcessRunningMock,
            closeSocket = closeSocketMock,
            stopProcess = stopProcessMock,
            clearCache = clearCache
        )

        // when
        val result = stopProcessController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `must fail if the cache failed to clear`() = runBlocking {
        // given
        val isProcessRunningMock: IIsProcessRunning = IsProcessRunningMock(succeed = true)
        val closeSocketMock: ICloseSocket = CloseSocketMock(succeed = true)
        val stopProcessMock: IStopProcess = StopProcessMock(succeed = true)
        val clearCache: IClearCache = ClearCacheMock(succeed = false)
        val stopProcessController: IStopProcessController = StopProcessController(
            isProcessRunning = isProcessRunningMock,
            closeSocket = closeSocketMock,
            stopProcess = stopProcessMock,
            clearCache = clearCache
        )

        // when
        val result = stopProcessController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `must succeed if all use-cases succeed`() = runBlocking {
        // given
        val isProcessRunningMock: IIsProcessRunning = IsProcessRunningMock(succeed = true)
        val closeSocketMock: ICloseSocket = CloseSocketMock(succeed = true)
        val stopProcessMock: IStopProcess = StopProcessMock(succeed = true)
        val clearCache: IClearCache = ClearCacheMock(succeed = true)
        val stopProcessController: IStopProcessController = StopProcessController(
            isProcessRunning = isProcessRunningMock,
            closeSocket = closeSocketMock,
            stopProcess = stopProcessMock,
            clearCache = clearCache
        )

        // when
        val result = stopProcessController()

        // then
        assert(result.isSuccess)
    }
}
