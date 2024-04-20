package com.kape.vpnservicemanager.data.models

import android.app.Notification
import android.app.PendingIntent
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

/**
 * Object representing all the configuration information needed for the vpn protocol operation
 *
 * @param sessionName `String`. Set the name of this session. It will be displayed in
 * system-managed dialogs and notifications.
 * @param configureIntent `PendingIntent`. Set the PendingIntent to an activity for users to
 * configure the VPN connection. If it is not set, the button to configure will not be shown.
 * @param protocolTarget `VPNServiceManagerProtocolTarget`.
 * @param serverIp `String`. Server's ip to connect to.
 * @param mtu `Int`. Maximum transmission unit to be used when connecting.
 * @param port `Int`. Server's port to connect to.
 * @param dnsInformation `VPNServiceDnsInformation`. List of dns to used as part of the connection.
 * @param notificationId `Int`. The identifier for this notification.
 * @param notification `Notification`. The Notification to be displayed as par of the foreground
 * service.
 * @param allowedApplicationPackages `List<String>`. List of application packages whose traffic
 * is ALLOWED to go through the vpn.
 * @param disallowedApplicationPackages `List<String>`. List of application packages whose traffic
 * is DISALLOWED to go through the vpn.
 * @param allowLocalNetworkAccess `Boolean`.
 * @param openVpnClientConfiguration `VPNServiceManagerOpenVpnClientConfiguration`. Object
 * containing the specifics of an OpenVpn configuration. Expected along with other protocols
 * configuration for future fallback purposes.
 * @param wireguardClientConfiguration `VPNServiceManagerWireguardClientConfiguration`. Object
 * containing the specifics of a Wireguard configuration. Expected along with other protocols
 * configuration for future fallback purposes.
 */
public data class VPNServiceManagerConfiguration(
    val sessionName: String,
    val configureIntent: PendingIntent?,
    val protocolTarget: VPNServiceManagerProtocolTarget,
    val mtu: Int,
    val dnsInformation: VPNServiceDnsInformation,
    val notificationId: Int,
    val notification: Notification,
    val allowedApplicationPackages: List<String> = emptyList(),
    val disallowedApplicationPackages: List<String> = emptyList(),
    val allowLocalNetworkAccess: Boolean,
    val openVpnClientConfiguration: VPNServiceManagerOpenVpnClientConfiguration,
    val wireguardClientConfiguration: VPNServiceManagerWireguardClientConfiguration,
)
