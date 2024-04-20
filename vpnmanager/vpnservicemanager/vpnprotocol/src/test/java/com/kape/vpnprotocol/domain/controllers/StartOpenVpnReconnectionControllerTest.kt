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

package com.kape.vpnprotocol.domain.controllers

import com.kape.vpnprotocol.domain.controllers.openvpn.IStartOpenVpnReconnectionController
import com.kape.vpnprotocol.domain.controllers.openvpn.StartOpenVpnReconnectionController
import com.kape.vpnprotocol.domain.usecases.common.IGetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.IIsNetworkAvailable
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.openvpn.ICreateOpenVpnProcessConnectedDeferrable
import com.kape.vpnprotocol.domain.usecases.openvpn.IStartOpenVpnEventHandler
import com.kape.vpnprotocol.domain.usecases.openvpn.IStartOpenVpnProcess
import com.kape.vpnprotocol.domain.usecases.openvpn.IStopOpenVpnProcess
import com.kape.vpnprotocol.domain.usecases.openvpn.IWaitForOpenVpnProcessConnectedDeferrable
import com.kape.vpnprotocol.testutils.mocks.CreateOpenVpnProcessConnectedDeferrableMock
import com.kape.vpnprotocol.testutils.mocks.GetProtocolConfigurationMock
import com.kape.vpnprotocol.testutils.mocks.IsNetworkAvailableMock
import com.kape.vpnprotocol.testutils.mocks.ReportConnectivityStatusMock
import com.kape.vpnprotocol.testutils.mocks.StartOpenVpnEventHandlerMock
import com.kape.vpnprotocol.testutils.mocks.StartOpenVpnProcessMock
import com.kape.vpnprotocol.testutils.mocks.StopOpenVpnProcessMock
import com.kape.vpnprotocol.testutils.mocks.WaitForOpenVpnProcessConnectedDeferrableMock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

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
internal class StartOpenVpnReconnectionControllerTest {

    @Test
    fun `should succeed if all use cases in the controller flow succeed`() = runTest {
        // given
        val startOpenVpnReconnectionController: IStartOpenVpnReconnectionController =
            provideSuccessfulStartOpenVpnReconnectionController()

        // when
        val result = startOpenVpnReconnectionController()

        // then
        assert(result.isSuccess)
    }

