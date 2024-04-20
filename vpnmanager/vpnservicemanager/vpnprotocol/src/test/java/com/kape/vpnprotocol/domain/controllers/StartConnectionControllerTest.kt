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

import com.kape.vpnprotocol.domain.controllers.common.IStartConnectionController
import com.kape.vpnprotocol.domain.controllers.common.StartConnectionController
import com.kape.vpnprotocol.domain.controllers.openvpn.IStartOpenVpnConnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.IStartWireguardConnectionController
import com.kape.vpnprotocol.presenters.VPNProtocolTarget
import com.kape.vpnprotocol.testutils.GivenModel
import com.kape.vpnprotocol.testutils.mocks.ServiceConfigurationFileDescriptorProviderMock
import com.kape.vpnprotocol.testutils.mocks.StartOpenVpnConnectionControllerMock
import com.kape.vpnprotocol.testutils.mocks.StartWireguardConnectionControllerMock
import com.kape.vpnprotocol.testutils.mocks.VPNProtocolServiceMock
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
internal class StartConnectionControllerTest {

    @Test
    fun `should invoke openvpn controller flow if set in the configuration`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val serviceConfigurationFileDescriptorProvider =
            ServiceConfigurationFileDescriptorProviderMock()
        val protocolConfigurationMock =
            GivenModel.vpnProtocolConfiguration(protocolTarget = VPNProtocolTarget.OPENVPN)
        val startOpenVpnConnectionControllerMock: IStartOpenVpnConnectionController =
            StartOpenVpnConnectionControllerMock()
        val startWireguardConnectionControllerMock: IStartWireguardConnectionController =
            StartWireguardConnectionControllerMock()
        val startConnectionController: IStartConnectionController =
            StartConnectionController(
                startOpenVpnConnectionController = startOpenVpnConnectionControllerMock,
                startWireguardConnectionController = startWireguardConnectionControllerMock
            )

        // when
        val result = startConnectionController(
            protocolConfiguration = protocolConfigurationMock,
            vpnService = vpnProtocolServiceMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProvider
        )

        // then
        assert(result.isSuccess)
        assert((startOpenVpnConnectionControllerMock as StartOpenVpnConnectionControllerMock).invocationsCounter == 1)
        assert((startWireguardConnectionControllerMock as StartWireguardConnectionControllerMock).invocationsCounter == 0)
    }

    @Test
    fun `should invoke wireguard controller flow if set in the configuration`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val serviceConfigurationFileDescriptorProvider =
            ServiceConfigurationFileDescriptorProviderMock()
        val protocolConfigurationMock =
            GivenModel.vpnProtocolConfiguration(protocolTarget = VPNProtocolTarget.WIREGUARD)
        val startOpenVpnConnectionControllerMock: IStartOpenVpnConnectionController =
            StartOpenVpnConnectionControllerMock()
        val startWireguardConnectionControllerMock: IStartWireguardConnectionController =
            StartWireguardConnectionControllerMock()
        val startConnectionController: IStartConnectionController =
            StartConnectionController(
                startOpenVpnConnectionController = startOpenVpnConnectionControllerMock,
                startWireguardConnectionController = startWireguardConnectionControllerMock
            )

        // when
        val result = startConnectionController(
            protocolConfiguration = protocolConfigurationMock,
            vpnService = vpnProtocolServiceMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProvider
        )

        // then
        assert(result.isSuccess)
        assert((startWireguardConnectionControllerMock as StartWireguardConnectionControllerMock).invocationsCounter == 1)
        assert((startOpenVpnConnectionControllerMock as StartOpenVpnConnectionControllerMock).invocationsCounter == 0)
    }
}
