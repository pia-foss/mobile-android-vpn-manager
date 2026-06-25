package com.kape.openvpn.domain.usecases

import com.kape.openvpn.presenters.OpenVpnProcessEventHandler
import com.kape.openvpn.presenters.OpenVpnState

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

internal class HandleOpenVpnStateOutput : IHandleOpenVpnStateOutput {

    // region IHandleOpenVpnStateOutput
    override fun invoke(
        line: String,
        openVpnProcessEventHandler: OpenVpnProcessEventHandler,
    ): Result<Unit> {
        return runCatching {
            // State lines arrive as: >state:timestamp,state_name,description,...
            val stateName = line.substringAfter("state:").split(",").getOrNull(1)
            val state = when (stateName) {
                "initial" -> OpenVpnState.Initial
                "connecting" -> OpenVpnState.Connecting
                "assign_ip" -> OpenVpnState.AssignIp
                "add_routes" -> OpenVpnState.AddRoutes
                "connected" -> OpenVpnState.Connected
                "reconnecting" -> OpenVpnState.Reconnecting
                "exiting" -> OpenVpnState.Exiting
                "wait" -> OpenVpnState.Wait
                "auth_pending" -> OpenVpnState.AuthPending
                "auth" -> OpenVpnState.Auth
                "get_config" -> OpenVpnState.GetConfig
                "resolve" -> OpenVpnState.Resolve
                "tcp_connect" -> OpenVpnState.TcpConnect
                else -> null
            }
            if (state != null) {
                openVpnProcessEventHandler.stateUpdated(state)
            }
        }
    }
    // endregion
}
