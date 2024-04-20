package com.kape.vpnprotocol.domain.usecases.openvpn

import com.kape.vpnprotocol.testutils.GivenModel
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

class FilterAdditionalOpenVpnParamsTest {

    // SUT
    private val usecase = FilterAdditionalOpenVpnParams()

    @Test
    fun `non whitelisted options are removed from the OpenVpn configuration`() = runTest {
        // given
        val nonWhitelistedParams = "--up 'my-up-script.vbs' --client-connect"
        val openVpnConfig = GivenModel.openVpnConfig(additionalParameters = nonWhitelistedParams)
        val config = GivenModel.protocolConfiguration(openVpnClientConfiguration = openVpnConfig)

        // when
        val filteredConfig = usecase(config)

        // then
        assert(filteredConfig.getOrNull()?.openVpnClientConfiguration?.additionalParameters?.isBlank() ?: false)
    }

    @Test
    fun `whitelisted options are kept in the OpenVpn configuration`() = runTest {
        // given
        val whitelistedParams = "--tun-mtu 1200 --mtu-test"
        val openVpnConfig = GivenModel.openVpnConfig(additionalParameters = whitelistedParams)
        val config = GivenModel.protocolConfiguration(openVpnClientConfiguration = openVpnConfig)

        // when
        val filteredConfig = usecase(config)

        // then
        assert(whitelistedParams.contentEquals(filteredConfig.getOrNull()?.openVpnClientConfiguration?.additionalParameters))
    }

    @Test
    fun `from a mix of whitelisted and non whitelisted options only the whitelisted ones are kept`() = runTest {
        // given
        val inputParams = "--mtu-test --client-connect"
        val expectedOutput = "--mtu-test"
        val openVpnConfig = GivenModel.openVpnConfig(additionalParameters = inputParams)
        val config = GivenModel.protocolConfiguration(openVpnClientConfiguration = openVpnConfig)

        // when
        val filteredConfig = usecase(config)

        // then
        assert(expectedOutput.contentEquals(filteredConfig.getOrNull()?.openVpnClientConfiguration?.additionalParameters))
    }

    @Test
    fun `leading and trailing whitespace chars are not added as additional parameter`() = runTest {
        // given
        val inputParams = "   --mtu-test   "
        val expectedOutput = "--mtu-test"
        val openVpnConfig = GivenModel.openVpnConfig(additionalParameters = inputParams)
        val config = GivenModel.protocolConfiguration(openVpnClientConfiguration = openVpnConfig)

        // when
        val filteredConfig = usecase(config)

        // then
        assert(expectedOutput.contentEquals(filteredConfig.getOrNull()?.openVpnClientConfiguration?.additionalParameters))
    }

    @Test
    fun `a blank input string is reduced to an empty input string`() = runTest {
        // given
        val inputParams = "   \n   "
        val expectedOutput = ""
        val openVpnConfig = GivenModel.openVpnConfig(additionalParameters = inputParams)
        val config = GivenModel.protocolConfiguration(openVpnClientConfiguration = openVpnConfig)

        // when
        val filteredConfig = usecase(config)

        // then
        assert(expectedOutput.contentEquals(filteredConfig.getOrNull()?.openVpnClientConfiguration?.additionalParameters))
    }
}
