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

internal class Subnet : ISubnet {

    // region ISubnet
    override fun excludeIpFromSubnets(
        subnets: List<NetworkDetails>,
        excludeIp: String,
    ): List<NetworkDetails> {
        val results = mutableListOf<NetworkDetails>()
        for (subnet in subnets) {
            val updatedSubnets = excludeIpFromSubnet(
                subnet = "${subnet.address}/${subnet.prefix}",
                excludeIp = excludeIp
            )
            if (updatedSubnets.isEmpty()) {
                continue
            }
            for (updatedSubnet in updatedSubnets.split(',')) {
                val (address, prefix) = updatedSubnet.split('/')
                results.add(NetworkDetails(address, prefix.toInt()))
            }
        }
        return results.toList()
    }
    // endregion

    // region private
    private fun excludeIpFromSubnet(subnet: String, excludeIp: String): String {
        val (baseIp, maskSize) = subnet.split("/")
        val base = IpAddress(baseIp)
        val excluded = IpAddress(excludeIp)
        val subnets = mutableListOf<String>()
        carveSubnets(base, maskSize.toInt(), excluded, subnets)
        return subnets.joinToString(",")
    }

    private fun carveSubnets(base: IpAddress, mask: Int, excluded: IpAddress, result: MutableList<String>) {
        val currentSubnet = IpSubnet(base, mask)

        if (!currentSubnet.contains(excluded)) {
            result.add(currentSubnet.toString())
            return
        }

        // Edge case: If we reach a /32 mask containing the excluded IP, don't add it.
        if (mask == 32) {
            return
        }

        val step = 1 shl (32 - mask)
        carveSubnets(base, mask + 1, excluded, result)
        carveSubnets(base + step / 2, mask + 1, excluded, result)
    }

    private data class IpAddress(val intValue: Int) : Comparable<IpAddress> {
        constructor(ip: String) : this(
            ip.split(".").map { it.toInt() }.fold(0) { acc, part ->
                (acc shl 8) or part
            }
        )

        operator fun plus(value: Int): IpAddress = IpAddress(intValue + value)
        override fun compareTo(other: IpAddress): Int = intValue.compareTo(other.intValue)
        override fun toString(): String = "${(intValue ushr 24) and 0xFF}.${(intValue ushr 16) and 0xFF}.${(intValue ushr 8) and 0xFF}.${intValue and 0xFF}"
    }

    private data class IpSubnet(val base: IpAddress, val mask: Int) {
        fun contains(address: IpAddress): Boolean {
            var reverseMaskBits = (1 shl mask) - 1
            // When shifting to the left by 32 positions, we lose all information about the number 1
            if (mask == 32) {
                reverseMaskBits = -1
            }
            val maskBits = reverseMaskBits shl (32 - mask)
            return (base.intValue and maskBits) == (address.intValue and maskBits)
        }

        override fun toString(): String = "$base/$mask"
    }
    // endregion
}
