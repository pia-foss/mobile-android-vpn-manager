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

package com.kape.vpnprotocol.domain.usecases.wireguard

import com.kape.vpnmanager.api.data.externals.IJob
import com.kape.vpnprotocol.data.externals.common.ICacheProtocol
import com.kape.vpnprotocol.data.externals.wireguard.ICacheWireguard
import com.kape.vpnprotocol.data.externals.wireguard.IWireguard
import com.kape.vpnprotocol.presenters.VPNProtocolError
import com.kape.vpnprotocol.presenters.VPNProtocolErrorCode

internal class StartWireguardByteCountJob(
    private val job: IJob,
    private val wireguard: IWireguard,
    private val cacheProtocol: ICacheProtocol,
    private val cacheWireguard: ICacheWireguard,
) : IStartWireguardByteCountJob {

    companion object {
        private const val BYTECOUNT_JOB_INTERVAL_MS = 1000L
    }

    // region IStartWireguardByteCountJob
    override suspend fun invoke(): Result<Unit> =
        cacheWireguard.setWireguardByteCountJob(job)
            .mapCatching {
                cacheWireguard.getWireguardTunnelHandle().getOrThrow()
            }
            .mapCatching { tunnelHandle ->
                job.repeatableJob(
                    delayMillis = BYTECOUNT_JOB_INTERVAL_MS,
                    action = {
                        jobAction(tunnelHandle = tunnelHandle)
                    }
                ).getOrThrow()
            }
    // endregion

    // region private
    private fun jobAction(tunnelHandle: Int) {
        val configuration = wireguard.configuration(tunnelHandle = tunnelHandle).getOrThrow()
        val (tx, rx) = getByteCountFromConfigurationOutput(output = configuration).getOrThrow()
        cacheProtocol.reportByteCount(tx = tx, rx = rx)
    }

    private fun getByteCountFromConfigurationOutput(output: String): Result<Pair<Long, Long>> {
        val outputList = output.split("\n")
        val txString = outputList.firstOrNull { it.contains("tx_bytes", ignoreCase = true) }
            ?: return Result.failure(VPNProtocolError(code = VPNProtocolErrorCode.INVALID_BYTECOUNT))
        val rxString = outputList.firstOrNull { it.contains("rx_bytes", ignoreCase = true) }
            ?: return Result.failure(VPNProtocolError(code = VPNProtocolErrorCode.INVALID_BYTECOUNT))

        val tx = txString.split("=").last().trim().toLong()
        val rx = rxString.split("=").last().trim().toLong()
        return Result.success(Pair(tx, rx))
    }
    // endregion
}
