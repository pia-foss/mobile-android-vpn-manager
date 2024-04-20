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

package com.kape.vpnservicemanager.data.externals

import com.kape.vpnservicemanager.data.models.NetworkDetails
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class SubnetTest {

    @Test
    fun `using an ip not present in the subnets, returns the original subnets`() = runTest {
        // given
        val subnet: ISubnet = Subnet()
        val ip = "127.0.0.1"
        val subnets = listOf(
            NetworkDetails("0.0.0.0", 8),
            NetworkDetails("1.0.0.0", 8),
            NetworkDetails("2.0.0.0", 8),
            NetworkDetails("3.0.0.0", 8)
        )

        // when
        val result = subnet.excludeIpFromSubnets(subnets = subnets, excludeIp = ip)

        // then
        assert(areNetworkDetailsListsEqual(result, subnets))
    }

    @Test
    fun `using an ip present in the subnets, returns the updated subnets`() = runTest {
        // given
        val subnet: ISubnet = Subnet()
        val ip = "2.1.1.1"
        val subnets = listOf(
            NetworkDetails("0.0.0.0", 8),
            NetworkDetails("1.0.0.0", 8),
            NetworkDetails("2.0.0.0", 8),
            NetworkDetails("3.0.0.0", 8)
        )

        // when
        val result = subnet.excludeIpFromSubnets(subnets = subnets, excludeIp = ip)

        // then
        assert(areNetworkDetailsListsEqual(result, subnets).not())
        assert(result.size == 27)
        assert(result[2].address == "2.0.0.0")
        assert(result[2].prefix == 16)
        assert(result[25].address == "2.128.0.0")
        assert(result[25].prefix == 9)
        assert(result.last().address == "3.0.0.0")
        assert(result.last().prefix == 8)
    }

    // region private
    private fun areNetworkDetailsListsEqual(
        listA: List<NetworkDetails>,
        listB: List<NetworkDetails>,
    ): Boolean =
        listA.zip(listB) { a, b -> a.address == b.address && a.prefix == b.prefix }.all { it }
    // endregion
}
