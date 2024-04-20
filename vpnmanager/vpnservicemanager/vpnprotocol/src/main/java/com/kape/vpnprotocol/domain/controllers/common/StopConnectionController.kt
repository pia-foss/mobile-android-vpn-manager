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

package com.kape.vpnprotocol.domain.controllers.common

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnprotocol.domain.controllers.openvpn.IStopOpenVpnConnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.IStopWireguardConnectionController
import com.kape.vpnprotocol.presenters.VPNProtocolTarget

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

internal class StopConnectionController(
    private val stopOpenVpnConnectionController: IStopOpenVpnConnectionController,
    private val stopWireguardConnectionController: IStopWireguardConnectionController,
) : IStopConnectionController {

    // region IStopConnectionController
    override suspend fun invoke(protocolTarget: VPNProtocolTarget, disconnectReason: DisconnectReason): Result<Unit> {
        return when (protocolTarget) {
            VPNProtocolTarget.OPENVPN -> stopOpenVpnConnectionController(disconnectReason)
            VPNProtocolTarget.WIREGUARD -> stopWireguardConnectionController(disconnectReason)
        }
    }
    // endregion
}
