package com.kape.vpnprotocol.data.externals.wireguard

import com.kape.wireguard.presenters.WireguardAPI

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

internal class Wireguard(
    private val wireguardApi: WireguardAPI,
) : IWireguard {

    // region IWireguard
    override fun turnOn(
        tunnelName: String,
        builderParcelFileDescriptorFd: Int,
        settings: String,
    ): Result<Int> =
        wireguardApi.turnOn(
            tunnelName = tunnelName,
            builderParcelFileDescriptorFd = builderParcelFileDescriptorFd,
            settings = settings
        )

    override fun turnOff(tunnelHandle: Int): Result<Unit> =
        wireguardApi.turnOff(tunnelHandle = tunnelHandle)

    override fun socketV4(tunnelHandle: Int): Result<Int> =
        wireguardApi.socketV4(tunnelHandle = tunnelHandle)

    override fun socketV6(tunnelHandle: Int): Result<Int> =
        wireguardApi.socketV6(tunnelHandle = tunnelHandle)

    override fun configuration(tunnelHandle: Int): Result<String> =
        wireguardApi.configuration(tunnelHandle = tunnelHandle)
    // endregion
}
