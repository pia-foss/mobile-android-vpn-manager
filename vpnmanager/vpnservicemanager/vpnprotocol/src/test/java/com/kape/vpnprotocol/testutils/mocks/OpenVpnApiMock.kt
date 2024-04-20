package com.kape.vpnprotocol.testutils.mocks

import com.kape.openvpn.presenters.OpenVpnAPI
import com.kape.openvpn.presenters.OpenVpnCallback
import com.kape.openvpn.presenters.OpenVpnProcessEventHandler

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

internal class OpenVpnApiMock : OpenVpnAPI {

    internal var invocationsPerformed: MutableMap<MethodSignature, Int> = mutableMapOf()

    internal enum class MethodSignature {
        START,
        STOP,
    }

    override fun start(
        commandLineParams: List<String>,
        openVpnProcessEventHandler: OpenVpnProcessEventHandler,
        callback: OpenVpnCallback,
    ) {
        increment(invocationsPerformed, MethodSignature.START)
        callback(Result.success(Unit))
    }

    override fun stop(callback: OpenVpnCallback) {
        increment(invocationsPerformed, MethodSignature.STOP)
        callback(Result.success(Unit))
    }

    // region private
    private fun <K> increment(map: MutableMap<K, Int>, key: K) {
        map.putIfAbsent(key, 0)
        map[key] = map[key]!! + 1
    }
    // endregion
}
