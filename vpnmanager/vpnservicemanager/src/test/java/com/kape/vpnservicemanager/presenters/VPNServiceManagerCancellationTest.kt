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

package com.kape.vpnservicemanager.presenters

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnmanager.api.data.externals.ConnectionOperationQueue
import com.kape.vpnmanager.api.data.externals.CoroutineContext
import com.kape.vpnprotocol.presenters.ServiceConfigurationFileDescriptorProvider
import com.kape.vpnprotocol.presenters.VPNProtocolService
import com.kape.vpnservicemanager.data.externals.Cache
import com.kape.vpnservicemanager.data.externals.IProtocol
import com.kape.vpnservicemanager.data.externals.Service
import com.kape.vpnservicemanager.data.externals.ServiceConnection
import com.kape.vpnservicemanager.data.externals.Subnet
import com.kape.vpnservicemanager.data.models.NetworkDetails
import com.kape.vpnservicemanager.data.models.VPNServiceServer
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation
import com.kape.vpnservicemanager.domain.controllers.StartConnectionController
import com.kape.vpnservicemanager.domain.controllers.StopConnectionController
import com.kape.vpnservicemanager.domain.datasources.ServiceGateway
import com.kape.vpnservicemanager.domain.usecases.ClearCache
import com.kape.vpnservicemanager.domain.usecases.GetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.GetVpnProtocolLogs
import com.kape.vpnservicemanager.domain.usecases.IStartReconnectionHandler
import com.kape.vpnservicemanager.domain.usecases.IsServiceCleared
import com.kape.vpnservicemanager.domain.usecases.IsServicePresent
import com.kape.vpnservicemanager.domain.usecases.SetProtocolConfiguration
import com.kape.vpnservicemanager.domain.usecases.SetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.StartConnection
import com.kape.vpnservicemanager.domain.usecases.StopConnection
import com.kape.vpnservicemanager.domain.usecases.UpdateServerList
import com.kape.vpnservicemanager.testutils.GivenModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * The service layer wired as in `VPNServiceManagerBuilder`, with a real `bindService`, `Service`,
 * `ServiceGateway` and `ServiceConnection`, and only the protocol below it faked.
 *
 * Robolectric's main looper is paused, so `onServiceConnected` is only delivered when the test
 * calls `deliverBinding()`. That makes "a stop before the bind callback" deterministic.
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
internal class VPNServiceManagerCancellationTest {

    // Behaves like the protocol layer: its own start keeps going when the service layer stops
    // waiting for it, until the service layer stops the protocol.
    private class FakeProtocol : IProtocol {
        var handshake = CompletableDeferred<Unit>()
        var starts = 0
        var stops = 0
        var attemptInFlight = false
        var connected = false
        var stopResult: Result<Unit> = Result.success(Unit)

        override suspend fun startConnection(
            vpnService: VPNProtocolService,
            allowedIps: List<NetworkDetails>,
            serviceConfigurationFileDescriptorProvider: ServiceConfigurationFileDescriptorProvider,
        ): Result<VPNServiceServerPeerInformation> {
            starts++
            attemptInFlight = true
            handshake.await()
            attemptInFlight = false
            connected = true
            return Result.success(VPNServiceServerPeerInformation(networkInterface = "tun0", gateway = "10.0.0.1"))
        }

        override suspend fun startReconnection(): Result<Unit> = Result.success(Unit)

        override suspend fun stopConnection(disconnectReason: DisconnectReason): Result<Unit> {
            stops++
            attemptInFlight = false
            connected = false
            return stopResult
        }

        override suspend fun getVpnProtocolLogs(
            protocolTarget: VPNServiceManagerProtocolTarget,
        ): Result<List<String>> = Result.success(emptyList())

        override suspend fun getTargetServer(): Result<VPNServiceServer> =
            Result.success(GivenModel.vpnServiceServer())

        override suspend fun updateServerList(servers: List<VPNServiceServer>): Result<Unit> =
            Result.success(Unit)
    }

