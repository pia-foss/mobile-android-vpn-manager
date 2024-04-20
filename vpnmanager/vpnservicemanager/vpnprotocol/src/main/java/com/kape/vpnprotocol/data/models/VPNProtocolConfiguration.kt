package com.kape.vpnprotocol.data.models

import com.kape.vpnprotocol.presenters.VPNProtocolTarget

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

/**
 * Object representing all the configuration information needed for the vpn protocol operation
 *
 * @param sessionName `String`.
 * @param protocolTarget `VPNProtocolTarget`.
 * @param mtu `Int`. Maximum transmission unit to be used when connecting.
 * @param allowedIps `List<String>`.
 * @param openVpnClientConfiguration `VPNProtocolOpenVpnConfiguration`. Object
 * containing the specifics of an OpenVpn configuration. Expected along with other protocols
 * configuration for future fallback purposes.
 * @param wireguardClientConfiguration `VPNProtocolWireguardConfiguration`. Object
 * containing the specifics of a Wireguard configuration. Expected along with other protocols
 * configuration for future fallback purposes.
 */
public data class VPNProtocolConfiguration(
    val sessionName: String,
    val protocolTarget: VPNProtocolTarget,
    val mtu: Int,
    val allowedIps: List<String>,
    val openVpnClientConfiguration: VPNProtocolOpenVpnConfiguration,
    val wireguardClientConfiguration: VPNProtocolWireguardConfiguration,
)
