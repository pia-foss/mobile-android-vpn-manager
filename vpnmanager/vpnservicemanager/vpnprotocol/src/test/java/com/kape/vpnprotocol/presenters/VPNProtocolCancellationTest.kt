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

package com.kape.vpnprotocol.presenters

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnmanager.api.data.externals.CoroutineContext
import com.kape.vpnmanager.api.data.model.TransportMode
import com.kape.vpnmanager.api.data.model.VpnProtocol
import com.kape.vpnprotocol.data.models.VPNProtocolConfiguration
import com.kape.vpnprotocol.data.models.VPNProtocolServer
import com.kape.vpnprotocol.data.models.VPNProtocolServerPeerInformation
import com.kape.vpnprotocol.data.models.WireguardAddKeyResponse
import com.kape.vpnprotocol.domain.controllers.common.IStartConnectionController
import com.kape.vpnprotocol.domain.controllers.common.IStartReconnectionController
import com.kape.vpnprotocol.domain.controllers.common.IStopConnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.StartWireguardConnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.StopWireguardConnectionController
import com.kape.vpnprotocol.domain.usecases.common.IClearCache
import com.kape.vpnprotocol.domain.usecases.common.IGetTargetServer
import com.kape.vpnprotocol.domain.usecases.common.IGetVpnProtocolLogs
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.common.IUpdateServerList
import com.kape.vpnprotocol.domain.usecases.wireguard.ICreateWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.IDestroyWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.IGetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.IPerformWireguardAddKeyRequest
import com.kape.vpnprotocol.domain.usecases.wireguard.ISetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.IStartWireguardByteCountJob
import com.kape.vpnprotocol.domain.usecases.wireguard.IStopWireguardByteCountJob
import com.kape.vpnprotocol.testutils.GivenModel
import com.kape.vpnprotocol.testutils.mocks.GenerateWireguardKeyPairMock
import com.kape.vpnprotocol.testutils.mocks.GenerateWireguardServerPeerInformationMock
import com.kape.vpnprotocol.testutils.mocks.GenerateWireguardSettingsMock
import com.kape.vpnprotocol.testutils.mocks.GetProtocolConfigurationMock
import com.kape.vpnprotocol.testutils.mocks.GetServerPeerInformationMock
import com.kape.vpnprotocol.testutils.mocks.IsNetworkAvailableMock
import com.kape.vpnprotocol.testutils.mocks.ProtectWireguardTunnelSocketMock
import com.kape.vpnprotocol.testutils.mocks.ServiceConfigurationFileDescriptorProviderMock
import com.kape.vpnprotocol.testutils.mocks.SetProtocolConfigurationMock
import com.kape.vpnprotocol.testutils.mocks.SetServerPeerInformationMock
import com.kape.vpnprotocol.testutils.mocks.SetServiceFileDescriptorMock
import com.kape.vpnprotocol.testutils.mocks.SetVpnServiceMock
import com.kape.vpnprotocol.testutils.mocks.SetWireguardAddKeyResponseMock
import com.kape.vpnprotocol.testutils.mocks.SetWireguardKeyPairMock
import com.kape.vpnprotocol.testutils.mocks.VPNProtocolServiceMock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The protocol layer, with the real WireGuard start and stop controllers, receiving a stop while
 * its start is still in flight.
 */
@ExperimentalCoroutinesApi
internal class VPNProtocolCancellationTest {

    // The status the start reports for `GivenModel.vpnProtocolConfiguration()`.
    private val connected = VPNManagerConnectionStatus.Connected(
        serverIp = "1.1.1.1",
        transportMode = TransportMode.UDP,
        vpnProtocol = VpnProtocol.WIREGUARD
    )

    // region fakes
    private class Recorder {
        val statuses = mutableListOf<VPNManagerConnectionStatus>()
        var tunnelsCreated = 0
        var tunnelsDestroyed = 0
        var cacheClears = 0
        var byteCountJobRunning = false
        var tunnelHandle: Int? = null
    }

    // Suspends like a real add key request over the network until `addKeyRequest` completes.
    private class Harness(scope: TestScope) {
        val recorder = Recorder()
        val addKeyRequest = CompletableDeferred<Unit>()
        var reconnection = CompletableDeferred<Result<Unit>>()
        var stopInvocations = 0

