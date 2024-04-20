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
import com.kape.vpnprotocol.domain.usecases.openvpn.ICreateOpenVpnCertificateFile
import com.kape.vpnprotocol.domain.usecases.openvpn.ICreateOpenVpnProcessConnectedDeferrable
import com.kape.vpnprotocol.domain.usecases.openvpn.IFilterAdditionalOpenVpnParams
import com.kape.vpnprotocol.domain.usecases.openvpn.IGenerateOpenVpnServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.openvpn.IGenerateOpenVpnSettings
import com.kape.vpnprotocol.domain.usecases.openvpn.ISetGeneratedOpenVpnSettings
import com.kape.vpnprotocol.domain.usecases.openvpn.IStartOpenVpnEventHandler
import com.kape.vpnprotocol.domain.usecases.openvpn.IStartOpenVpnProcess
import com.kape.vpnprotocol.domain.usecases.openvpn.IWaitForOpenVpnProcessConnectedDeferrable
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

internal class StartOpenVpnConnectionController(
    private val reportConnectivityStatus: IReportConnectivityStatus,
    private val isNetworkAvailable: IIsNetworkAvailable,
    private val setVpnService: ISetVpnService,
    private val filterAdditionalOpenVpnParams: IFilterAdditionalOpenVpnParams,
    private val setProtocolConfiguration: ISetProtocolConfiguration,
    private val setServiceFileDescriptor: ISetServiceFileDescriptor,
    private val createOpenVpnCertificateFile: ICreateOpenVpnCertificateFile,
    private val generateOpenVpnSettings: IGenerateOpenVpnSettings,
    private val setGeneratedOpenVpnSettings: ISetGeneratedOpenVpnSettings,
    private val createOpenVpnProcessConnectedDeferrable: ICreateOpenVpnProcessConnectedDeferrable,
    private val startOpenVpnEventHandler: IStartOpenVpnEventHandler,
    private val startOpenVpnProcess: IStartOpenVpnProcess,
    private val waitForOpenVpnProcessConnectedDeferrable: IWaitForOpenVpnProcessConnectedDeferrable,
    private val generateOpenVpnServerPeerInformation: IGenerateOpenVpnServerPeerInformation,
    private val setServerPeerInformation: ISetServerPeerInformation,
    private val getServerPeerInformation: IGetServerPeerInformation,
    private val clearCache: IClearCache,
) : IStartOpenVpnConnectionController {

    // region IStartOpenVpnConnectionController
    override suspend fun invoke(
        vpnService: VPNProtocolService,
        protocolConfiguration: VPNProtocolConfiguration,
        serviceConfigurationFileDescriptorProvider: ServiceConfigurationFileDescriptorProvider,
    ): Result<VPNProtocolServerPeerInformation> {
        return reportConnectivityStatus(connectivityStatus = VPNManagerConnectionStatus.Connecting)
            .mapCatching {
                isNetworkAvailable(
                    host = protocolConfiguration.openVpnClientConfiguration.server.ip
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
                filterAdditionalOpenVpnParams(
                    config = protocolConfiguration
                ).getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                setProtocolConfiguration(
                    protocolConfiguration = it
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
                createOpenVpnCertificateFile().getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                generateOpenVpnSettings(certificateFilePath = it).getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                setGeneratedOpenVpnSettings(generatedOpenVpnSettings = it).getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                createOpenVpnProcessConnectedDeferrable().getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                startOpenVpnEventHandler().getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                startOpenVpnProcess(openVpnProcessEventHandler = it).getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                waitForOpenVpnProcessConnectedDeferrable().getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                generateOpenVpnServerPeerInformation().getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.SERVER_ERROR)
                }
            }
            .mapCatching {
                setServerPeerInformation(
                    serverPeerInformation = it
                ).getOrFail { throwable ->
                    handleFailure(throwable, DisconnectReason.SERVER_ERROR)
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
