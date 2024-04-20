package com.kape.vpnprotocol.domain.usecases

import com.kape.vpnprotocol.data.externals.common.IProcess
import com.kape.vpnprotocol.data.models.ProcessStreams
import com.kape.vpnprotocol.domain.usecases.common.IGetVpnProtocolLogs
import com.kape.vpnprotocol.presenters.VPNProtocolTarget
import com.kape.vpnprotocol.testutils.GivenUsecase
import com.kape.vpnprotocol.testutils.mocks.ProcessMock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.InputStream

/*
 *  Copyright (c) 2023 Private Internet Access, Inc.
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
internal class GetVpnProtocolLogsTest {

    @Test
    fun `should succeed with openvpn logs when present on input stream`() = runTest {
        // given
        val inputStreamMock: InputStream = "openvpn hello world\n something else".byteInputStream()
        val processStreamsMock = ProcessStreams(
            inputStream = inputStreamMock,
            errorStream = InputStream.nullInputStream()
        )
        val processMock: IProcess = ProcessMock(processStreams = processStreamsMock)
        val getVpnProtocolLogs: IGetVpnProtocolLogs = GivenUsecase.getVpnProtocolLogs(process = processMock)

        // when
        val result = getVpnProtocolLogs(VPNProtocolTarget.OPENVPN)

        // then
        assert(result.isSuccess)
        assert(result.getOrThrow().size == 1)
        assert(result.getOrThrow().first() == "openvpn hello world")
    }

    @Test
    fun `should succeed with wireguard logs when present on input stream`() = runTest {
        // given
        val inputStreamMock: InputStream = "wireguard hello world\n something else".byteInputStream()
        val processStreamsMock = ProcessStreams(
            inputStream = inputStreamMock,
            errorStream = InputStream.nullInputStream()
        )
        val processMock: IProcess = ProcessMock(processStreams = processStreamsMock)
        val getVpnProtocolLogs: IGetVpnProtocolLogs = GivenUsecase.getVpnProtocolLogs(process = processMock)

        // when
        val result = getVpnProtocolLogs(VPNProtocolTarget.WIREGUARD)

        // then
        assert(result.isSuccess)
        assert(result.getOrThrow().size == 1)
        assert(result.getOrThrow().first() == "wireguard hello world")
    }

    @Test
    fun `should succeed with error logs when no logs are found on input stream but some are present on error stream`() = runTest {
        // given
        val errorStreamMock: InputStream = "wireguard this is an error\n something else".byteInputStream()
        val processStreamsMock = ProcessStreams(
            inputStream = InputStream.nullInputStream(),
            errorStream = errorStreamMock
        )
        val processMock: IProcess = ProcessMock(processStreams = processStreamsMock)
        val getVpnProtocolLogs: IGetVpnProtocolLogs = GivenUsecase.getVpnProtocolLogs(process = processMock)

        // when
        val result = getVpnProtocolLogs(VPNProtocolTarget.WIREGUARD)

        // then
        assert(result.isSuccess)
        assert(result.getOrThrow().size == 1)
        assert(result.getOrThrow().first() == "wireguard this is an error")
    }

    @Test
    fun `should fail when no logs are found on the input stream or the error stream`() = runTest {
        // given
        val processStreamsMock = ProcessStreams(
            inputStream = InputStream.nullInputStream(),
            errorStream = InputStream.nullInputStream()
        )
        val processMock: IProcess = ProcessMock(processStreams = processStreamsMock)
        val getVpnProtocolLogs: IGetVpnProtocolLogs = GivenUsecase.getVpnProtocolLogs(process = processMock)

        // when
        val result = getVpnProtocolLogs(VPNProtocolTarget.WIREGUARD)

        // then
        assert(result.isFailure)
    }
}
