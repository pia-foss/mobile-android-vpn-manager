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
import com.kape.vpnprotocol.data.models.mapToApiModel
import com.kape.vpnprotocol.domain.usecases.common.IGetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.common.ISetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.wireguard.ICreateWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.IDestroyWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.IGenerateWireguardSettings
import com.kape.vpnprotocol.domain.usecases.wireguard.IGetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.IPerformWireguardAddKeyRequest
import com.kape.vpnprotocol.domain.usecases.wireguard.IProtectWireguardTunnelSocket
import com.kape.vpnprotocol.domain.usecases.wireguard.ISetWireguardAddKeyResponse
import com.kape.vpnprotocol.domain.usecases.wireguard.ISetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.IStartWireguardByteCountJob
import com.kape.vpnprotocol.domain.usecases.wireguard.IStopWireguardByteCountJob
import com.kape.vpnprotocol.presenters.mapToApiModel

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
    private val setProtocolConfiguration: ISetProtocolConfiguration,
    private val getWireguardTunnelHandle: IGetWireguardTunnelHandle,
    private val destroyWireguardTunnel: IDestroyWireguardTunnel,
    private val stopWireguardByteCountJob: IStopWireguardByteCountJob,
    private val performWireguardAddKeyRequest: IPerformWireguardAddKeyRequest,
    private val setWireguardAddKeyResponse: ISetWireguardAddKeyResponse,
    private val generateWireguardSettings: IGenerateWireguardSettings,
    private val createWireguardTunnel: ICreateWireguardTunnel,
    private val setWireguardTunnelHandle: ISetWireguardTunnelHandle,
    private val protectWireguardTunnelSocket: IProtectWireguardTunnelSocket,
    private val startWireguardByteCountJob: IStartWireguardByteCountJob,
) : IStartWireguardReconnectionController {

    // endregion
    // region IStartWireguardReconnectionController
    override suspend fun invoke(): Result<Unit> =
        reportConnectivityStatus(connectivityStatus = VPNManagerConnectionStatus.Reconnecting)
            .mapCatching { getProtocolConfiguration().getOrThrow() }
            .mapCatching { config ->
                // Cyclically advance to the next server without connectivity checks.
                // Connectivity checks use unprotected sockets that route through the broken VPN tunnel
                // and therefore fail for all servers when the tunnel is down.
                val servers = config.wireguardClientConfiguration.servers.ifEmpty {
                    listOf(config.wireguardClientConfiguration.server)
                }
                val currentIndex = servers.indexOfFirst { it.ip == config.wireguardClientConfiguration.server.ip }
                val nextIndex = if (currentIndex < 0) 0 else (currentIndex + 1) % servers.size
                val nextServer = servers[nextIndex]
                if (nextServer.ip != config.wireguardClientConfiguration.server.ip) {
                    val updatedConfig = config.copy(
                        wireguardClientConfiguration = config.wireguardClientConfiguration.copy(
                            server = nextServer
                        )
                    )
                    setProtocolConfiguration(protocolConfiguration = updatedConfig).getOrThrow()
                }
            }
            .mapCatching {
                // Stop the byte count job before destroying the tunnel. Tolerate missing job
                // (e.g. second reconnection attempt where job was already cleared).
                stopWireguardByteCountJob().getOrNull()
            }
            .mapCatching {
                // If a live tunnel exists destroy it so traffic goes directly and the add-key
                // request can reach the server. If no handle is cached (already destroyed in a
                // previous failed reconnection attempt), skip straight to the request.
                getWireguardTunnelHandle().getOrNull()?.let { tunnelHandle ->
                    destroyWireguardTunnel(tunnelHandle = tunnelHandle).getOrThrow()
                }
            }
            // With the tunnel destroyed traffic goes directly, so the add-key HTTP request can reach
            // the server. This also refreshes the server's WireGuard public key for the new config.
            .mapCatching { performWireguardAddKeyRequest().getOrThrow() }
            .mapCatching { addKeyResponse ->
                setWireguardAddKeyResponse(wireguardAddKeyResponse = addKeyResponse).getOrThrow()
            }
            .mapCatching { generateWireguardSettings().getOrThrow() }
            .mapCatching { settings -> createWireguardTunnel(generatedSettings = settings).getOrThrow() }
            .mapCatching { newHandle -> setWireguardTunnelHandle(tunnelHandle = newHandle).getOrThrow() }
            .mapCatching { protectWireguardTunnelSocket().getOrThrow() }
            .mapCatching { startWireguardByteCountJob().getOrThrow() }
            .mapCatching { getProtocolConfiguration().getOrThrow() }
            .mapCatching {
                reportConnectivityStatus(
                    connectivityStatus = VPNManagerConnectionStatus.Connected(
                        serverIp = it.wireguardClientConfiguration.server.ip,
                        transportMode = it.wireguardClientConfiguration.server.transport.mapToApiModel(),
                        vpnProtocol = it.protocolTarget.mapToApiModel()
                    )
                ).getOrThrow()
            }
}
