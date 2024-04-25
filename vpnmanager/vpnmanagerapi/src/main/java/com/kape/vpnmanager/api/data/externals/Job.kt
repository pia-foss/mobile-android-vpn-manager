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

import com.kape.vpnmanager.api.data.model.JobError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Job(
    private val coroutineContext: ICoroutineContext,
) : IJob {

    private var job: Job? = null

    // region IJob
    override suspend fun repeatableJob(
        delayMillis: Long,
        action: suspend () -> Unit,
    ): Result<Unit> {
        val moduleCoroutineContext = coroutineContext.getModuleCoroutineContext().getOrThrow()
        job = CoroutineScope(moduleCoroutineContext).launch {
            while (true) {
                delay(delayMillis)
                action()
            }
        }
        return Result.success(Unit)
    }

    override fun delayedJob(delayMillis: Long, action: suspend () -> Unit): Result<Unit> {
        val moduleCoroutineContext = coroutineContext.getModuleCoroutineContext().getOrThrow()
        job = CoroutineScope(moduleCoroutineContext).launch {
            delay(delayMillis)
            action()
        }
        return Result.success(Unit)
    }

    override suspend fun cancel(): Result<Unit> =
        job?.let {
            it.cancel()
            it.join()
            Result.success(Unit)
        } ?: Result.failure(JobError(code = JobError.JobErrorCode.JOB_NOT_FOUND))
    // endregion
}