        private val reportConnectivityStatus = object : IReportConnectivityStatus {
            override suspend fun invoke(connectivityStatus: VPNManagerConnectionStatus): Result<Unit> {
                recorder.statuses.add(connectivityStatus)
                return Result.success(Unit)
            }
        }
        private val clearCache = object : IClearCache {
            override suspend fun invoke(): Result<Unit> {
                recorder.cacheClears++
                recorder.tunnelHandle = null
                recorder.byteCountJobRunning = false
                return Result.success(Unit)
            }
        }
        private val startWireguard = StartWireguardConnectionController(
            reportConnectivityStatus = reportConnectivityStatus,
            isNetworkAvailable = IsNetworkAvailableMock(shouldSucceed = true),
            setVpnService = SetVpnServiceMock(shouldSucceed = true),
            setProtocolConfiguration = SetProtocolConfigurationMock(shouldSucceed = true),
            setServiceFileDescriptor = SetServiceFileDescriptorMock(shouldSucceed = true),
            generateWireguardKeyPair = GenerateWireguardKeyPairMock(shouldSucceed = true),
            setWireguardKeyPair = SetWireguardKeyPairMock(shouldSucceed = true),
            performWireguardAddKeyRequest = object : IPerformWireguardAddKeyRequest {
                override suspend fun invoke(): Result<WireguardAddKeyResponse> {
                    addKeyRequest.await()
                    return Result.success(GivenModel.wireguardAddKeyResponse())
                }
            },
            setWireguardAddKeyResponse = SetWireguardAddKeyResponseMock(shouldSucceed = true),
            generateWireguardSettings = GenerateWireguardSettingsMock(shouldSucceed = true),
            createWireguardTunnel = object : ICreateWireguardTunnel {
                override suspend fun invoke(generatedSettings: String): Result<Int> {
                    recorder.tunnelsCreated++
                    recorder.tunnelHandle = 7
                    return Result.success(7)
                }
            },
            setWireguardTunnelHandle = object : ISetWireguardTunnelHandle {
                override suspend fun invoke(tunnelHandle: Int): Result<Unit> = Result.success(Unit)
            },
            protectWireguardTunnelSocket = ProtectWireguardTunnelSocketMock(shouldSucceed = true),
            generateWireguardServerPeerInformation = GenerateWireguardServerPeerInformationMock(shouldSucceed = true),
            startWireguardByteCountJob = object : IStartWireguardByteCountJob {
                override suspend fun invoke(): Result<Unit> {
                    recorder.byteCountJobRunning = true
                    return Result.success(Unit)
                }
            },
            setServerPeerInformation = SetServerPeerInformationMock(shouldSucceed = true),
            getServerPeerInformation = GetServerPeerInformationMock(shouldSucceed = true),
            getProtocolConfiguration = GetProtocolConfigurationMock(shouldSucceed = true),
            clearCache = clearCache
        )
        private val stopWireguard = StopWireguardConnectionController(
            reportConnectivityStatus = reportConnectivityStatus,
            getWireguardTunnelHandle = object : IGetWireguardTunnelHandle {
                override suspend fun invoke(): Result<Int> =
                    recorder.tunnelHandle?.let { Result.success(it) }
                        ?: Result.failure(VPNProtocolError(VPNProtocolErrorCode.TUNNEL_HANDLE_NOT_READY))
            },
            stopWireguardByteCountJob = object : IStopWireguardByteCountJob {
                override suspend fun invoke(): Result<Unit> =
                    if (recorder.byteCountJobRunning) {
                        recorder.byteCountJobRunning = false
                        Result.success(Unit)
                    } else {
                        Result.failure(VPNProtocolError(VPNProtocolErrorCode.NO_BYTECOUNT_JOB_FOUND))
                    }
            },
            destroyWireguardTunnel = object : IDestroyWireguardTunnel {
                override suspend fun invoke(tunnelHandle: Int): Result<Unit> {
                    recorder.tunnelsDestroyed++
                    return Result.success(Unit)
                }
            },
            clearCache = clearCache
        )

