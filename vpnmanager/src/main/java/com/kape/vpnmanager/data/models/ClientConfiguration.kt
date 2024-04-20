package com.kape.vpnmanager.data.models

import android.app.Notification
import android.app.PendingIntent
import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget

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
 * Object representing the client side configuration to be used on a vpn connection.
 *
 * @param sessionName `String`. Set the name of this session. It will be displayed in
 * system-managed dialogs and notifications.
 * @param configureIntent `PendingIntent`. Set the PendingIntent to an activity for users to
 * configure the VPN connection. If it is not set, the button to configure will not be shown.
 * @param protocolTarget `VPNManagerProtocolTarget`.
 * @param mtu `Int`. Maximum transmission unit to be used when connecting.
 * @param notificationId `Int`. The identifier for this notification.
 * @param notification `Notification`. The Notification to be displayed as par of the foreground
 * service.
 * @param allowedApplicationPackages `List<String>`. List of application packages whose traffic
 * is ALLOWED to go through the vpn.
 * @param disallowedApplicationPackages `List<String>`. List of application packages whose traffic
 * is DISALLOWED to go through the vpn.
 * @param allowLocalNetworkAccess `Boolean`.
 * @param serverList `ServerList`. List of servers to be used when trying to connect.
 * It looks for the one with the lowest latency and tries to connect to it. Upon failure it
 * falls back ot the others on the list.
 * @param openVpnClientConfiguration `OpenVpnClientConfiguration`. Object containing the
 * specifics of an OpenVpn configuration. Expected along with other protocols configuration for
 * future fallback purposes.
 * @param wireguardClientConfiguration `WireguardClientConfiguration`. Object containing the
 * specifics of a Wireguard configuration. Expected along with other protocols configuration for
 * future fallback purposes.
 */
data class ClientConfiguration(
    val sessionName: String,
    val configureIntent: PendingIntent? = null,
    val protocolTarget: VPNManagerProtocolTarget,
    val mtu: Int,
    val notificationId: Int,
    val notification: Notification,
    val allowedApplicationPackages: List<String> = emptyList(),
    val disallowedApplicationPackages: List<String> = emptyList(),
    val allowLocalNetworkAccess: Boolean,
    val serverList: ServerList,
    val openVpnClientConfiguration: OpenVpnClientConfiguration,
    val wireguardClientConfiguration: WireguardClientConfiguration,
)
