package com.kape.vpnservicemanager.domain.controllers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnservicemanager.domain.usecases.IClearCache
import com.kape.vpnservicemanager.domain.usecases.IIsServicePresent
import com.kape.vpnservicemanager.domain.usecases.IStopConnection
import com.kape.vpnservicemanager.testutils.GivenController
import com.kape.vpnservicemanager.testutils.mocks.ClearCacheMock
import com.kape.vpnservicemanager.testutils.mocks.IsServicePresentMock
import com.kape.vpnservicemanager.testutils.mocks.StopConnectionMock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
internal class StopConnectionControllerTest {

    @Test
    fun `avoid clearing cache if stopping fails`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val clearCacheMock: IClearCache = ClearCacheMock()
        val isServicePresentMock: IIsServicePresent = IsServicePresentMock(shouldSucceed = true)
        val stopConnectionMock: IStopConnection = StopConnectionMock(shouldSucceed = false)
        val stopConnectionController: com.kape.vpnservicemanager.domain.controllers.IStopConnectionController = GivenController.stopConnectionController(
            context = context,
            isServicePresent = isServicePresentMock,
            stopConnection = stopConnectionMock,
            clearCache = clearCacheMock
        )

        // when
        val result = stopConnectionController(DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isFailure)
        assert((clearCacheMock as ClearCacheMock).invocationsCounter == 0)
    }

    @Test
    fun `clear cache after stopping successfully`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val clearCacheMock: IClearCache = ClearCacheMock()
        val isServicePresentMock: IIsServicePresent = IsServicePresentMock(shouldSucceed = true)
        val stopConnectionMock: IStopConnection = StopConnectionMock(shouldSucceed = true)
        val stopConnectionController: com.kape.vpnservicemanager.domain.controllers.IStopConnectionController = GivenController.stopConnectionController(
            context = context,
            isServicePresent = isServicePresentMock,
            stopConnection = stopConnectionMock,
            clearCache = clearCacheMock
        )

        // when
        val result = stopConnectionController(DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isSuccess)
        assert((clearCacheMock as ClearCacheMock).invocationsCounter == 1)
    }
}
