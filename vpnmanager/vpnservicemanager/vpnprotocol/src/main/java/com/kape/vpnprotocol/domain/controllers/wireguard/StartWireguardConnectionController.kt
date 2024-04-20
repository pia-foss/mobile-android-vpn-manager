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

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnprotocol.data.models.VPNProtocolConfiguration
import com.kape.vpnprotocol.data.models.VPNProtocolServerPeerInformation
import com.kape.vpnprotocol.data.utils.getOrFail
import com.kape.vpnprotocol.domain.usecases.common.IClearCache
import com.kape.vpnprotocol.domain.usecases.common.IGetServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.common.IIsNetworkAvailable
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.common.ISetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.ISetServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.common.ISetServiceFileDescriptor
import com.kape.vpnprotocol.domain.usecases.common.ISetVpnService
import com.kape.vpnprotocol.domain.usecases.wireguard.ICreateWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.IGenerateWireguardKeyPair
import com.kape.vpnprotocol.domain.usecases.wireguard.IGenerateWireguardServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.wireguard.IGenerateWireguardSettings
import com.kape.vpnprotocol.domain.usecases.wireguard.IPerformWireguardAddKeyRequest
import com.kape.vpnprotocol.domain.usecases.wireguard.IProtectWireguardTunnelSocket
import com.kape.vpnprotocol.domain.usecases.wireguard.ISetWireguardAddKeyResponse
import com.kape.vpnprotocol.domain.usecases.wireguard.ISetWireguardKeyPair
import com.kape.vpnprotocol.domain.usecases.wireguard.ISetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.IStartWireguardByteCountJob
import com.kape.vpnprotocol.presenters.ServiceConfigurationFileDescriptorProvider
import com.kape.vpnprotocol.presenters.VPNProtocolService

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

internal class StartWireguardConnectionController(
    private val reportConnectivityStatus: IReportConnectivityStatus,
    private val isNetworkAvailable: IIsNetworkAvailable,
    private val setVpnService: ISetVpnService,
    private val setProtocolConfiguration: ISetProtocolConfiguration,
    private val setServiceFileDescriptor: ISetServiceFileDescriptor,
    private val generateWireguardKeyPair: IGenerateWireguardKeyPair,
    private val setWireguardKeyPair: ISetWireguardKeyPair,
    private val performWireguardAddKeyRequest: IPerformWireguardAddKeyRequest,
    private val setWireguardAddKeyResponse: ISetWireguardAddKeyResponse,
    private val generateWireguardSettings: IGenerateWireguardSettings,
    private val createWireguardTunnel: ICreateWireguardTunnel,
    private val setWireguardTunnelHandle: ISetWireguardTunnelHandle,
    private val protectWireguardTunnelSocket: IProtectWireguardTunnelSocket,
    private val generateWireguardServerPeerInformation: IGenerateWireguardServerPeerInformation,
    private val startWireguardByteCountJob: IStartWireguardByteCountJob,
    private val setServerPeerInformation: ISetServerPeerInformation,
    private val getServerPeerInformation: IGetServerPeerInformation,
    private val clearCache: IClearCache,
) : IStartWireguardConnectionController {

    // region IStartWireguardConnectionController
    override suspend fun invoke(
        vpnService: VPNProtocolService,
        protocolConfiguration: VPNProtocolConfiguration,
        serviceConfigurationFileDescriptorProvider: ServiceConfigurationFileDescriptorProvider,
    ): Result<VPNProtocolServerPeerInformation> {
        return reportConnectivityStatus(connectivityStatus = VPNManagerConnectionStatus.Connecting)
            .mapCatching {
                isNetworkAvailable(
                    host = protocolConfiguration.wireguardClientConfiguration.server.ip
                ).getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.NETWORK_ERROR)
                }
            }
            .mapCatching {
                setVpnService(vpnProtocolService = vpnService).getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                setProtocolConfiguration(
                    protocolConfiguration = protocolConfiguration
                ).getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                setServiceFileDescriptor(
                    serviceFileDescriptorProvider = serviceConfigurationFileDescriptorProvider
                ).getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                generateWireguardKeyPair().getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                setWireguardKeyPair(wireguardKeyPair = it).getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                performWireguardAddKeyRequest().getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.SERVER_ERROR)
                }
            }
            .mapCatching {
                setWireguardAddKeyResponse(wireguardAddKeyResponse = it).getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                generateWireguardSettings().getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                createWireguardTunnel(generatedSettings = it).getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.SERVER_ERROR)
                }
            }
            .mapCatching {
                setWireguardTunnelHandle(tunnelHandle = it).getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                protectWireguardTunnelSocket().getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                generateWireguardServerPeerInformation().getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.SERVER_ERROR)
                }
            }
            .mapCatching {
                setServerPeerInformation(serverPeerInformation = it).getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.SERVER_ERROR)
                }
            }
            .mapCatching {
                startWireguardByteCountJob().getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                reportConnectivityStatus(
                    connectivityStatus = VPNManagerConnectionStatus.Connected()
                ).getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                getServerPeerInformation().getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.SERVER_ERROR)
                }
            }
    }
    // endregion

    private suspend fun handleFailure(throwable: Throwable, disconnectReason: DisconnectReason) {
        reportConnectivityStatus(connectivityStatus = VPNManagerConnectionStatus.Disconnected(disconnectReason))
        clearCache()
        throw throwable
    }
}