    private class Harness(scope: TestScope) {
        val context: Application = ApplicationProvider.getApplicationContext()
        private val dispatcher = StandardTestDispatcher(scope.testScheduler)
        private val coroutineContext = CoroutineContext(
            clientCoroutineContext = dispatcher,
            moduleCoroutineContext = dispatcher
        )
        val cache = Cache()
        val protocol = FakeProtocol()
        private val connectionOperationQueue = ConnectionOperationQueue(dispatcher)
        val serviceConnection = ServiceConnection(
            context = context,
            cache = cache,
            subnet = Subnet(),
            protocol = protocol,
            coroutineContext = coroutineContext,
            connectionOperationQueue = connectionOperationQueue
        )
        private val serviceGateway = ServiceGateway(
            context = context,
            cache = cache,
            serviceConnection = serviceConnection
        )
        val service: Service = Robolectric.buildService(Service::class.java).create().get()
        private val stopConnection = StopConnection(serviceGateway = serviceGateway)
        private val clearCache = ClearCache(cache = cache)
        val api: VPNServiceManagerAPI = VPNServiceManager(
            startConnectionController = StartConnectionController(
                isServiceCleared = IsServiceCleared(cacheService = cache),
                setProtocolConfiguration = SetProtocolConfiguration(cacheProtocol = cache),
                setServerPeerInformation = SetServerPeerInformation(cacheProtocol = cache),
                startConnection = StartConnection(serviceGateway = serviceGateway),
                // The reconnection handler pings on a timer and is not what these tests are about.
                startReconnectionHandler = object : IStartReconnectionHandler {
                    override suspend fun invoke(): Result<Unit> = Result.success(Unit)
                },
                getServerPeerInformation = GetServerPeerInformation(cacheProtocol = cache),
                stopConnection = stopConnection,
                clearCache = clearCache
            ),
            stopConnectionController = StopConnectionController(
                isServicePresent = IsServicePresent(cacheService = cache),
                stopConnection = stopConnection,
                clearCache = clearCache
            ),
            getVpnProtocolLogsUseCase = GetVpnProtocolLogs(protocol = protocol),
            updateServerListUseCase = UpdateServerList(protocol = protocol),
            coroutineContext = coroutineContext,
            connectionOperationQueue = connectionOperationQueue
        )
        val startResults = mutableListOf<Result<VPNServiceServerPeerInformation>>()
        val stopResults = mutableListOf<Result<Unit>>()

        init {
            shadowOf(context).setComponentNameAndServiceForBindService(
                ComponentName(context, Service::class.java),
                service.onBind(Intent())
            )
        }

        fun start() = api.startConnection(
            protocolConfiguration = GivenModel.vpnServiceManagerConfiguration(context = context)
        ) { startResults.add(it) }

        fun stop() = api.stopConnection(DisconnectReason.CLIENT_INITIATED) { stopResults.add(it) }

        fun deliverBinding() = shadowOf(Looper.getMainLooper()).idle()

        val boundConnections get() = shadowOf(context).boundServiceConnections

        val foregroundNotification get() = shadowOf(service).lastForegroundNotification

        fun assertNothingLeaked() {
            assertTrue("service left bound", boundConnections.isEmpty())
            assertFalse("service left bound in cache", cache.isServiceBound())
            assertTrue("service left in cache", cache.getService().isFailure)
            assertFalse("protocol left connected", protocol.connected)
            assertFalse("protocol attempt left running", protocol.attemptInFlight)
        }
    }

    private fun Result<*>.isCancellation() =
        (exceptionOrNull() as? VPNServiceManagerError)?.error?.message ==
            VPNServiceManager.CONNECTION_CANCELLED_MESSAGE

    @Test
    fun `a connect that is never cancelled behaves as before`() = runTest {
        // given
        val harness = Harness(this)

        // when
        harness.start()
        runCurrent()
        harness.deliverBinding()
        runCurrent()
        harness.protocol.handshake.complete(Unit)
        runCurrent()

        // then
        assertEquals(
            listOf(Result.success(VPNServiceServerPeerInformation(networkInterface = "tun0", gateway = "10.0.0.1"))),
            harness.startResults
        )
        assertEquals(1, harness.protocol.starts)
        assertEquals(0, harness.protocol.stops)
        assertNotNull(harness.foregroundNotification)
        assertEquals(listOf(harness.serviceConnection), harness.boundConnections)
        assertTrue(harness.cache.getService().isSuccess)
        assertEquals(0L, currentTime)

        // and a regular disconnect afterwards still tears everything down
        harness.stop()
        runCurrent()
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
        assertTrue(shadowOf(harness.service).isForegroundStopped)
        harness.assertNothingLeaked()
    }

