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

package com.kape.vpnmanager.presenters

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnmanager.api.data.externals.CoroutineContext
import com.kape.vpnmanager.data.externals.IConnectionEventAnnouncer
import com.kape.vpnmanager.data.externals.IServiceManager
import com.kape.vpnmanager.data.externals.ITargetProvider
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.data.models.ServerPeerInformation
import com.kape.vpnmanager.data.models.TransportProtocol
import com.kape.vpnmanager.domain.controllers.StartConnectionController
import com.kape.vpnmanager.testutils.GivenModel
import com.kape.vpnmanager.testutils.GivenUsecase
import com.kape.vpnmanager.testutils.mocks.GrantPermissionsMock
import com.kape.vpnmanager.testutils.mocks.SetClientConfigurationMock
import com.kape.vpnmanager.usecases.IAddConnectionListener
import com.kape.vpnmanager.usecases.IGetServerList
import com.kape.vpnmanager.usecases.IGetVpnProtocolLogs
import com.kape.vpnmanager.usecases.IRemoveConnectionListener
import com.kape.vpnmanager.usecases.ISetServerList
import com.kape.vpnmanager.usecases.StartConnection
import com.kape.vpnmanager.usecases.StartIteratingConnection
import com.kape.vpnmanager.usecases.StopConnection
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The top level API, with the real start controller and server iteration, receiving a stop while
 * a connection attempt is in flight.
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
internal class VPNManagerCancellationTest {

    // Stands in for the service layer. Each start attempt either fails right away or waits on
    // `pendingAttempt` like a real bind and handshake would.
    private class ScriptedServiceManager : IServiceManager {
        var failingAttempts = 0

        // When set, failing attempts only report their failure once it completes, and do so even
        // when the attempt was cancelled meanwhile, like a rollback that must run to completion.
        var failureReported: CompletableDeferred<Unit>? = null
        var pendingAttempt = CompletableDeferred<Result<ServerPeerInformation>>()
        var starts = 0
        var stops = 0
        var connected = false

        override suspend fun start(): Result<ServerPeerInformation> {
            starts++
            if (starts <= failingAttempts) {
                failureReported?.let {
                    withContext(NonCancellable) { it.await() }
                }
                return Result.failure(VPNManagerError(VPNManagerErrorCode.FAILED))
            }
            return pendingAttempt.await().onSuccess { connected = true }
        }

        override suspend fun stop(disconnectReason: DisconnectReason): Result<Unit> {
            stops++
            connected = false
            return Result.success(Unit)
        }

        override suspend fun getVpnProtocolLogs(
            protocolTarget: VPNManagerProtocolTarget,
        ): Result<List<String>> = Result.success(emptyList())

        override suspend fun updateServerList(serverList: ServerList): Result<Unit> = Result.success(Unit)
    }

    private class RecordingAnnouncer : IConnectionEventAnnouncer {
        val succeeded = mutableListOf<String>()
        val failed = mutableListOf<String>()

        override fun handleServerConnectAttemptSucceeded(
            serverIp: String,
            transportMode: TransportProtocol,
            vpnProtocol: VPNManagerProtocolTarget,
        ) {
            succeeded.add(serverIp)
        }

        override fun handleServerConnectAttemptFailed(
            serverIp: String,
            transportMode: TransportProtocol,
            vpnProtocol: VPNManagerProtocolTarget,
            throwable: Throwable,
        ) {
            failed.add(serverIp)
        }

        override fun handleConnectivityStatusChange(status: VPNManagerConnectionStatus) {}

        override fun handleMtuTestResult(localToRemote: Int, remoteToLocal: Int) {}
    }

    // Picks the lowest latency, like the target provider does for servers with known latencies,
    // without its own threads so the test scheduler drives everything.
    private class LowestLatencyTargetProvider : ITargetProvider {
        override suspend fun getOptimalServer(serverList: ServerList): Result<ServerList.Server> =
            Result.success(serverList.servers.minBy { it.latency!! })
    }

