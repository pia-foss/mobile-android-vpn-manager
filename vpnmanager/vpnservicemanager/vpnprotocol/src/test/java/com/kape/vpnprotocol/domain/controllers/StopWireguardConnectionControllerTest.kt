package com.kape.vpnprotocol.domain.controllers

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnprotocol.domain.controllers.wireguard.IStopWireguardConnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.StopWireguardConnectionController
import com.kape.vpnprotocol.domain.usecases.common.IClearCache
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.wireguard.IDestroyWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.IGetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.IStopWireguardByteCountJob
import com.kape.vpnprotocol.testutils.mocks.ClearCacheMock
import com.kape.vpnprotocol.testutils.mocks.DestroyWireguardTunnelMock
import com.kape.vpnprotocol.testutils.mocks.GetWireguardTunnelHandleMock
import com.kape.vpnprotocol.testutils.mocks.ReportConnectivityStatusMock
import com.kape.vpnprotocol.testutils.mocks.StopWireguardByteCountJobMock
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
internal class StopWireguardConnectionControllerTest {

    @Test
    fun `should succeed if all use cases in the controller flow succeed`() = runTest {
        // given
        val stopWireguardConnectionController: IStopWireguardConnectionController =
            provideSuccessfulStopWireguardConnectionController()

        // when
        val result = stopWireguardConnectionController(DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isSuccess)
    }

    @Test
    fun `should fail if reporting the connectivity status failed`() = runTest {
        // given
        val reportConnectivityStatusMock: IReportConnectivityStatus =
            ReportConnectivityStatusMock(shouldSucceed = false)
        val stopWireguardConnectionController: IStopWireguardConnectionController =
            provideSuccessfulStopWireguardConnectionController(
                reportConnectivityStatusMock = reportConnectivityStatusMock
            )

        // when
        val result = stopWireguardConnectionController(DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if getting the wireguard tunnel handle failed`() = runTest {
        // given
        val getWireguardTunnelHandleMock: IGetWireguardTunnelHandle =
            GetWireguardTunnelHandleMock(shouldSucceed = false)
        val stopWireguardConnectionController: IStopWireguardConnectionController =
            provideSuccessfulStopWireguardConnectionController(
                getWireguardTunnelHandleMock = getWireguardTunnelHandleMock
            )

        // when
        val result = stopWireguardConnectionController(DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if destroying the wireguard tunnel failed`() = runTest {
        // given
        val destroyWireguardTunnelMock: IDestroyWireguardTunnel =
            DestroyWireguardTunnelMock(shouldSucceed = false)
        val stopWireguardConnectionController: IStopWireguardConnectionController =
            provideSuccessfulStopWireguardConnectionController(
                destroyWireguardTunnelMock = destroyWireguardTunnelMock
            )

        // when
        val result = stopWireguardConnectionController(DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if clearing cache failed`() = runTest {
        // given
        val clearCacheMock: IClearCache =
            ClearCacheMock(shouldSucceed = false)
        val stopWireguardConnectionController: IStopWireguardConnectionController =
            provideSuccessfulStopWireguardConnectionController(
                clearCacheMock = clearCacheMock
            )

        // when
        val result = stopWireguardConnectionController(DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if stopping byte count job failed`() = runTest {
        // given
        val stopWireguardByteCountJobMock: IStopWireguardByteCountJob =
            StopWireguardByteCountJobMock(shouldSucceed = false)
        val stopWireguardConnectionController: IStopWireguardConnectionController =
            provideSuccessfulStopWireguardConnectionController(
                stopWireguardByteCountJobMock = stopWireguardByteCountJobMock
            )

        // when
        val result = stopWireguardConnectionController(DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isFailure)
    }

    // region private
    private fun provideSuccessfulStopWireguardConnectionController(
        reportConnectivityStatusMock: IReportConnectivityStatus =
            ReportConnectivityStatusMock(shouldSucceed = true),
        getWireguardTunnelHandleMock: IGetWireguardTunnelHandle =
            GetWireguardTunnelHandleMock(shouldSucceed = true),
        stopWireguardByteCountJobMock: IStopWireguardByteCountJob =
            StopWireguardByteCountJobMock(shouldSucceed = true),
        destroyWireguardTunnelMock: IDestroyWireguardTunnel =
            DestroyWireguardTunnelMock(shouldSucceed = true),
        clearCacheMock: IClearCache =
            ClearCacheMock(shouldSucceed = true),
    ): IStopWireguardConnectionController =
        StopWireguardConnectionController(
            reportConnectivityStatus = reportConnectivityStatusMock,
            getWireguardTunnelHandle = getWireguardTunnelHandleMock,
            stopWireguardByteCountJob = stopWireguardByteCountJobMock,
            destroyWireguardTunnel = destroyWireguardTunnelMock,
            clearCache = clearCacheMock
        )
    // endregion
}
