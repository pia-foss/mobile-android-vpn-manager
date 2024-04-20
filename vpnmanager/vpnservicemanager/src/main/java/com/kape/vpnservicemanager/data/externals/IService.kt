package com.kape.vpnservicemanager.data.externals

import com.kape.vpnmanager.api.DisconnectReason
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

internal interface IService {

    /**
     * @param protocol `IProtocol`.
     * @param subnet `ISubnet`.
     * @param cache `ICache`.
     *
     * @return `Result<Unit>`.
     */
    fun bootstrap(
        protocol: IProtocol,
        subnet: ISubnet,
        cache: ICache,
    ): Result<Unit>

    /**
     * @return `Result<VPNServiceServerPeerInformation>`.
     */
    suspend fun startConnection(): Result<VPNServiceServerPeerInformation>

    /**
     * @return `Result<Unit>`.
     */
    suspend fun startReconnection(): Result<Unit>

    /**
     * @return `Result<Unit>`.
     */
    suspend fun stopConnection(disconnectReason: DisconnectReason): Result<Unit>
}
