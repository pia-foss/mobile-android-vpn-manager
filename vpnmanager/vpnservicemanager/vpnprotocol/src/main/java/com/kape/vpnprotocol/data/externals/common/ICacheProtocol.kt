package com.kape.vpnprotocol.data.externals.common

import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnprotocol.data.models.VPNProtocolConfiguration
import com.kape.vpnprotocol.data.models.VPNProtocolServerPeerInformation

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

internal interface ICacheProtocol {

    /**
     * @return `Result<String>`.
     */
    fun getManagementPath(): Result<String>

    /**
     * @param connectivityStatus `VPNManagerConnectionStatus`.
     *
     * @return `Result<Unit>`.
     */
    fun reportConnectivityStatus(connectivityStatus: VPNManagerConnectionStatus): Result<Unit>

    /**
     * @param tx `Long`.
     * @param rx `Long`.
     *
     * @return `Result<Unit>`.
     */
    fun reportByteCount(tx: Long, rx: Long): Result<Unit>

    /**
     * @param protocolConfiguration `VPNProtocolConfiguration`.
     *
     * @return `Result<Unit>`.
     */
    fun setProtocolConfiguration(protocolConfiguration: VPNProtocolConfiguration): Result<Unit>

    /**
     * @return `Result<VPNProtocolConfiguration>`.
     */
    fun getProtocolConfiguration(): Result<VPNProtocolConfiguration>

    /**
     * @return `Result<Unit>`.
     */
    fun clearProtocolConfiguration(): Result<Unit>

    /**
     * @param serverPeerInformation `VPNProtocolServerPeerInformation`.
     *
     * @return `Result<Unit>`.
     */
    fun setProtocolServerPeerInformation(
        serverPeerInformation: VPNProtocolServerPeerInformation,
    ): Result<Unit>

    /**
     * @return `Result<VPNProtocolServerPeerInformation>`.
     */
    fun getProtocolServerPeerInformation(): Result<VPNProtocolServerPeerInformation>

    /**
     * @return `Result<Unit>`.
     */
    fun clearProtocolServerPeerInformation(): Result<Unit>
}
