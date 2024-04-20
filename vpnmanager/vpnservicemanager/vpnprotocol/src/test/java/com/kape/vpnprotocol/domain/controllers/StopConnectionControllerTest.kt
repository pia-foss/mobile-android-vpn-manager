package com.kape.vpnprotocol.domain.controllers

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnprotocol.domain.controllers.common.IStopConnectionController
import com.kape.vpnprotocol.domain.controllers.common.StopConnectionController
import com.kape.vpnprotocol.domain.controllers.openvpn.IStopOpenVpnConnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.IStopWireguardConnectionController
import com.kape.vpnprotocol.presenters.VPNProtocolTarget
import com.kape.vpnprotocol.testutils.mocks.StopOpenVpnConnectionControllerMock
import com.kape.vpnprotocol.testutils.mocks.StopWireguardConnectionControllerMock
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
internal class StopConnectionControllerTest {

    @Test
    fun `should invoke openvpn controller flow`() = runTest {
        // given
        val stopOpenVpnConnectionController: IStopOpenVpnConnectionController =
            StopOpenVpnConnectionControllerMock()
        val stopWireguardConnectionController: IStopWireguardConnectionController =
            StopWireguardConnectionControllerMock()
        val stopConnectionController: IStopConnectionController =
            StopConnectionController(
                stopOpenVpnConnectionController = stopOpenVpnConnectionController,
                stopWireguardConnectionController = stopWireguardConnectionController
            )

        // when
        val result = stopConnectionController(VPNProtocolTarget.OPENVPN, DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isSuccess)
        assert((stopOpenVpnConnectionController as StopOpenVpnConnectionControllerMock).invocationsCounter == 1)
        assert((stopWireguardConnectionController as StopWireguardConnectionControllerMock).invocationsCounter == 0)
    }

    @Test
    fun `should invoke wireguard controller flow`() = runTest {
        // given
        val stopOpenVpnConnectionController: IStopOpenVpnConnectionController =
            StopOpenVpnConnectionControllerMock()
        val stopWireguardConnectionController: IStopWireguardConnectionController =
            StopWireguardConnectionControllerMock()
        val stopConnectionController: IStopConnectionController =
            StopConnectionController(
                stopOpenVpnConnectionController = stopOpenVpnConnectionController,
                stopWireguardConnectionController = stopWireguardConnectionController
            )

        // when
        val result = stopConnectionController(VPNProtocolTarget.WIREGUARD, DisconnectReason.CLIENT_INITIATED)

        // then
        assert(result.isSuccess)
        assert((stopWireguardConnectionController as StopWireguardConnectionControllerMock).invocationsCounter == 1)
        assert((stopOpenVpnConnectionController as StopOpenVpnConnectionControllerMock).invocationsCounter == 0)
    }
}
