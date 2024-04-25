package com.kape.openvpn.domain.usecases

import com.kape.openvpn.data.externals.ICache
import com.kape.openvpn.data.externals.IOpenVpnProcessSocket
import com.kape.vpnmanager.api.data.externals.IJob

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

internal class HandleOpenVpnHoldOutput(
    private val openVpnProcessSocket: IOpenVpnProcessSocket,
    private val cache: ICache,
    private val job: IJob,
) : IHandleOpenVpnHoldOutput {

    private enum class OpenVpnHoldLinesOfInterest(val line: String) {
        WAITING_RELEASE("waiting for hold release:"),
    }

    // region IHandleOpenVpnHoldOutput
    override fun invoke(line: String): Result<Unit> {
        return runCatching {
            when {
                line.contains(OpenVpnHoldLinesOfInterest.WAITING_RELEASE.line) -> {
                    val holdReleaseDelaySeconds = line.trim().split(":").last().toLong()
                    job.delayedJob(delayMillis = holdReleaseDelaySeconds * 1000) {
                        openVpnProcessSocket.write("hold release\n").getOrThrow()
                    }
                    cache.setHoldReleaseJob(job = job)
                }
            }
        }
    }
    // endregion
}
