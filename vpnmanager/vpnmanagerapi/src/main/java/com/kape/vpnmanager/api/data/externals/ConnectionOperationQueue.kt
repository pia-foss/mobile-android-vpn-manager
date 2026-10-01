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

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Runs the start and stop requests of a connection one after the other, in the order they were
 * requested, whichever thread requests them and whichever dispatcher backs [coroutineContext].
 *
 * A stop supersedes any start still waiting or in flight: the start is cancelled, and the stop only
 * runs once the start has fully unwound. The stop therefore always tears down the final state the
 * start left behind, and a start can never complete behind the back of a stop requested after it.
 *
 * Every request gets exactly one result:
 * - A start that was superseded gets `cancelledResult`, even if it got as far as succeeding.
 * - A stop that superseded a start succeeds once its teardown has run. Teardown errors caused by
 *   the connection being only partially set up are expected in that case, and a caller cannot act
 *   on them.
 * - A stop requested while another stop is still running, with no start in between, shares the
 *   result of the running stop instead of tearing down a second time.
 */
class ConnectionOperationQueue(
    coroutineContext: CoroutineContext,
) {

    private class PendingStop(onResult: (Result<Unit>) -> Unit) {
        val callbacks = mutableListOf(onResult)
    }

    private val scope = CoroutineScope(coroutineContext + SupervisorJob())
    private val lock = Any()

    // Completes once every operation enqueued so far has completed. A cancelled start completes
    // immediately, so joining the previous operation alone would let a later operation overtake
    // an earlier one that is still running.
    private var barrier: Job? = null
    private var pendingStart: Job? = null
    private var pendingStop: PendingStop? = null

    /**
     * @param operation Start logic. Runs once all previously requested operations completed.
     * @param cancelledResult Result delivered when a stop supersedes this start.
     * @param onResult Invoked exactly once, on the thread the operation completes on.
     */
    fun <T> start(
        operation: suspend () -> Result<T>,
        cancelledResult: () -> Result<T>,
        onResult: (Result<T>) -> Unit,
    ) {
        var result: Result<T>? = null
        val job = synchronized(lock) {
            enqueue {
                result = runOperation(operation)
            }.also {
                pendingStart = it
            }
        }
        job.invokeOnCompletion { cause ->
            onResult(
                when (cause) {
                    null -> result ?: cancelledResult()
                    is CancellationException -> cancelledResult()
                    else -> Result.failure(cause)
                }
            )
        }
    }

    /**
     * @param operation Teardown logic. Runs once all previously requested operations completed,
     * including a superseded start unwinding.
     * @param skipWhenIdle When `true` and no start was requested since the last stop, succeed
     * without running [operation].
     * @param onResult Invoked exactly once, on the thread the operation completes on.
     */
    fun stop(
        operation: suspend () -> Result<Unit>,
        skipWhenIdle: Boolean = false,
        onResult: (Result<Unit>) -> Unit,
    ) {
        var superseded: Job? = null
        val enqueued = synchronized(lock) {
            if (pendingStart == null) {
                pendingStop?.let {
                    it.callbacks.add(onResult)
                    return
                }
                if (skipWhenIdle) {
                    return@synchronized false
                }
            }

            val supersededStart = pendingStart?.takeIf { it.isActive }
            pendingStart = null
            val stop = PendingStop(onResult)
            pendingStop = stop
            enqueue {
                val result = runOperation(operation)
                val deliveredResult = if (supersededStart != null) Result.success(Unit) else result
                val callbacks = synchronized(lock) {
                    if (pendingStop === stop) {
                        pendingStop = null
                    }
                    stop.callbacks.toList()
                }
                callbacks.forEach { it(deliveredResult) }
            }
            superseded = supersededStart
            true
        }
        if (!enqueued) {
            onResult(Result.success(Unit))
            return
        }
        superseded?.cancel()
    }

    /**
     * @return `true` while any requested operation has yet to complete.
     */
    internal fun hasPendingOperations(): Boolean =
        synchronized(lock) { barrier?.isActive == true }

    // region private
    // Must be called holding `lock`.
    private fun enqueue(body: suspend () -> Unit): Job {
        val previousBarrier = barrier
        val job = scope.launch {
            previousBarrier?.join()
            body()
        }
        val nextBarrier = Job()
        val completeWhenJobCompletes: () -> Unit = {
            job.invokeOnCompletion { nextBarrier.complete() }
        }
        if (previousBarrier == null) {
            completeWhenJobCompletes()
        } else {
            previousBarrier.invokeOnCompletion { completeWhenJobCompletes() }
        }
        barrier = nextBarrier
        return job
    }

    private suspend fun <T> runOperation(operation: suspend () -> Result<T>): Result<T> =
        try {
            operation()
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
    // endregion
}