        val vpnProtocol: VPNProtocolAPI = VPNProtocol(
            startConnectionController = object : IStartConnectionController {
                override suspend fun invoke(
                    vpnService: VPNProtocolService,
                    protocolConfiguration: VPNProtocolConfiguration,
                    serviceConfigurationFileDescriptorProvider: ServiceConfigurationFileDescriptorProvider,
                ): Result<VPNProtocolServerPeerInformation> =
                    startWireguard(vpnService, protocolConfiguration, serviceConfigurationFileDescriptorProvider)
            },
            startReconnectionController = object : IStartReconnectionController {
                override suspend fun invoke(): Result<Unit> = reconnection.await()
            },
            stopConnectionController = object : IStopConnectionController {
                override suspend fun invoke(
                    protocolTarget: VPNProtocolTarget,
                    disconnectReason: DisconnectReason,
                ): Result<Unit> {
                    stopInvocations++
                    return stopWireguard(disconnectReason)
                }
            },
            getVpnProtocolLogs = object : IGetVpnProtocolLogs {
                override suspend fun invoke(protocolTarget: VPNProtocolTarget): Result<List<String>> =
                    Result.success(emptyList())
            },
            getTargetServer = object : IGetTargetServer {
                override suspend fun invoke(): Result<VPNProtocolServer> =
                    Result.failure(VPNProtocolError(VPNProtocolErrorCode.PROTOCOL_CONFIGURATION_NOT_READY))
            },
            updateServerListUseCase = object : IUpdateServerList {
                override suspend fun invoke(servers: List<VPNProtocolServer>): Result<Unit> =
                    Result.success(Unit)
            },
            coroutineContext = StandardTestDispatcher(scope.testScheduler).let {
                CoroutineContext(clientCoroutineContext = it, moduleCoroutineContext = it)
            }
        )

        val startResults = mutableListOf<Result<VPNProtocolServerPeerInformation>>()
        val stopResults = mutableListOf<Result<Unit>>()

        fun start() = vpnProtocol.startConnection(
            vpnService = VPNProtocolServiceMock(),
            protocolConfiguration = GivenModel.vpnProtocolConfiguration(),
            serviceConfigurationFileDescriptorProvider = ServiceConfigurationFileDescriptorProviderMock()
        ) { startResults.add(it) }

        fun stop() = vpnProtocol.stopConnection(
            protocolTarget = VPNProtocolTarget.WIREGUARD,
            disconnectReason = DisconnectReason.CLIENT_INITIATED
        ) { stopResults.add(it) }
    }
    // endregion

    @Test
    fun `a connect that is never cancelled behaves as before`() = runTest {
        // given
        val harness = Harness(this)

        // when
        harness.start()
        runCurrent()
        harness.addKeyRequest.complete(Unit)
        runCurrent()

        // then
        assertEquals(1, harness.startResults.size)
        assertTrue(harness.startResults.single().isSuccess)
        assertEquals(1, harness.recorder.tunnelsCreated)
        assertEquals(
            listOf(VPNManagerConnectionStatus.Connecting, connected),
            harness.recorder.statuses
        )
        assertEquals(0, harness.recorder.cacheClears)
        assertEquals(0, harness.stopInvocations)
        assertEquals(0L, currentTime)
    }

    @Test
    fun `a stop mid handshake stops the attempt before any tunnel exists`() = runTest {
        // given
        val harness = Harness(this)
        harness.start()
        runCurrent()

        // when: the add key request is still in flight
        harness.stop()
        runCurrent()
        harness.addKeyRequest.complete(Unit)
        runCurrent()

        // then
        assertEquals(0, harness.recorder.tunnelsCreated)
        assertEquals(1, harness.startResults.size)
        assertEquals(
            VPNProtocol.CONNECTION_CANCELLED_MESSAGE,
            (harness.startResults.single().exceptionOrNull() as VPNProtocolError).error?.message
        )
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
        // The cancelled start reports nothing itself. Listeners see the stop's statuses only.
        assertEquals(
            listOf(
                VPNManagerConnectionStatus.Connecting,
                VPNManagerConnectionStatus.Disconnecting,
                VPNManagerConnectionStatus.Disconnected(DisconnectReason.CLIENT_INITIATED)
            ),
            harness.recorder.statuses
        )
        assertEquals(1, harness.recorder.cacheClears)
    }

