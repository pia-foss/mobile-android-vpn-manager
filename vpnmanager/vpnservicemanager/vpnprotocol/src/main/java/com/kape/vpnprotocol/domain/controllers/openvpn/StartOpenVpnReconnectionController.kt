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

package com.kape.vpnprotocol.domain.controllers.openvpn

import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnprotocol.domain.usecases.common.IGetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.IIsNetworkAvailable
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.openvpn.ICreateOpenVpnProcessConnectedDeferrable
import com.kape.vpnprotocol.domain.usecases.openvpn.IStartOpenVpnEventHandler
import com.kape.vpnprotocol.domain.usecases.openvpn.IStartOpenVpnProcess
import com.kape.vpnprotocol.domain.usecases.openvpn.IStopOpenVpnProcess
import com.kape.vpnprotocol.domain.usecases.openvpn.IWaitForOpenVpnProcessConnectedDeferrable

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

internal class StartOpenVpnReconnectionController(
    private val reportConnectivityStatus: IReportConnectivityStatus,
    private val getProtocolConfiguration: IGetProtocolConfiguration,
    private val isNetworkAvailable: IIsNetworkAvailable,
    private val stopOpenVpnProcess: IStopOpenVpnProcess,
    private val createOpenVpnProcessConnectedDeferrable: ICreateOpenVpnProcessConnectedDeferrable,
    private val startOpenVpnEventHandler: IStartOpenVpnEventHandler,
    private val startOpenVpnProcess: IStartOpenVpnProcess,
    private val waitForOpenVpnProcessConnectedDeferrable: IWaitForOpenVpnProcessConnectedDeferrable,
) : IStartOpenVpnReconnectionController {

    // region IStartOpenVpnReconnectionController
    override suspend fun invoke(): Result<Unit> =
        reportConnectivityStatus(connectivityStatus = VPNManagerConnectionStatus.Reconnecting)
            .mapCatching {
                getProtocolConfiguration().getOrThrow()
            }
            .mapCatching {
                for (server in it.openVpnClientConfiguration.serverList) {
                    val result = isNetworkAvailable(server.ip)
                    if (result.isSuccess) {
                        break
                    }
                    result.getOrThrow()
                }
            }
            .mapCatching {
                stopOpenVpnProcess().getOrThrow()
            }
            .mapCatching {
                createOpenVpnProcessConnectedDeferrable().getOrThrow()
            }
            .mapCatching {
                startOpenVpnEventHandler().getOrThrow()
            }
            .mapCatching {
                startOpenVpnProcess(openVpnProcessEventHandler = it).getOrThrow()
            }
            .mapCatching {
                waitForOpenVpnProcessConnectedDeferrable().getOrThrow()
            }
            .mapCatching {
                reportConnectivityStatus(
                    connectivityStatus = VPNManagerConnectionStatus.Connected()
                ).getOrThrow()
            }
    // endregion
}
