package com.kape.vpnprotocol.domain.usecases

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kape.vpnprotocol.domain.usecases.wireguard.IProtectWireguardTunnelSocket
import com.kape.vpnprotocol.domain.usecases.wireguard.ProtectWireguardTunnelSocket
import com.kape.vpnprotocol.presenters.VPNProtocolError
import com.kape.vpnprotocol.presenters.VPNProtocolErrorCode
import com.kape.vpnprotocol.presenters.VPNProtocolService
import com.kape.vpnprotocol.testutils.GivenExternal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

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

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
internal class ProtectWireguardTunnelSocketTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `should succeed when both sockets are protected`() = runTest {
        // given
        val service = SequencedVPNProtocolService(Result.success(true), Result.success(true))

        // when
        val result = protectWireguardTunnelSocket(service)()

        // then
        assert(result.isSuccess)
        assert(service.invocations == 2)
    }

    @Test
    fun `should fail when the v4 socket protection returns false`() = runTest {
        // given
        val service = SequencedVPNProtocolService(Result.success(false), Result.success(true))

        // when
        val result = protectWireguardTunnelSocket(service)()

        // then
        assert(result.isFailure)
        assert((result.exceptionOrNull() as VPNProtocolError).code == VPNProtocolErrorCode.PROTOCOL_SERVICE_ERROR)
        assert(service.invocations == 1)
    }

    @Test
    fun `should fail when the v6 socket protection returns false`() = runTest {
        // given
        val service = SequencedVPNProtocolService(Result.success(true), Result.success(false))

        // when
        val result = protectWireguardTunnelSocket(service)()

        // then
        assert(result.isFailure)
        assert((result.exceptionOrNull() as VPNProtocolError).code == VPNProtocolErrorCode.PROTOCOL_SERVICE_ERROR)
        assert(service.invocations == 2)
    }

    @Test
    fun `should propagate the service error when socket protection throws`() = runTest {
        // given
        val error = IllegalStateException("protect failed")
        val service = SequencedVPNProtocolService(Result.failure(error), Result.success(true))

        // when
        val result = protectWireguardTunnelSocket(service)()

        // then
        assert(result.exceptionOrNull() == error)
        assert(service.invocations == 1)
    }

    private fun protectWireguardTunnelSocket(service: VPNProtocolService): IProtectWireguardTunnelSocket {
        val cache = GivenExternal.cache(context = context).apply {
            setVpnProtocolService(vpnProtocolService = service)
            setWireguardTunnelHandle(tunnelHandle = 1)
        }
        return ProtectWireguardTunnelSocket(
            cacheWireguard = cache,
            cacheService = cache,
            wireguard = GivenExternal.wireguard()
        )
    }

    private class SequencedVPNProtocolService(
        v4Result: Result<Boolean>,
        v6Result: Result<Boolean>,
    ) : VPNProtocolService {
        private val results = listOf(v4Result, v6Result)
        var invocations = 0
            private set

        override fun serviceProtect(socket: Int): Result<Boolean> = results[invocations++]
    }
}
