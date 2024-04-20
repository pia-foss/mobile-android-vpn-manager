package com.kape.openvpn.testutils.mocks

import com.kape.openvpn.data.externals.ICache
import com.kape.openvpn.domain.usecases.IOpenVpnProcessOutputHandler

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

internal class CacheMock : ICache {

    internal var invocationsPerformed: MutableMap<MethodSignature, Int> = mutableMapOf()

    internal enum class MethodSignature {
        CLEAR,
        SET_PROCESS,
        GET_PROCESS,
        CLEAR_PROCESS,
        GET_PROCESS_ID,
        SET_PROCESS_OUTPUT_HANDLER,
        GET_PROCESS_OUTPUT_HANDLER,
        CLEAR_PROCESS_OUTPUT_HANDLER,
    }

    // region ICache
    override fun clear(): Result<Unit> {
        increment(invocationsPerformed, MethodSignature.CLEAR)
        return Result.success(Unit)
    }

    override fun setProcess(process: Process): Result<Unit> {
        increment(invocationsPerformed, MethodSignature.SET_PROCESS)
        return Result.success(Unit)
    }

    override fun getProcess(): Result<Process> {
        increment(invocationsPerformed, MethodSignature.GET_PROCESS)
        return Result.success(ProcessMock())
    }

    override fun clearProcess(): Result<Unit> {
        increment(invocationsPerformed, MethodSignature.CLEAR_PROCESS)
        return Result.success(Unit)
    }

    override fun getProcessId(): Result<Int> {
        increment(invocationsPerformed, MethodSignature.GET_PROCESS_ID)
        return Result.success(123)
    }

    override fun setProcessOutputHandler(openVpnProcessOutputHandler: IOpenVpnProcessOutputHandler): Result<Unit> {
        increment(invocationsPerformed, MethodSignature.SET_PROCESS_OUTPUT_HANDLER)
        return Result.success(Unit)
    }

    override fun getProcessOutputHandler(): Result<IOpenVpnProcessOutputHandler> {
        increment(invocationsPerformed, MethodSignature.GET_PROCESS_OUTPUT_HANDLER)
        return Result.success(OpenVpnProcessOutputHandlerMock())
    }

    override fun clearProcessOutputHandler(): Result<Unit> {
        increment(invocationsPerformed, MethodSignature.CLEAR_PROCESS_OUTPUT_HANDLER)
        return Result.success(Unit)
    }
    // endregion

    // region private
    private fun <K> increment(map: MutableMap<K, Int>, key: K) {
        map.putIfAbsent(key, 0)
        map[key] = map[key]!! + 1
    }
    // endregion
}
