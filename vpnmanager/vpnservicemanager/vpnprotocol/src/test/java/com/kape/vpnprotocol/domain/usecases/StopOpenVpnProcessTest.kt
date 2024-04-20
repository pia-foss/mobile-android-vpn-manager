package com.kape.vpnprotocol.domain.usecases

import com.kape.openvpn.presenters.OpenVpnAPI
import com.kape.vpnprotocol.data.externals.openvpn.IOpenVpn
import com.kape.vpnprotocol.domain.usecases.openvpn.IStopOpenVpnProcess
import com.kape.vpnprotocol.domain.usecases.openvpn.StopOpenVpnProcess
import com.kape.vpnprotocol.testutils.GivenExternal
import com.kape.vpnprotocol.testutils.mocks.OpenVpnApiMock
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
internal class StopOpenVpnProcessTest {

    @Test
    fun `should access the stop openvpn module api when stopping the tunnel`() = runTest {
        // given
        val openVpnApiMock: OpenVpnAPI = OpenVpnApiMock()
        val openVpn: IOpenVpn = GivenExternal.openVpn(openVpnApiMock)
        val stopOpenVpnProcess: IStopOpenVpnProcess =
            StopOpenVpnProcess(openVpn = openVpn)

        // when
        val result = stopOpenVpnProcess()

        // then
        assert(result.isSuccess)
        assert((openVpnApiMock as OpenVpnApiMock).invocationsPerformed[OpenVpnApiMock.MethodSignature.STOP] == 1)
    }
}
