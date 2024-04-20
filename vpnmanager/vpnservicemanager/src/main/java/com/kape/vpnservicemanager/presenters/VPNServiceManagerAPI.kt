package com.kape.vpnservicemanager.presenters

import com.kape.vpnmanager.api.DisconnectReason
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

/**
 * Interface defining the API available to the clients.
 */
public interface VPNServiceManagerAPI {

    /**
     * Starts the vpn connection attempt with given configuration. Callback invoked on main thread.
     *
     * @param protocolConfiguration `VPNServiceManagerConfiguration`.
     * @param callback `VPNServiceManagerResultCallback<VPNServiceServerPeerInformation`.
     */
    fun startConnection(
        protocolConfiguration: VPNServiceManagerConfiguration,
        callback: VPNServiceManagerResultCallback<VPNServiceServerPeerInformation>,
    )

    /**
     * Stops an active vpn connection. Callback invoked on main thread.
     *
     * @param callback `VPNServiceManagerCallback`.
     */
    fun stopConnection(disconnectReason: DisconnectReason, callback: VPNServiceManagerCallback)

    /**
     * It gets the known VPN protocol via the callback as a collection of lines, as read by the process.
     * Callback invoked on the CoroutineContext set on the VPNManagerBuilder.
     *
     * @param protocolTarget `VPNServiceManagerProtocolTarget`.
     * @param callback `VPNServiceManagerResultCallback<List<String>>`.
     */
    fun getVpnProtocolLogs(
        protocolTarget: VPNServiceManagerProtocolTarget,
        callback: VPNServiceManagerResultCallback<List<String>>,
    )
}

/**
 * Enum representing the supported target protocols to connect to.
 */
public enum class VPNServiceManagerProtocolTarget {
    OPENVPN,
    WIREGUARD,
}

/**
 * Object containing the details of an API failure.
 *
 * @param code `VPNServiceManagerErrorCode`.
 * @param error `Error`.
 */
public data class VPNServiceManagerError(
    val code: VPNServiceManagerErrorCode,
    val error: Error? = null,
) : Throwable()

/**
 * Enum representing the list of possible API response statuses.
 */
public enum class VPNServiceManagerErrorCode {
    SERVICE_NOT_READY,
    KNOWN_SERVICE_PRESENT,
    PROTOCOL_CONFIGURATION_NOT_READY,
    PROTOCOL_PEER_INFORMATION_NOT_READY,
    SERVICE_CONFIGURATION_ERROR,
    SERVICE_CONNECTION_TIMED_OUT,
    PARSING_ADDRESS_ERROR,
    NETWORK_UNREACHABLE,
}

/**
 * It defines the callback structure for an API method without a response object.
 */
public typealias VPNServiceManagerCallback = (Result<Unit>) -> Unit

/**
 * It defines the callback structure for an API method requiring an object in its response.
 */
public typealias VPNServiceManagerResultCallback<T> = (Result<T>) -> Unit
