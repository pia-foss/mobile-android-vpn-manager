package com.kape.openvpn.domain.usecases

import com.kape.openvpn.data.models.OpenVpnServerPeerInformation
import com.kape.openvpn.presenters.OpenVpnError
import com.kape.openvpn.presenters.OpenVpnErrorCode
import java.lang.Error

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

internal class HandleOpenVpnPushOutput : IHandleOpenVpnPushOutput {

    private enum class OpenVpnPushLinesOfInterest(val line: String) {
        PUSH_CONTROL_MESSAGE("received control message"),
    }

    // region IHandleOpenVpnPushOutput
    override fun invoke(line: String): Result<OpenVpnServerPeerInformation> {
        when {
            line.contains(OpenVpnPushLinesOfInterest.PUSH_CONTROL_MESSAGE.line) -> {
                val gatewayAddress = line.substringAfter("route-gateway ").substringBefore(",")
                val ifconfigAddress = line.substringAfter("ifconfig ").substringBefore(" ")
                if (gatewayAddress.isEmpty() || ifconfigAddress.isEmpty()) {
                    return Result.failure(
                        OpenVpnError(
                            code = OpenVpnErrorCode.OPENVPN_PUSH_COMMAND_ERROR,
                            error = Error("Invalid message")
                        )
                    )
                }
                return Result.success(
                    OpenVpnServerPeerInformation(
                        address = ifconfigAddress,
                        gateway = gatewayAddress
                    )
                )
            }
            else ->
                return Result.failure(
                    OpenVpnError(
                        code = OpenVpnErrorCode.OPENVPN_PUSH_COMMAND_ERROR,
                        error = Error("Unknown command")
                    )
                )
        }
    }
    // endregion
}
