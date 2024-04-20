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

package com.kape.vpnprotocol.domain.controllers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kape.vpnprotocol.data.externals.common.ICacheProtocol
import com.kape.vpnprotocol.domain.controllers.common.IStartReconnectionController
import com.kape.vpnprotocol.domain.controllers.common.StartReconnectionController
import com.kape.vpnprotocol.domain.controllers.openvpn.IStartOpenVpnReconnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.IStartWireguardReconnectionController
import com.kape.vpnprotocol.domain.usecases.common.GetTargetProtocol
import com.kape.vpnprotocol.domain.usecases.common.IGetTargetProtocol
import com.kape.vpnprotocol.presenters.VPNProtocolTarget
import com.kape.vpnprotocol.testutils.GivenExternal
import com.kape.vpnprotocol.testutils.GivenModel
import com.kape.vpnprotocol.testutils.mocks.StartOpenVpnReconnectionControllerMock
import com.kape.vpnprotocol.testutils.mocks.StartWireguardReconnectionControllerMock
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
internal class StartReconnectionControllerTest {

    @Test
    fun `should invoke openvpn controller flow if that is the known target protocol`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val protocolConfigurationMock =
            GivenModel.vpnProtocolConfiguration(protocolTarget = VPNProtocolTarget.OPENVPN)
        val cacheProtocol: ICacheProtocol = GivenExternal.cache(context = context).apply {
            setProtocolConfiguration(protocolConfiguration = protocolConfigurationMock)
        }
        val startOpenVpnReconnectionControllerMock: IStartOpenVpnReconnectionController =
            StartOpenVpnReconnectionControllerMock()
        val startWireguardReconnectionControllerMock: IStartWireguardReconnectionController =
            StartWireguardReconnectionControllerMock()
        val getTargetProtocol: IGetTargetProtocol =
            GetTargetProtocol(cacheProtocol = cacheProtocol)
        val startReconnectionController: IStartReconnectionController =
            StartReconnectionController(
                getTargetProtocol = getTargetProtocol,
                startOpenVpnReconnectionController = startOpenVpnReconnectionControllerMock,
                startWireguardReconnectionController = startWireguardReconnectionControllerMock
            )

        // when
        val result = startReconnectionController()

        // then
        assert(result.isSuccess)
        assert((startOpenVpnReconnectionControllerMock as StartOpenVpnReconnectionControllerMock).invocationsCounter == 1)
        assert((startWireguardReconnectionControllerMock as StartWireguardReconnectionControllerMock).invocationsCounter == 0)
    }

    @Test
    fun `should invoke wireguard controller flow if that is the known target protocol`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val protocolConfigurationMock =
            GivenModel.vpnProtocolConfiguration(protocolTarget = VPNProtocolTarget.WIREGUARD)
        val cacheProtocol: ICacheProtocol = GivenExternal.cache(context = context).apply {
            setProtocolConfiguration(protocolConfiguration = protocolConfigurationMock)
        }
        val startOpenVpnReconnectionControllerMock: IStartOpenVpnReconnectionController =
            StartOpenVpnReconnectionControllerMock()
        val startWireguardReconnectionControllerMock: IStartWireguardReconnectionController =
            StartWireguardReconnectionControllerMock()
        val getTargetProtocol: IGetTargetProtocol =
            GetTargetProtocol(cacheProtocol = cacheProtocol)
        val startReconnectionController: IStartReconnectionController = StartReconnectionController(
            getTargetProtocol = getTargetProtocol,
            startOpenVpnReconnectionController = startOpenVpnReconnectionControllerMock,
            startWireguardReconnectionController = startWireguardReconnectionControllerMock
        )

        // when
        val result = startReconnectionController()

        // then
        assert(result.isSuccess)
        assert((startWireguardReconnectionControllerMock as StartWireguardReconnectionControllerMock).invocationsCounter == 1)
        assert((startOpenVpnReconnectionControllerMock as StartOpenVpnReconnectionControllerMock).invocationsCounter == 0)
    }
}
