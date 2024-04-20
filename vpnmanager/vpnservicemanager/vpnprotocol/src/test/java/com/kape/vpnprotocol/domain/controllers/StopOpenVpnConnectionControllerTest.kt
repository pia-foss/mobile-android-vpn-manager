package com.kape.vpnprotocol.domain.controllers

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnprotocol.domain.controllers.openvpn.IStopOpenVpnConnectionController
import com.kape.vpnprotocol.domain.controllers.openvpn.StopOpenVpnConnectionController
import com.kape.vpnprotocol.domain.usecases.common.IClearCache
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.openvpn.IStopOpenVpnProcess
import com.kape.vpnprotocol.testutils.mocks.ClearCacheMock
import com.kape.vpnprotocol.testutils.mocks.ReportConnectivityStatusMock
import com.kape.vpnprotocol.testutils.mocks.StopOpenVpnProcessMock
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
internal class StopOpenVpnConnectionControllerTest {

    @Test
    fun `should succeed if all use cases in the controller flow succeed`() = runTest {
        // given
        val stopOpenVpnConnectionController: IStopOpenVpnConnectionController =
            provideSuccessfulStopOpenVpnConnectionController()

        // when
        val result = stopOpenVpnConnectionController(DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isSuccess)
    }

    @Test
    fun `should fail if reporting the connectivity status failed`() = runTest {
        // given
        val reportConnectivityStatusMock: IReportConnectivityStatus =
            ReportConnectivityStatusMock(shouldSucceed = false)
        val stopOpenVpnConnectionController: IStopOpenVpnConnectionController =
            provideSuccessfulStopOpenVpnConnectionController(
                reportConnectivityStatusMock = reportConnectivityStatusMock
            )

        // when
        val result = stopOpenVpnConnectionController(DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if stopping the open vpn process failed`() = runTest {
        // given
        val stopOpenVpnProcessMock: IStopOpenVpnProcess =
            StopOpenVpnProcessMock(shouldSucceed = false)
        val stopOpenVpnConnectionController: IStopOpenVpnConnectionController =
            provideSuccessfulStopOpenVpnConnectionController(
                stopOpenVpnProcessMock = stopOpenVpnProcessMock
            )

        // when
        val result = stopOpenVpnConnectionController(DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if clearing cache failed`() = runTest {
        // given
        val clearCacheMock: IClearCache =
            ClearCacheMock(shouldSucceed = false)
        val stopOpenVpnConnectionController: IStopOpenVpnConnectionController =
            provideSuccessfulStopOpenVpnConnectionController(
                clearCacheMock = clearCacheMock
            )

        // when
        val result = stopOpenVpnConnectionController(DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isFailure)
    }

    // region private
    private fun provideSuccessfulStopOpenVpnConnectionController(
        reportConnectivityStatusMock: IReportConnectivityStatus =
            ReportConnectivityStatusMock(shouldSucceed = true),
        stopOpenVpnProcessMock: IStopOpenVpnProcess =
            StopOpenVpnProcessMock(shouldSucceed = true),
        clearCacheMock: IClearCache =
            ClearCacheMock(shouldSucceed = true),
    ): IStopOpenVpnConnectionController =
        StopOpenVpnConnectionController(
            reportConnectivityStatus = reportConnectivityStatusMock,
            stopOpenVpnProcess = stopOpenVpnProcessMock,
            clearCache = clearCacheMock
        )
    // endregion
}
