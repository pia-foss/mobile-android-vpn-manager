package com.kape.vpnservicemanager.data.externals

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnprotocol.presenters.ServiceConfigurationFileDescriptorProvider
import com.kape.vpnprotocol.presenters.VPNProtocolService
import com.kape.vpnservicemanager.data.models.NetworkDetails
import com.kape.vpnservicemanager.data.models.VPNServiceServer
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation
import com.kape.vpnservicemanager.presenters.VPNServiceManagerProtocolTarget

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

internal interface IProtocol {

    /**
     * @param vpnService `VPNProtocolService`.
     * @param allowedIps `List<NetworkDetails>`.
     * @param serviceConfigurationFileDescriptorProvider `ServiceConfigurationFileDescriptorProvider`.
     *
     * @return `Result<VPNServiceServerPeerInformation>`.
     */
    suspend fun startConnection(
        vpnService: VPNProtocolService,
        allowedIps: List<NetworkDetails>,
        serviceConfigurationFileDescriptorProvider: ServiceConfigurationFileDescriptorProvider,
    ): Result<VPNServiceServerPeerInformation>

    /**
     * @return `Result<Unit>`.
     */
    suspend fun startReconnection(): Result<Unit>

    /**
     * @return `Result<Unit>`.
     */
    suspend fun stopConnection(disconnectReason: DisconnectReason): Result<Unit>

    /**
     * @param protocolTarget `VPNServiceManagerProtocolTarget`.
     *
     * @return `Result<List<String>>`.
     */
    suspend fun getVpnProtocolLogs(
        protocolTarget: VPNServiceManagerProtocolTarget,
    ): Result<List<String>>

    /**
     * @return `Result<VPNServiceServer>`.
     */
    suspend fun getTargetServer(): Result<VPNServiceServer>
}