    @Test
    fun `a stop before the bind callback fired unbinds, succeeds and never starts the connection`() = runTest {
        // given: bindService was called, onServiceConnected not delivered yet
        val harness = Harness(this)
        harness.start()
        runCurrent()
        assertEquals(listOf(harness.serviceConnection), harness.boundConnections)

        // when
        harness.stop()
        runCurrent()
        // Robolectric still delivers the callback it posted before the unbind.
        harness.deliverBinding()
        runCurrent()

        // then
        assertTrue(harness.startResults.single().isCancellation())
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
        assertEquals(0, harness.protocol.starts)
        assertNull(harness.foregroundNotification)
        assertTrue(harness.serviceConnection in shadowOf(harness.context).unboundServiceConnections)
        harness.assertNothingLeaked()
    }

    @Test
    fun `a stop before the start even ran never binds`() = runTest {
        // given
        val harness = Harness(this)

        // when
        harness.start()
        harness.stop()
        runCurrent()
        harness.deliverBinding()
        runCurrent()

        // then
        assertTrue(harness.startResults.single().isCancellation())
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
        assertTrue(shadowOf(harness.context).boundServiceConnections.isEmpty())
        assertTrue(shadowOf(harness.context).unboundServiceConnections.isEmpty())
        harness.assertNothingLeaked()
    }

    @Test
    fun `a stop mid handshake stops the protocol and leaves the foreground`() = runTest {
        // given: bound and the protocol negotiating
        val harness = Harness(this)
        harness.start()
        runCurrent()
        harness.deliverBinding()
        runCurrent()
        assertTrue(harness.protocol.attemptInFlight)

        // when
        harness.stop()
        runCurrent()
        harness.protocol.handshake.complete(Unit)
        runCurrent()

        // then
        assertTrue(harness.startResults.single().isCancellation())
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
        assertEquals(1, harness.protocol.stops)
        assertTrue(shadowOf(harness.service).isForegroundStopped)
        assertTrue(shadowOf(harness.service).isStoppedBySelf)
        harness.assertNothingLeaked()
    }

    @Test
    fun `a stop landing as the connection succeeds still ends disconnected`() = runTest {
        // given
        val harness = Harness(this)
        harness.start()
        runCurrent()
        harness.deliverBinding()
        runCurrent()

        // when: the handshake completes in the same instant the stop arrives
        harness.protocol.handshake.complete(Unit)
        harness.stop()
        runCurrent()

        // then
        assertEquals(1, harness.startResults.size)
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
        assertEquals(1, harness.protocol.stops)
        harness.assertNothingLeaked()
    }

    @Test
    fun `a failing protocol stop still takes the service out of the foreground`() = runTest {
        // given: connected
        val harness = Harness(this)
        harness.protocol.handshake.complete(Unit)
        harness.start()
        runCurrent()
        harness.deliverBinding()
        runCurrent()
        assertNotNull(harness.foregroundNotification)

        // when: the protocol reports an error while stopping, as it did for NO_BYTECOUNT_JOB_FOUND
        harness.protocol.stopResult = Result.failure(Exception("NO_BYTECOUNT_JOB_FOUND"))
        harness.stop()
        runCurrent()

        // then: the stop still reports the error, but the notification is gone and nothing is bound
        assertTrue(harness.stopResults.single().isFailure)
        assertTrue(shadowOf(harness.service).isForegroundStopped)
        assertTrue(shadowOf(harness.service).isStoppedBySelf)
        assertTrue(harness.boundConnections.isEmpty())
    }