    private class Harness(scope: TestScope, serverCount: Int = 10) {
        val context: Context = ApplicationProvider.getApplicationContext()
        val serviceManager = ScriptedServiceManager()
        val announcer = RecordingAnnouncer()
        private val coroutineContext = StandardTestDispatcher(scope.testScheduler).let {
            CoroutineContext(clientCoroutineContext = it, moduleCoroutineContext = it)
        }

        // Lowest latency first, so servers are attempted in list order.
        private val servers = (1..serverCount).map {
            GivenModel.server().copy(ip = "10.0.0.$it", latency = it.toLong())
        }
        val api: VPNManagerAPI = VPNManager(
            startConnectionController = StartConnectionController(
                grantPermissions = GrantPermissionsMock(shouldSucceed = true),
                setClientConfiguration = SetClientConfigurationMock(shouldSucceed = true),
                getServerList = object : IGetServerList {
                    override suspend fun invoke(): Result<ServerList> = Result.success(ServerList(servers = servers))
                },
                startIteratingConnection = StartIteratingConnection(
                    targetProvider = LowestLatencyTargetProvider(),
                    setServer = GivenUsecase.setServer(),
                    startConnection = StartConnection(serviceManager = serviceManager),
                    connectionEventAnnouncer = announcer,
                    coroutineContext = coroutineContext
                )
            ),
            stopConnectionUseCase = StopConnection(serviceManager = serviceManager),
            addConnectionListenerUseCase = object : IAddConnectionListener {
                override suspend fun invoke(listener: VPNManagerConnectionListener): Result<Unit> = Result.success(Unit)
            },
            removeConnectionListenerUseCase = object : IRemoveConnectionListener {
                override suspend fun invoke(listener: VPNManagerConnectionListener): Result<Unit> = Result.success(Unit)
            },
            getVpnProtocolLogsUseCase = object : IGetVpnProtocolLogs {
                override suspend fun invoke(protocolTarget: VPNManagerProtocolTarget): Result<List<String>> =
                    Result.success(emptyList())
            },
            setServerListUseCase = object : ISetServerList {
                override suspend fun invoke(serverList: ServerList): Result<Unit> = Result.success(Unit)
            },
            coroutineContext = coroutineContext
        )
        val startResults = mutableListOf<Result<ServerPeerInformation>>()
        val stopResults = mutableListOf<Result<Unit>>()

        fun start() = api.startConnection(GivenModel.clientConfiguration(context = context)) { startResults.add(it) }

        fun stop() = api.stopConnection { stopResults.add(it) }
    }

    private fun Result<*>.isCancellation() =
        (exceptionOrNull() as? VPNManagerError)?.error?.message == VPNManager.CONNECTION_CANCELLED_MESSAGE

    @Test
    fun `a connect that is never cancelled behaves as before`() = runTest {
        // given
        val harness = Harness(this)
        harness.serviceManager.failingAttempts = 2
        harness.serviceManager.pendingAttempt.complete(Result.success(GivenModel.serverPeerInformation()))

        // when
        harness.start()
        runCurrent()

        // then
        assertEquals(listOf(Result.success(GivenModel.serverPeerInformation())), harness.startResults)
        assertEquals(3, harness.serviceManager.starts)
        assertEquals(listOf("10.0.0.1", "10.0.0.2"), harness.announcer.failed)
        assertEquals(listOf("10.0.0.3"), harness.announcer.succeeded)
        assertEquals(0, harness.serviceManager.stops)
        assertEquals(0L, currentTime)
    }

    @Test
    fun `a cancel while trying server 3 of 10 stops iterating`() = runTest {
        // given: servers 1 and 2 failed, server 3 is being attempted
        val harness = Harness(this, serverCount = 10)
        harness.serviceManager.failingAttempts = 2
        harness.start()
        runCurrent()
        assertEquals(3, harness.serviceManager.starts)

        // when
        harness.stop()
        runCurrent()
        // The service layer answering late for the cancelled attempt changes nothing.
        harness.serviceManager.pendingAttempt.complete(Result.failure(VPNManagerError(VPNManagerErrorCode.FAILED)))
        runCurrent()

        // then
        assertEquals(3, harness.serviceManager.starts)
        assertEquals(listOf("10.0.0.1", "10.0.0.2"), harness.announcer.failed)
        assertTrue(harness.announcer.succeeded.isEmpty())
        assertTrue(harness.startResults.single().isCancellation())
        assertEquals(1, harness.serviceManager.stops)
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
    }

