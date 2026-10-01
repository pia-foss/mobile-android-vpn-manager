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

package com.kape.vpnmanager.api.data.externals

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@ExperimentalCoroutinesApi
internal class ConnectionOperationQueueTest {

    private val cancelled = Result.failure<String>(Exception("cancelled"))

    private fun TestScope.queue() = ConnectionOperationQueue(StandardTestDispatcher(testScheduler))

    @Test
    fun `a start that is never stopped delivers its own result once, without added delay`() = runTest {
        // given
        val queue = queue()
        val results = mutableListOf<Result<String>>()
        var invocations = 0

        // when
        queue.start(
            operation = {
                invocations++
                Result.success("connected")
            },
            cancelledResult = { cancelled },
            onResult = { results.add(it) }
        )
        runCurrent()

        // then
        assertEquals(listOf(Result.success("connected")), results)
        assertEquals(1, invocations)
        assertEquals(0L, currentTime)
        assertFalse(queue.hasPendingOperations())
    }

    @Test
    fun `a stop requested before the start ran cancels it without running it`() = runTest {
        // given
        val queue = queue()
        val startResults = mutableListOf<Result<String>>()
        val stopResults = mutableListOf<Result<Unit>>()
        var startInvocations = 0

        // when
        queue.start(
            operation = {
                startInvocations++
                Result.success("connected")
            },
            cancelledResult = { cancelled },
            onResult = { startResults.add(it) }
        )
        queue.stop(operation = { Result.success(Unit) }, onResult = { stopResults.add(it) })
        runCurrent()

        // then
        assertEquals(0, startInvocations)
        assertEquals(listOf(cancelled), startResults)
        assertEquals(listOf(Result.success(Unit)), stopResults)
        assertFalse(queue.hasPendingOperations())
    }

    @Test
    fun `a stop cancels an in flight start and only tears down once the start unwound`() = runTest {
        // given
        val queue = queue()
        val events = mutableListOf<String>()
        val handshake = CompletableDeferred<Unit>()
        val startResults = mutableListOf<Result<String>>()

        queue.start(
            operation = {
                events.add("start began")
                try {
                    handshake.await()
                    events.add("start completed handshake")
                    Result.success("connected")
                } finally {
                    events.add("start unwound")
                }
            },
            cancelledResult = { cancelled },
            onResult = { startResults.add(it) }
        )
        runCurrent()

        // when
        queue.stop(operation = { events.add("stop ran"); Result.success(Unit) }, onResult = { })
        handshake.complete(Unit)
        runCurrent()

        // then
        assertEquals(listOf("start began", "start unwound", "stop ran"), events)
        assertEquals(listOf(cancelled), startResults)
    }

    @Test
    fun `a stop that superseded a start succeeds even when teardown reports missing state`() = runTest {
        // given
        val queue = queue()
        val stopResults = mutableListOf<Result<Unit>>()
        queue.start(
            operation = { CompletableDeferred<Result<String>>().await() },
            cancelledResult = { cancelled },
            onResult = { }
        )
        runCurrent()

        // when
        queue.stop(
            operation = { Result.failure(Exception("nothing to stop")) },
            onResult = { stopResults.add(it) }
        )
        runCurrent()

        // then
        assertEquals(listOf(Result.success(Unit)), stopResults)
    }

    @Test
    fun `a stop after a completed start is not superseding and reports its own result`() = runTest {
        // given
        val queue = queue()
        val startResults = mutableListOf<Result<String>>()
        val stopResults = mutableListOf<Result<Unit>>()
        val failure = Result.failure<Unit>(Exception("teardown failed"))
        queue.start(
            operation = { Result.success("connected") },
            cancelledResult = { cancelled },
            onResult = { startResults.add(it) }
        )
        runCurrent()

        // when
        queue.stop(operation = { failure }, onResult = { stopResults.add(it) })
        runCurrent()

        // then
        assertEquals(listOf(Result.success("connected")), startResults)
        assertEquals(listOf(failure), stopResults)
    }

    @Test
    fun `a stop landing as the start completes still leaves one result each and a teardown`() = runTest {
        // given
        val queue = queue()
        val handshake = CompletableDeferred<Unit>()
        val startResults = mutableListOf<Result<String>>()
        val stopInvocations = AtomicInteger()
        queue.start(
            operation = {
                handshake.await()
                Result.success("connected")
            },
            cancelledResult = { cancelled },
            onResult = { startResults.add(it) }
        )
        runCurrent()

        // when: the handshake completes, but the stop lands before the start resumes
        handshake.complete(Unit)
        queue.stop(operation = { stopInvocations.incrementAndGet(); Result.success(Unit) }, onResult = { })
        runCurrent()

        // then
        assertEquals(listOf(cancelled), startResults)
        assertEquals(1, stopInvocations.get())
    }

    @Test
    fun `a start requested while a stop tears down waits for the teardown to finish`() = runTest {
        // given
        val queue = queue()
        val events = mutableListOf<String>()
        val teardown = CompletableDeferred<Unit>()
        queue.start(
            operation = { CompletableDeferred<Result<String>>().await() },
            cancelledResult = { cancelled },
            onResult = { }
        )
        queue.stop(
            operation = {
                events.add("stop began")
                teardown.await()
                events.add("stop finished")
                Result.success(Unit)
            },
            onResult = { }
        )
        runCurrent()

        // when
        queue.start(
            operation = { events.add("second start ran"); Result.success("connected") },
            cancelledResult = { cancelled },
            onResult = { }
        )
        runCurrent()
        val eventsWhileTearingDown = events.toList()
        teardown.complete(Unit)
        runCurrent()

        // then
        assertEquals(listOf("stop began"), eventsWhileTearingDown)
        assertEquals(listOf("stop began", "stop finished", "second start ran"), events)
    }

