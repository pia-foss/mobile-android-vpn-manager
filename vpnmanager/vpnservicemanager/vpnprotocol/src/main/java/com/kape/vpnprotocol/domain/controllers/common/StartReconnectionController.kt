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

import com.kape.vpnprotocol.domain.controllers.openvpn.IStartOpenVpnReconnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.IStartWireguardReconnectionController
import com.kape.vpnprotocol.domain.usecases.common.IGetTargetProtocol
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

internal class StartReconnectionController(
    private val getTargetProtocol: IGetTargetProtocol,
    private val startOpenVpnReconnectionController: IStartOpenVpnReconnectionController,
    private val startWireguardReconnectionController: IStartWireguardReconnectionController,
) : IStartReconnectionController {

    // region IStartReconnectionController
    override suspend fun invoke(): Result<Unit> {
        val protocolTarget = getTargetProtocol().getOrElse {
            return Result.failure(it)
        }

        return when (protocolTarget) {
            VPNProtocolTarget.OPENVPN -> startOpenVpnReconnectionController()
            VPNProtocolTarget.WIREGUARD -> startWireguardReconnectionController()
        }
    }
    // endregion
}
