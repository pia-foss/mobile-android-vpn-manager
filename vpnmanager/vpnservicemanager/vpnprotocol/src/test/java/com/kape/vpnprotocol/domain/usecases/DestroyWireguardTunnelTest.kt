package com.kape.vpnprotocol.domain.usecases

import com.kape.vpnprotocol.data.externals.wireguard.IWireguard
import com.kape.vpnprotocol.domain.usecases.wireguard.DestroyWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.IDestroyWireguardTunnel
import com.kape.vpnprotocol.testutils.GivenExternal
import com.kape.vpnprotocol.testutils.mocks.WireguardApiMock
import com.kape.wireguard.presenters.WireguardAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

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
internal class DestroyWireguardTunnelTest {

    @Test
    fun `should access the turn off wireguard module api when destroying the tunnel`() = runTest {
        // given
        val tunnelHandleMock = 1234
        val wireguardApiMock: WireguardAPI = WireguardApiMock()
        val wireguard: IWireguard = GivenExternal.wireguard(wireguardApi = wireguardApiMock)
        val destroyWireguardTunnel: IDestroyWireguardTunnel =
            DestroyWireguardTunnel(wireguard = wireguard)

        // when
        val result = destroyWireguardTunnel(tunnelHandle = tunnelHandleMock)

        // then
        assert(result.isSuccess)
        assert((wireguardApiMock as WireguardApiMock).invocationsPerformed[WireguardApiMock.MethodSignature.TURN_OFF] == 1)
    }
}
