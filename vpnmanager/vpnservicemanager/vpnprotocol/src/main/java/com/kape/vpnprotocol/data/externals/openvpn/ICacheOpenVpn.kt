package com.kape.vpnprotocol.data.externals.openvpn

import com.kape.openvpn.data.models.OpenVpnServerPeerInformation
import kotlinx.coroutines.CompletableDeferred

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

internal interface ICacheOpenVpn {

    /**
     * @param generatedSettings `List<String>`.
     *
     * @return `Result<Unit>`
     */
    fun setOpenVpnGeneratedSettings(generatedSettings: List<String>): Result<Unit>

    /**
     * @return `Result<List<String>>`
     */
    fun getOpenVpnGeneratedSettings(): Result<List<String>>

    /**
     * @return `Result<Unit>`
     */
    fun clearOpenVpnGeneratedSettings(): Result<Unit>

    /**
     * @return `Result<Unit>`
     */
    fun createOpenVpnProcessConnectedDeferrable(): Result<Unit>

    /**
     * @return `Result<CompletableDeferred<Unit>>`
     */
    fun getOpenVpnProcessConnectedDeferrable(): Result<CompletableDeferred<Unit>>

    /**
     * @return `Result<Unit>`
     */
    fun clearOpenVpnProcessConnectedDeferrable(): Result<Unit>

    /**
     * @param openVpnServerPeerInformation `OpenVpnServerPeerInformation`.
     *
     * @return `Result<Unit>`.
     */
    fun setOpenVpnServerPeerInformation(
        openVpnServerPeerInformation: OpenVpnServerPeerInformation,
    ): Result<Unit>

    /**
     * @return `Result<OpenVpnServerPeerInformation>`.
     */
    fun getOpenVpnServerPeerInformation(): Result<OpenVpnServerPeerInformation>

    /**
     * @return `Result<Unit>`
     */
    fun clearOpenVpnServerPeerInformation(): Result<Unit>
}
