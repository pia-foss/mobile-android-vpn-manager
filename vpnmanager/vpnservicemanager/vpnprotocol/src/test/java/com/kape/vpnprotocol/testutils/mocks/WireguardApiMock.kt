package com.kape.vpnprotocol.testutils.mocks

import com.kape.wireguard.presenters.WireguardAPI

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

internal class WireguardApiMock : WireguardAPI {

    internal var invocationsPerformed: MutableMap<MethodSignature, Int> = mutableMapOf()

    internal enum class MethodSignature {
        TURN_ON,
        TURN_OFF,
        SOCKET_V4,
        SOCKET_V6,
        CONFIGURATION,
        VERSION,
    }

    override fun turnOn(
        tunnelName: String,
        builderParcelFileDescriptorFd: Int,
        settings: String,
    ): Result<Int> {
        increment(invocationsPerformed, MethodSignature.TURN_ON)
        return Result.success(1)
    }

    override fun turnOff(tunnelHandle: Int): Result<Unit> {
        increment(invocationsPerformed, MethodSignature.TURN_OFF)
        return Result.success(Unit)
    }

    override fun socketV4(tunnelHandle: Int): Result<Int> {
        increment(invocationsPerformed, MethodSignature.SOCKET_V4)
        return Result.success(1)
    }

    override fun socketV6(tunnelHandle: Int): Result<Int> {
        increment(invocationsPerformed, MethodSignature.SOCKET_V6)
        return Result.success(1)
    }

    override fun configuration(tunnelHandle: Int): Result<String> {
        increment(invocationsPerformed, MethodSignature.CONFIGURATION)
        return Result.success("configuration")
    }

    override fun version(): Result<String> {
        increment(invocationsPerformed, MethodSignature.VERSION)
        return Result.success("version")
    }

    // region private
    private fun <K> increment(map: MutableMap<K, Int>, key: K) {
        map.putIfAbsent(key, 0)
        map[key] = map[key]!! + 1
    }
    // endregion
}
