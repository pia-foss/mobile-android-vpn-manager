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

import com.kape.vpnprotocol.data.models.VPNProtocolConfiguration
import com.kape.vpnprotocol.data.models.VPNProtocolServerPeerInformation
import com.kape.vpnprotocol.domain.controllers.openvpn.IStartOpenVpnConnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.IStartWireguardConnectionController
import com.kape.vpnprotocol.presenters.ServiceConfigurationFileDescriptorProvider
import com.kape.vpnprotocol.presenters.VPNProtocolService
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

internal class StartConnectionController(
    private val startOpenVpnConnectionController: IStartOpenVpnConnectionController,
    private val startWireguardConnectionController: IStartWireguardConnectionController,
) : IStartConnectionController {

    // region IStartConnectionController
    override suspend fun invoke(
        vpnService: VPNProtocolService,
        protocolConfiguration: VPNProtocolConfiguration,
        serviceConfigurationFileDescriptorProvider: ServiceConfigurationFileDescriptorProvider,
    ): Result<VPNProtocolServerPeerInformation> {
        return when (protocolConfiguration.protocolTarget) {
            VPNProtocolTarget.OPENVPN ->
                startOpenVpnConnectionController(
                    vpnService = vpnService,
                    protocolConfiguration = protocolConfiguration,
                    serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProvider
                )
            VPNProtocolTarget.WIREGUARD ->
                startWireguardConnectionController(
                    vpnService = vpnService,
                    protocolConfiguration = protocolConfiguration,
                    serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProvider
                )
        }
    }
    // endregion
}
