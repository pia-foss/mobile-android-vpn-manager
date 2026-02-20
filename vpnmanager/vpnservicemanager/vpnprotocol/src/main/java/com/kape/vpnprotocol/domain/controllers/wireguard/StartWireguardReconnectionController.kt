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

package com.kape.vpnprotocol.domain.controllers.wireguard

import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnprotocol.domain.usecases.common.IGetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.IIsNetworkAvailable
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.wireguard.ICreateWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.IDestroyWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.IGenerateWireguardSettings
import com.kape.vpnprotocol.domain.usecases.wireguard.IGetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.IProtectWireguardTunnelSocket
import com.kape.vpnprotocol.domain.usecases.wireguard.ISetWireguardTunnelHandle

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

internal class StartWireguardReconnectionController(
    private val reportConnectivityStatus: IReportConnectivityStatus,
    private val getProtocolConfiguration: IGetProtocolConfiguration,
    private val isNetworkAvailable: IIsNetworkAvailable,
    private val getWireguardTunnelHandle: IGetWireguardTunnelHandle,
    private val destroyWireguardTunnel: IDestroyWireguardTunnel,
    private val generateWireguardSettings: IGenerateWireguardSettings,
    private val createWireguardTunnel: ICreateWireguardTunnel,
    private val setWireguardTunnelHandle: ISetWireguardTunnelHandle,
    private val protectWireguardTunnelSocket: IProtectWireguardTunnelSocket,
) : IStartWireguardReconnectionController {

    // region IStartWireguardReconnectionController
    override suspend fun invoke(): Result<Unit> =
        reportConnectivityStatus(connectivityStatus = VPNManagerConnectionStatus.Reconnecting)
            .mapCatching {
                getProtocolConfiguration().getOrThrow()
            }
            .mapCatching {
                for (server in it.wireguardClientConfiguration.serverList) {
                    val result = isNetworkAvailable(server.ip)
                    if (result.isSuccess) {
                        break
                    }
                    result.getOrThrow()
                }
            }
            .mapCatching {
                getWireguardTunnelHandle().getOrThrow()
            }
            .mapCatching {
                destroyWireguardTunnel(tunnelHandle = it).getOrThrow()
            }
            .mapCatching {
                generateWireguardSettings().getOrThrow()
            }
            .mapCatching {
                createWireguardTunnel(generatedSettings = it).getOrThrow()
            }
            .mapCatching {
                setWireguardTunnelHandle(tunnelHandle = it).getOrThrow()
            }
            .mapCatching {
                protectWireguardTunnelSocket().getOrThrow()
            }
            .mapCatching {
                reportConnectivityStatus(
                    connectivityStatus = VPNManagerConnectionStatus.Connected()
                ).getOrThrow()
            }
    // endregion
}