    @Test
    fun `should fail if reporting the connectivity status failed`() = runTest {
        // given
        val reportConnectivityStatusMock: IReportConnectivityStatus =
            ReportConnectivityStatusMock(shouldSucceed = false)
        val startOpenVpnReconnectionController: IStartOpenVpnReconnectionController =
            provideSuccessfulStartOpenVpnReconnectionController(
                reportConnectivityStatusMock = reportConnectivityStatusMock
            )

        // when
        val result = startOpenVpnReconnectionController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if getting the protocol configuration failed`() = runTest {
        // given
        val getProtocolConfigurationMock: IGetProtocolConfiguration =
            GetProtocolConfigurationMock(shouldSucceed = false)
        val startOpenVpnReconnectionController: IStartOpenVpnReconnectionController =
            provideSuccessfulStartOpenVpnReconnectionController(
                getProtocolConfigurationMock = getProtocolConfigurationMock
            )

        // when
        val result = startOpenVpnReconnectionController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if there is no network connectivity`() = runTest {
        // given
        val isNetworkAvailableMock: IIsNetworkAvailable =
            IsNetworkAvailableMock(shouldSucceed = false)
        val startOpenVpnReconnectionController: IStartOpenVpnReconnectionController =
            provideSuccessfulStartOpenVpnReconnectionController(
                isNetworkAvailableMock = isNetworkAvailableMock
            )

        // when
        val result = startOpenVpnReconnectionController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if stopping the open vpn process failed`() = runTest {
        // given
        val stopOpenVpnProcessMock: IStopOpenVpnProcess =
            StopOpenVpnProcessMock(shouldSucceed = false)
        val startOpenVpnReconnectionController: IStartOpenVpnReconnectionController =
            provideSuccessfulStartOpenVpnReconnectionController(
                stopOpenVpnProcessMock = stopOpenVpnProcessMock
            )

        // when
        val result = startOpenVpnReconnectionController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if creating the open vpn connection deferrable failed`() = runTest {
        // given
        val createOpenVpnProcessConnectedDeferrableMock: ICreateOpenVpnProcessConnectedDeferrable =
            CreateOpenVpnProcessConnectedDeferrableMock(shouldSucceed = false)
        val startOpenVpnReconnectionController: IStartOpenVpnReconnectionController =
            provideSuccessfulStartOpenVpnReconnectionController(
                createOpenVpnProcessConnectedDeferrableMock = createOpenVpnProcessConnectedDeferrableMock
            )

        // when
        val result = startOpenVpnReconnectionController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if starting the open vpn event handler failed`() = runTest {
        // given
        val startOpenVpnEventHandlerMock: IStartOpenVpnEventHandler =
            StartOpenVpnEventHandlerMock(shouldSucceed = false)
        val startOpenVpnReconnectionController: IStartOpenVpnReconnectionController =
            provideSuccessfulStartOpenVpnReconnectionController(
                startOpenVpnEventHandlerMock = startOpenVpnEventHandlerMock
            )

        // when
        val result = startOpenVpnReconnectionController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if starting the open vpn process failed`() = runTest {
        // given
        val startOpenVpnProcessMock: IStartOpenVpnProcess =
            StartOpenVpnProcessMock(shouldSucceed = false)
        val startOpenVpnReconnectionController: IStartOpenVpnReconnectionController =
            provideSuccessfulStartOpenVpnReconnectionController(
                startOpenVpnProcessMock = startOpenVpnProcessMock
            )

        // when
        val result = startOpenVpnReconnectionController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if waiting for the connection deferrable failed`() = runTest {
        // given
        val waitForOpenVpnProcessConnectedDeferrableMock: IWaitForOpenVpnProcessConnectedDeferrable =
            WaitForOpenVpnProcessConnectedDeferrableMock(shouldSucceed = false)
        val startOpenVpnReconnectionController: IStartOpenVpnReconnectionController =
            provideSuccessfulStartOpenVpnReconnectionController(
                waitForOpenVpnProcessConnectedDeferrableMock = waitForOpenVpnProcessConnectedDeferrableMock
            )

        // when
        val result = startOpenVpnReconnectionController()

        // then
        assert(result.isFailure)
    }

    // region private
    private fun provideSuccessfulStartOpenVpnReconnectionController(
        reportConnectivityStatusMock: IReportConnectivityStatus =
            ReportConnectivityStatusMock(shouldSucceed = true),
        getProtocolConfigurationMock: IGetProtocolConfiguration =
            GetProtocolConfigurationMock(shouldSucceed = true),
        isNetworkAvailableMock: IIsNetworkAvailable =
            IsNetworkAvailableMock(shouldSucceed = true),
        stopOpenVpnProcessMock: IStopOpenVpnProcess =
            StopOpenVpnProcessMock(shouldSucceed = true),
        createOpenVpnProcessConnectedDeferrableMock: ICreateOpenVpnProcessConnectedDeferrable =
            CreateOpenVpnProcessConnectedDeferrableMock(shouldSucceed = true),
        startOpenVpnEventHandlerMock: IStartOpenVpnEventHandler =
            StartOpenVpnEventHandlerMock(shouldSucceed = true),
        startOpenVpnProcessMock: IStartOpenVpnProcess =
            StartOpenVpnProcessMock(shouldSucceed = true),
        waitForOpenVpnProcessConnectedDeferrableMock: IWaitForOpenVpnProcessConnectedDeferrable =
            WaitForOpenVpnProcessConnectedDeferrableMock(shouldSucceed = true),
    ): IStartOpenVpnReconnectionController =
        StartOpenVpnReconnectionController(
            reportConnectivityStatus = reportConnectivityStatusMock,
            getProtocolConfiguration = getProtocolConfigurationMock,
            isNetworkAvailable = isNetworkAvailableMock,
            stopOpenVpnProcess = stopOpenVpnProcessMock,
            createOpenVpnProcessConnectedDeferrable = createOpenVpnProcessConnectedDeferrableMock,
            startOpenVpnEventHandler = startOpenVpnEventHandlerMock,
            startOpenVpnProcess = startOpenVpnProcessMock,
            waitForOpenVpnProcessConnectedDeferrable = waitForOpenVpnProcessConnectedDeferrableMock
        )
    // endregion
}
