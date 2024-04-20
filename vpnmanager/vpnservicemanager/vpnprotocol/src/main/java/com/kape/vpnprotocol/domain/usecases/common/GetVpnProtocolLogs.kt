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

package com.kape.vpnprotocol.domain.usecases.common

import com.kape.vpnprotocol.data.externals.common.ILogsProcessor
import com.kape.vpnprotocol.data.externals.common.IProcess
import com.kape.vpnprotocol.presenters.VPNProtocolTarget

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

internal class GetVpnProtocolLogs(
    private val process: IProcess,
    private val logsProcessor: ILogsProcessor,
) : IGetVpnProtocolLogs {

    companion object {
        private const val WIREGUARD_PREFIX = "WireGuard"
        private const val OPENVPN_PREFIX = "OpenVPN"
        private const val COMMAND = "logcat -b all -t 2000 -d -v threadtime *:V"
    }

    // region IGetVpnProtocolLogs
    override suspend fun invoke(protocolTarget: VPNProtocolTarget): Result<List<String>> {
        val targetPrefix = when (protocolTarget) {
            VPNProtocolTarget.OPENVPN -> OPENVPN_PREFIX
            VPNProtocolTarget.WIREGUARD -> WIREGUARD_PREFIX
        }
        val processStreams = process.executeCommand(command = COMMAND).getOrThrow()
        return logsProcessor.processLogs(tag = targetPrefix, streams = processStreams)
    }
    // endregion
}
