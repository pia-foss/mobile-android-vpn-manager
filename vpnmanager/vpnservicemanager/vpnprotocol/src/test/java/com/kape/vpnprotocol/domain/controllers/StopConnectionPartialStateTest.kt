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

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnprotocol.domain.controllers.openvpn.StopOpenVpnConnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.StopWireguardConnectionController
import com.kape.vpnprotocol.domain.usecases.common.IClearCache
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.openvpn.IStopOpenVpnProcess
import com.kape.vpnprotocol.domain.usecases.wireguard.IDestroyWireguardTunnel
import com.kape.vpnprotocol.presenters.VPNProtocolError
import com.kape.vpnprotocol.presenters.VPNProtocolErrorCode
import com.kape.vpnprotocol.testutils.mocks.GetWireguardTunnelHandleMock
import com.kape.vpnprotocol.testutils.mocks.StopWireguardByteCountJobMock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * A stop can supersede a start that only got partway. Teardown must then still release whatever
 * the start did set up.
 */
@ExperimentalCoroutinesApi
internal class StopConnectionPartialStateTest {

    private val statuses = mutableListOf<VPNManagerConnectionStatus>()
    private var cacheClears = 0

    private fun reportConnectivityStatus(shouldSucceed: Boolean = true) = object : IReportConnectivityStatus {
        override suspend fun invoke(connectivityStatus: VPNManagerConnectionStatus): Result<Unit> {
            statuses.add(connectivityStatus)
            return if (shouldSucceed) {
                Result.success(Unit)
            } else {
                Result.failure(VPNProtocolError(VPNProtocolErrorCode.CONNECTION_FAILURE))
            }
        }
    }

    private val clearCache = object : IClearCache {
        override suspend fun invoke(): Result<Unit> {
            cacheClears++
            return Result.success(Unit)
        }
    }

    @Test
    fun `wireguard destroys the tunnel even when the byte count job was never started`() = runTest {
        // given
        var tunnelsDestroyed = 0
        val controller = StopWireguardConnectionController(
            reportConnectivityStatus = reportConnectivityStatus(),
            getWireguardTunnelHandle = GetWireguardTunnelHandleMock(shouldSucceed = true),
            stopWireguardByteCountJob = StopWireguardByteCountJobMock(shouldSucceed = false),
            destroyWireguardTunnel = object : IDestroyWireguardTunnel {
                override suspend fun invoke(tunnelHandle: Int): Result<Unit> {
                    tunnelsDestroyed++
                    return Result.success(Unit)
                }
            },
            clearCache = clearCache
        )

        // when
        val result = controller(DisconnectReason.CLIENT_INITIATED)

        // then
        assertEquals(VPNProtocolErrorCode.NO_BYTECOUNT_JOB_FOUND, (result.exceptionOrNull() as VPNProtocolError).code)
        assertEquals(1, tunnelsDestroyed)
        assertEquals(1, cacheClears)
        assertEquals(
            listOf(
                VPNManagerConnectionStatus.Disconnecting,
                VPNManagerConnectionStatus.Disconnected(DisconnectReason.CLIENT_INITIATED)
            ),
            statuses
        )
    }

    @Test
    fun `openvpn stops the process even when reporting the disconnecting status fails`() = runTest {
        // given
        var processesStopped = 0
        val controller = StopOpenVpnConnectionController(
            reportConnectivityStatus = reportConnectivityStatus(shouldSucceed = false),
            stopOpenVpnProcess = object : IStopOpenVpnProcess {
                override suspend fun invoke(): Result<Unit> {
                    processesStopped++
                    return Result.success(Unit)
                }
            },
            clearCache = clearCache
        )

        // when
        val result = controller(DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isFailure)
        assertEquals(1, processesStopped)
        assertEquals(1, cacheClears)
    }

    @Test
    fun `openvpn reports disconnected once even when the process was not running`() = runTest {
        // given
        val controller = StopOpenVpnConnectionController(
            reportConnectivityStatus = reportConnectivityStatus(),
            stopOpenVpnProcess = object : IStopOpenVpnProcess {
                override suspend fun invoke(): Result<Unit> =
                    Result.failure(VPNProtocolError(VPNProtocolErrorCode.CONNECTION_FAILURE))
            },
            clearCache = clearCache
        )

        // when
        val result = controller(DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isFailure)
        assertEquals(
            listOf(
                VPNManagerConnectionStatus.Disconnecting,
                VPNManagerConnectionStatus.Disconnected(DisconnectReason.CLIENT_INITIATED)
            ),
            statuses
        )
        assertEquals(1, cacheClears)
    }
}