    @Test
    fun `a cancel whose attempt still reports a failure does not move on to the next server`() = runTest {
        // given: every server fails, and server 1 is reporting its failure
        val harness = Harness(this, serverCount = 10)
        val failureReported = CompletableDeferred<Unit>()
        harness.serviceManager.failingAttempts = 10
        harness.serviceManager.failureReported = failureReported
        harness.start()
        runCurrent()

        // when: the stop lands before the failure is reported
        harness.stop()
        runCurrent()
        failureReported.complete(Unit)
        runCurrent()

        // then
        assertEquals(1, harness.serviceManager.starts)
        assertTrue(harness.startResults.single().isCancellation())
        assertEquals(1, harness.serviceManager.stops)
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
    }

    @Test
    fun `a cancel before the start ran never reaches the service layer`() = runTest {
        // given
        val harness = Harness(this)

        // when
        harness.start()
        harness.stop()
        runCurrent()

        // then
        assertEquals(0, harness.serviceManager.starts)
        assertTrue(harness.startResults.single().isCancellation())
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
    }

    @Test
    fun `a cancel landing as the connection succeeds still ends disconnected`() = runTest {
        // given
        val harness = Harness(this)
        harness.start()
        runCurrent()

        // when
        harness.serviceManager.pendingAttempt.complete(Result.success(GivenModel.serverPeerInformation()))
        harness.stop()
        runCurrent()

        // then
        assertEquals(1, harness.startResults.size)
        assertEquals(1, harness.serviceManager.stops)
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
        assertEquals(false, harness.serviceManager.connected)
    }

    @Test
    fun `connect, cancel, then connect again waits for the teardown and connects`() = runTest {
        // given
        val harness = Harness(this)
        harness.start()
        runCurrent()

        // when
        harness.stop()
        harness.start()
        runCurrent()
        harness.serviceManager.pendingAttempt.complete(Result.success(GivenModel.serverPeerInformation()))
        runCurrent()

        // then
        assertEquals(2, harness.startResults.size)
        assertTrue(harness.startResults[0].isCancellation())
        assertTrue(harness.startResults[1].isSuccess)
        assertEquals(1, harness.serviceManager.stops)
        assertTrue(harness.serviceManager.connected)
    }

    @Test
    fun `a double cancel stops the service layer once and answers both`() = runTest {
        // given
        val harness = Harness(this)
        harness.serviceManager.pendingAttempt.complete(Result.success(GivenModel.serverPeerInformation()))
        harness.start()
        runCurrent()

        // when
        harness.stop()
        harness.stop()
        runCurrent()

        // then
        assertEquals(1, harness.serviceManager.stops)
        assertEquals(listOf(Result.success(Unit), Result.success(Unit)), harness.stopResults)
    }

    @Test
    fun `a stop with nothing connected succeeds`() = runTest {
        // given
        val harness = Harness(this)

        // when
        harness.stop()
        runCurrent()

        // then
        assertEquals(1, harness.serviceManager.stops)
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
    }

    @Test
    fun `rapid connect and cancel cycling answers every call and ends disconnected`() = runTest {
        // given
        val harness = Harness(this)
        harness.serviceManager.pendingAttempt.complete(Result.success(GivenModel.serverPeerInformation()))
        val cycles = 100

        // when
        repeat(cycles) {
            harness.start()
            if (it % 2 == 0) runCurrent()
            harness.stop()
        }
        runCurrent()

        // then
        assertEquals(cycles, harness.startResults.size)
        assertEquals(cycles, harness.stopResults.size)
        assertTrue(harness.stopResults.all { it.isSuccess })
        assertEquals(false, harness.serviceManager.connected)
        // Never more than one attempt per cycle: no retrying forever after a cancel.
        assertTrue(harness.serviceManager.starts <= cycles)
    }
}