    @Test
    fun `a stop never overtakes an earlier stop, even through a cancelled start`() = runTest {
        // given: start, stop (tearing down), start, stop
        val queue = queue()
        val running = AtomicInteger()
        val maxRunning = AtomicInteger()
        val firstTeardown = CompletableDeferred<Unit>()
        val stopOperation: suspend (CompletableDeferred<Unit>?) -> Result<Unit> = { gate ->
            maxRunning.set(maxOf(maxRunning.get(), running.incrementAndGet()))
            gate?.await()
            running.decrementAndGet()
            Result.success(Unit)
        }
        val startResults = mutableListOf<Result<String>>()

        // when
        queue.start({ Result.success("first") }, { cancelled }, { })
        queue.stop(operation = { stopOperation(firstTeardown) }, onResult = { })
        runCurrent()
        queue.start({ Result.success("second") }, { cancelled }, { startResults.add(it) })
        queue.stop(operation = { stopOperation(null) }, onResult = { })
        runCurrent()
        firstTeardown.complete(Unit)
        runCurrent()

        // then
        assertEquals(1, maxRunning.get())
        assertEquals(listOf(cancelled), startResults)
        assertFalse(queue.hasPendingOperations())
    }

    @Test
    fun `a second stop while the first is running shares its result and does not tear down twice`() = runTest {
        // given
        val queue = queue()
        val teardown = CompletableDeferred<Unit>()
        var stopInvocations = 0
        val stopResults = mutableListOf<Result<Unit>>()
        val failure = Result.failure<Unit>(Exception("teardown failed"))

        // when
        queue.stop(
            operation = {
                stopInvocations++
                teardown.await()
                failure
            },
            onResult = { stopResults.add(it) }
        )
        runCurrent()
        queue.stop(operation = { stopInvocations++; Result.success(Unit) }, onResult = { stopResults.add(it) })
        teardown.complete(Unit)
        runCurrent()

        // then
        assertEquals(1, stopInvocations)
        assertEquals(listOf(failure, failure), stopResults)
    }

    @Test
    fun `a stop with nothing started runs its teardown, or skips it when asked to`() = runTest {
        // given
        val queue = queue()
        var invocations = 0
        val results = mutableListOf<Result<Unit>>()
        val failure = Result.failure<Unit>(Exception("service not ready"))

        // when
        queue.stop(operation = { invocations++; failure }, onResult = { results.add(it) })
        runCurrent()
        queue.stop(operation = { invocations++; failure }, skipWhenIdle = true, onResult = { results.add(it) })
        runCurrent()

        // then
        assertEquals(1, invocations)
        assertEquals(listOf(failure, Result.success(Unit)), results)
    }

    @Test
    fun `an operation throwing is delivered as a failure and the queue keeps working`() = runTest {
        // given
        val queue = queue()
        val results = mutableListOf<Result<String>>()
        val exception = IllegalStateException("boom")

        // when
        queue.start({ throw exception }, { cancelled }, { results.add(it) })
        runCurrent()
        queue.stop(operation = { Result.success(Unit) }, onResult = { })
        queue.start({ Result.success("connected") }, { cancelled }, { results.add(it) })
        runCurrent()

        // then
        assertEquals(listOf(Result.failure<String>(exception), Result.success("connected")), results)
    }

    @Test
    fun `rapid start and stop cycling from many threads stays ordered, answers every call and leaks nothing`() {
        // given: a multi-threaded dispatcher, as a client could configure for the module context
        val executor = Executors.newFixedThreadPool(4)
        val dispatcher = executor.asCoroutineDispatcher()
        val queue = ConnectionOperationQueue(dispatcher)
        val cycles = 500
        val connected = AtomicBoolean(false)
        val running = AtomicInteger()
        val overlaps = AtomicInteger()
        val callbacks = AtomicInteger()
        val allAnswered = CountDownLatch(cycles * 2 + 1)
        val stopResults = Collections.synchronizedList(mutableListOf<Result<Unit>>())
        val track: suspend (suspend () -> Unit) -> Unit = { body ->
            if (running.incrementAndGet() > 1) overlaps.incrementAndGet()
            try {
                body()
            } finally {
                running.decrementAndGet()
            }
        }
        val callers = Executors.newFixedThreadPool(4)

        // when
        repeat(cycles) {
            callers.execute {
                queue.start(
                    operation = {
                        track {
                            yield()
                            connected.set(true)
                        }
                        Result.success("connected")
                    },
                    cancelledResult = { cancelled },
                    onResult = { callbacks.incrementAndGet(); allAnswered.countDown() }
                )
                queue.stop(
                    operation = { track { connected.set(false) }; Result.success(Unit) },
                    onResult = { callbacks.incrementAndGet(); allAnswered.countDown() }
                )
            }
        }
        callers.shutdown()
        callers.awaitTermination(10, TimeUnit.SECONDS)
        queue.stop(
            operation = { track { connected.set(false) }; Result.success(Unit) },
            onResult = { stopResults.add(it); allAnswered.countDown() }
        )

        // then
        assertTrue(allAnswered.await(10, TimeUnit.SECONDS))
        assertEquals(cycles * 2, callbacks.get())
        assertEquals(0, overlaps.get())
        assertFalse(connected.get())
        assertEquals(listOf(Result.success(Unit)), stopResults)
        // Callbacks fire just before their operation's job completes.
        val deadline = System.currentTimeMillis() + 5_000
        while (queue.hasPendingOperations() && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
        assertFalse(queue.hasPendingOperations())
        executor.shutdown()
    }
}