    @Test
    fun `a stop landing as the handshake completes still tears the tunnel down`() = runTest {
        // given
        val harness = Harness(this)
        harness.start()
        runCurrent()

        // when: the handshake finishes, and the stop arrives before the start resumes
        harness.addKeyRequest.complete(Unit)
        harness.stop()
        runCurrent()

        // then: whichever side won, nothing is left behind
        assertEquals(harness.recorder.tunnelsCreated, harness.recorder.tunnelsDestroyed)
        assertEquals(null, harness.recorder.tunnelHandle)
        assertEquals(false, harness.recorder.byteCountJobRunning)
        assertEquals(1, harness.startResults.size)
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
        assertEquals(
            VPNManagerConnectionStatus.Disconnected(DisconnectReason.CLIENT_INITIATED),
            harness.recorder.statuses.last()
        )
    }

    @Test
    fun `a stop after the connection completed destroys the tunnel`() = runTest {
        // given
        val harness = Harness(this)
        harness.start()
        harness.addKeyRequest.complete(Unit)
        runCurrent()

        // when
        harness.stop()
        runCurrent()

        // then
        assertTrue(harness.startResults.single().isSuccess)
        assertEquals(1, harness.recorder.tunnelsDestroyed)
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
    }

    @Test
    fun `a double stop tears down once and answers both`() = runTest {
        // given
        val harness = Harness(this)
        harness.start()
        harness.addKeyRequest.complete(Unit)
        runCurrent()

        // when
        harness.stop()
        harness.stop()
        runCurrent()

        // then
        assertEquals(1, harness.stopInvocations)
        assertEquals(1, harness.recorder.tunnelsDestroyed)
        assertEquals(listOf(Result.success(Unit), Result.success(Unit)), harness.stopResults)
    }

    @Test
    fun `a stop with no connection started succeeds without tearing anything down`() = runTest {
        // given
        val harness = Harness(this)

        // when
        harness.stop()
        runCurrent()

        // then
        assertEquals(0, harness.stopInvocations)
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
        assertTrue(harness.recorder.statuses.isEmpty())
    }

    @Test
    fun `a stop supersedes a reconnection in flight`() = runTest {
        // given
        val harness = Harness(this)
        harness.start()
        harness.addKeyRequest.complete(Unit)
        runCurrent()
        val reconnectionResults = mutableListOf<Result<Unit>>()
        harness.vpnProtocol.startReconnection { reconnectionResults.add(it) }
        runCurrent()

        // when
        harness.stop()
        runCurrent()
        harness.reconnection.complete(Result.success(Unit))
        runCurrent()

        // then
        assertEquals(1, reconnectionResults.size)
        assertEquals(
            VPNProtocol.CONNECTION_CANCELLED_MESSAGE,
            (reconnectionResults.single().exceptionOrNull() as VPNProtocolError).error?.message
        )
        assertEquals(1, harness.recorder.tunnelsDestroyed)
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
    }

    @Test
    fun `connect then cancel then connect again connects once the teardown finished`() = runTest {
        // given
        val harness = Harness(this)
        harness.start()
        runCurrent()

        // when
        harness.stop()
        harness.start()
        runCurrent()
        harness.addKeyRequest.complete(Unit)
        runCurrent()

        // then
        assertEquals(2, harness.startResults.size)
        assertTrue(harness.startResults[0].isFailure)
        assertTrue(harness.startResults[1].isSuccess)
        assertEquals(1, harness.recorder.tunnelsCreated)
        assertEquals(connected, harness.recorder.statuses.last())
    }

    @Test
    fun `rapid connect and cancel cycling leaves no tunnel and answers every call`() = runTest {
        // given
        val harness = Harness(this)
        harness.addKeyRequest.complete(Unit)
        val cycles = 100

        // when
        repeat(cycles) {
            harness.start()
            if (it % 3 == 0) runCurrent()
            harness.stop()
        }
        runCurrent()

        // then
        assertEquals(cycles, harness.startResults.size)
        assertEquals(cycles, harness.stopResults.size)
        assertTrue(harness.stopResults.all { it.isSuccess })
        assertEquals(harness.recorder.tunnelsCreated, harness.recorder.tunnelsDestroyed)
        assertEquals(null, harness.recorder.tunnelHandle)
        assertEquals(false, harness.recorder.byteCountJobRunning)
    }
}
