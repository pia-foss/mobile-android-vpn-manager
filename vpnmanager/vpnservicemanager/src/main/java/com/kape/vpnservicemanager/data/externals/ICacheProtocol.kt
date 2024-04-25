package com.kape.vpnservicemanager.data.externals

import com.kape.vpnservicemanager.data.models.VPNServiceManagerConfiguration
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation

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
     * @param protocolConfiguration `VPNServiceManagerConfiguration`.
     *
     * @return `Result<Unit>`.
     */
    fun setProtocolConfiguration(protocolConfiguration: VPNServiceManagerConfiguration): Result<Unit>

    /**
     * @return `Result<VPNServiceManagerConfiguration>`.
     */
    fun getProtocolConfiguration(): Result<VPNServiceManagerConfiguration>

    /**
     * @return `Result<Unit>`.
     */
    fun clearProtocolConfiguration(): Result<Unit>

    /**
     * @param serverPeerInformation `VPNServiceServerPeerInformation`.
     *
     * @return `Result<Unit>`.
     */
    fun setServerPeerInformation(serverPeerInformation: VPNServiceServerPeerInformation): Result<Unit>

    /**
     * @return `Result<VPNServiceServerPeerInformation>`.
     */
    fun getServerPeerInformation(): Result<VPNServiceServerPeerInformation>

    /**
     * @return `Result<Unit>`.
     */
    fun clearServerPeerInformation(): Result<Unit>

    /**
     * @param gateway `String`.
     *
     * @return `Result<Unit>`.
     */
    fun setGateway(gateway: String): Result<Unit>

    /**
     * @return `Result<String>`.
     */
    fun getGateway(): Result<String>

    /**
     * @return `Result<Unit>`.
     */
    fun clearGateway(): Result<Unit>
}
