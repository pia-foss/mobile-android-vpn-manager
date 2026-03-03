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
import com.kape.vpnmanager.api.data.model.VpnProtocol
import com.kape.vpnprotocol.data.models.mapToApiModel
import com.kape.vpnprotocol.domain.usecases.common.IGetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.common.ISetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.openvpn.ICreateOpenVpnCertificateFile
import com.kape.vpnprotocol.domain.usecases.openvpn.ICreateOpenVpnProcessConnectedDeferrable
import com.kape.vpnprotocol.domain.usecases.openvpn.IGenerateOpenVpnSettings
import com.kape.vpnprotocol.domain.usecases.openvpn.ISetGeneratedOpenVpnSettings
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
    private val setProtocolConfiguration: ISetProtocolConfiguration,
    private val stopOpenVpnProcess: IStopOpenVpnProcess,
    private val createOpenVpnCertificateFile: ICreateOpenVpnCertificateFile,
    private val generateOpenVpnSettings: IGenerateOpenVpnSettings,
    private val setGeneratedOpenVpnSettings: ISetGeneratedOpenVpnSettings,
    private val createOpenVpnProcessConnectedDeferrable: ICreateOpenVpnProcessConnectedDeferrable,
    private val startOpenVpnEventHandler: IStartOpenVpnEventHandler,
    private val startOpenVpnProcess: IStartOpenVpnProcess,
    private val waitForOpenVpnProcessConnectedDeferrable: IWaitForOpenVpnProcessConnectedDeferrable,
) : IStartOpenVpnReconnectionController {

    // region IStartOpenVpnReconnectionController
    override suspend fun invoke(): Result<Unit> =
        reportConnectivityStatus(connectivityStatus = VPNManagerConnectionStatus.Reconnecting)
            .mapCatching { getProtocolConfiguration().getOrThrow() }
            .mapCatching { config ->
                val servers = config.openVpnClientConfiguration.servers.ifEmpty {
                    listOf(config.openVpnClientConfiguration.server)
                }
                val currentIndex = servers.indexOfFirst {
                    it.ip == config.openVpnClientConfiguration.server.ip
                }
                val startIndex = if (currentIndex < 0) 0 else (currentIndex + 1) % servers.size

                for (offset in servers.indices) {
                    val server = servers[(startIndex + offset) % servers.size]
                    val connected = setProtocolConfiguration(
                        protocolConfiguration = config.copy(
                            openVpnClientConfiguration = config.openVpnClientConfiguration.copy(server = server)
                        )
                    )
                        .mapCatching {
                            stopOpenVpnProcess()
                            createOpenVpnCertificateFile().getOrThrow()
                        }
                        .mapCatching { certFilePath ->
                            generateOpenVpnSettings(certificateFilePath = certFilePath).getOrThrow()
                        }
                        .mapCatching { settings ->
                            setGeneratedOpenVpnSettings(generatedOpenVpnSettings = settings).getOrThrow()
                        }
                        .mapCatching { createOpenVpnProcessConnectedDeferrable().getOrThrow() }
                        .mapCatching { startOpenVpnEventHandler().getOrThrow() }
                        .mapCatching { handler ->
                            startOpenVpnProcess(openVpnProcessEventHandler = handler).getOrThrow()
                        }
                        .mapCatching { waitForOpenVpnProcessConnectedDeferrable().getOrThrow() }

                    if (connected.isSuccess) {
                        reportConnectivityStatus(
                            connectivityStatus = VPNManagerConnectionStatus.Connected(
                                serverIp = server.ip,
                                transportMode = server.transport.mapToApiModel(),
                                vpnProtocol = VpnProtocol.OPENVPN
                            )
                        )
                        return@mapCatching
                    }
                }
                throw Exception("OpenVPN reconnection failed: no server could be reached")
            }
    // endregion
}