    @Test
    fun `connect, cancel, then connect again before the teardown finished connects cleanly`() = runTest {
        // given
        val harness = Harness(this)
        harness.start()
        runCurrent()
        harness.deliverBinding()
        runCurrent()

        // when
        harness.stop()
        harness.start()
        harness.protocol.handshake = CompletableDeferred()
        runCurrent()
        harness.deliverBinding()
        runCurrent()
        harness.protocol.handshake.complete(Unit)
        runCurrent()

        // then
        assertEquals(2, harness.startResults.size)
        assertTrue(harness.startResults[0].isCancellation())
        assertTrue(harness.startResults[1].isSuccess)
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
        assertTrue(harness.protocol.connected)
        assertEquals(listOf(harness.serviceConnection), harness.boundConnections)
    }

    @Test
    fun `a double stop tears down once and answers both without throwing`() = runTest {
        // given
        val harness = Harness(this)
        harness.start()
        runCurrent()
        harness.deliverBinding()
        runCurrent()

        // when
        harness.stop()
        harness.stop()
        runCurrent()

        // then
        assertEquals(1, harness.protocol.stops)
        assertEquals(listOf(Result.success(Unit), Result.success(Unit)), harness.stopResults)
        harness.assertNothingLeaked()
    }

    @Test
    fun `a stop with nothing to stop succeeds and touches nothing`() = runTest {
        // given
        val harness = Harness(this)

        // when
        harness.stop()
        runCurrent()

        // then
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
        assertTrue(shadowOf(harness.context).unboundServiceConnections.isEmpty())
        assertEquals(0, harness.protocol.stops)
    }

    @Test
    fun `a revocation mid handshake supersedes the start and cleans up`() = runTest {
        // given
        val harness = Harness(this)
        harness.start()
        runCurrent()
        harness.deliverBinding()
        runCurrent()

        // when: the system revokes the VPN permission while negotiating
        harness.service.onRevoke()
        runCurrent()

        // then
        assertTrue(harness.startResults.single().isCancellation())
        assertEquals(1, harness.protocol.stops)
        harness.assertNothingLeaked()

        // and connecting again afterwards works
        harness.protocol.handshake = CompletableDeferred()
        harness.protocol.handshake.complete(Unit)
        harness.start()
        runCurrent()
        harness.deliverBinding()
        runCurrent()
        assertTrue(harness.startResults.last().isSuccess)
    }

    @Test
    fun `a revocation while a stop is tearing down does not tear down twice`() = runTest {
        // given
        val harness = Harness(this)
        harness.start()
        runCurrent()
        harness.deliverBinding()
        runCurrent()

        // when
        harness.stop()
        harness.service.onRevoke()
        runCurrent()

        // then
        assertEquals(1, harness.protocol.stops)
        assertEquals(listOf(Result.success(Unit)), harness.stopResults)
        harness.assertNothingLeaked()
    }

    @Test
    fun `a bind that never calls back times out and is released`() = runTest {
        // given
        val harness = Harness(this)
        harness.start()
        runCurrent()

        // when: onServiceConnected never arrives
        advanceTimeBy(10_001)
        runCurrent()

        // then
        assertEquals(
            VPNServiceManagerErrorCode.SERVICE_CONNECTION_TIMED_OUT,
            (harness.startResults.single().exceptionOrNull() as VPNServiceManagerError).code
        )
        harness.deliverBinding()
        runCurrent()
        assertEquals(0, harness.protocol.starts)
        harness.assertNothingLeaked()
    }

    @Test
    fun `rapid connect and cancel cycling leaks no binding, service or protocol attempt`() = runTest {
        // given
        val harness = Harness(this)
        harness.protocol.handshake.complete(Unit)
        val cycles = 60

        // when: cancel at every possible point of the attempt
        repeat(cycles) { cycle ->
            harness.start()
            when (cycle % 4) {
                0 -> Unit
                1 -> runCurrent()
                2 -> {
                    runCurrent()
                    harness.deliverBinding()
                }
                3 -> {
                    runCurrent()
                    harness.deliverBinding()
                    runCurrent()
                }
            }
            harness.stop()
        }
        runCurrent()
        harness.deliverBinding()
        runCurrent()

        // then
        assertEquals(cycles, harness.startResults.size)
        assertEquals(cycles, harness.stopResults.size)
        assertTrue(harness.stopResults.all { it.isSuccess })
        assertEquals(harness.protocol.starts, harness.protocol.stops)
        assertNull(harness.cache.getGateway().getOrNull())
        harness.assertNothingLeaked()
    }
}
