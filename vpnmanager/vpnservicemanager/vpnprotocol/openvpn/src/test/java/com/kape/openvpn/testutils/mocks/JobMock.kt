package com.kape.openvpn.testutils.mocks

import com.kape.vpnmanager.api.data.externals.IJob

internal class JobMock : IJob {

    internal var invocationsPerformed: MutableMap<MethodSignature, Int> = mutableMapOf()

    internal enum class MethodSignature {
        REPEATABLE_JOB,
        DELAYED_JOB,
        CANCEL_JOB,
    }

    override suspend fun repeatableJob(
        delayMillis: Long,
        action: suspend () -> Unit,
    ): Result<Unit> {
        increment(invocationsPerformed, MethodSignature.REPEATABLE_JOB)
        return Result.success(Unit)
    }

    override fun delayedJob(delayMillis: Long, action: suspend () -> Unit): Result<Unit> {
        increment(invocationsPerformed, MethodSignature.DELAYED_JOB)
        return Result.success(Unit)
    }

    override suspend fun cancel(): Result<Unit> {
        increment(invocationsPerformed, MethodSignature.CANCEL_JOB)
        return Result.success(Unit)
    }

    // region private
    private fun <K> increment(map: MutableMap<K, Int>, key: K) {
        map.putIfAbsent(key, 0)
        map[key] = map[key]!! + 1
    }
    // endregion
}
