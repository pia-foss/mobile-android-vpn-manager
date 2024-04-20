package com.kape.vpnmanager.presenters

import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.ServerPeerInformation
import com.kape.vpnmanager.data.models.TransportProtocol

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
public interface VPNManagerAPI {

    /**
     * Starts the vpn connection attempt with given configuration.
     * Callback invoked on the CoroutineContext set on the VPNManagerBuilder.
     *
     * @param clientConfiguration `SetProtocolConfiguration`.
     * @param callback `VPNManagerResultCallback<ServerPeerInformation>`.
     */
    fun startConnection(
        clientConfiguration: ClientConfiguration,
        callback: VPNManagerResultCallback<ServerPeerInformation>,
    )

    /**
     * Stops an active vpn connection.
     * Callback invoked on the CoroutineContext set on the VPNManagerBuilder.
     *
     * @param callback `VPNManagerCallback`.
     */
    fun stopConnection(callback: VPNManagerCallback)

    /**
     * It adds a new listener to the list of connection listeners.
     * Callback invoked on the CoroutineContext set on the VPNManagerBuilder.
     *
     * @param listener `VPNManagerConnectionListener`.
     * @param callback `VPNManagerCallback`.
     */
    fun addConnectionListener(
        listener: VPNManagerConnectionListener,
        callback: VPNManagerCallback,
    )

    /**
     * It removes an existing listener from the list of connection listeners.
     * Callback invoked on the CoroutineContext set on the VPNManagerBuilder.
     *
     * @param listener `VPNManagerConnectionListener`.
     * @param callback `VPNManagerCallback`.
     */
    fun removeConnectionListener(
        listener: VPNManagerConnectionListener,
        callback: VPNManagerCallback,
    )

    /**
     * It gets the known VPN protocol via the callback as a collection of lines, as read by the process.
     * Callback invoked on the CoroutineContext set on the VPNManagerBuilder.
     *
     * @param protocolTarget `VPNManagerProtocolTarget`.
     * @param callback `VPNManagerResultCallback<List<String>>`.
     */
    fun getVpnProtocolLogs(
        protocolTarget: VPNManagerProtocolTarget,
        callback: VPNManagerResultCallback<List<String>>,
    )
}

/**
 * Interface to conform when registering as a connection listener.
 */
public interface VPNManagerConnectionListener {

    /**
     * It reports changes on the vpn connection status.
     *
     * @param status `VPNManagerConnectionStatus`.
     */
    fun handleConnectionStatusChange(status: VPNManagerConnectionStatus)

    /**
     * It reports the successful connection to each individual server passed in the server list
     * for startConnection.
     */
    fun handleServerConnectAttemptSucceeded(
        serverIp: String,
        transportMode: TransportProtocol,
        vpnProtocol: VPNManagerProtocolTarget,
    ) {
    }

    /**
     * It reports the failures to connect to each individual server passed in the server list
     * for startConnection.
     */
    fun handleServerConnectAttemptFailed(
        serverIp: String,
        transportMode: TransportProtocol,
        vpnProtocol: VPNManagerProtocolTarget,
        throwable: Throwable,
    ) {
    }

    /**
     * It reports the results of the MTU Test if the --mtu-test parameter was passed to OpenVPN.
     * It is not invoked for Wireguard connections.
     */
    fun handleMtuTestResult(localToRemote: Int, remoteToLocal: Int) {
    }
}

/**
 * Object containing the details of an API failure.
 *
 * @param code `VPNManagerError`.
 * @param error `Error`.
 */
public data class VPNManagerError(
    val code: VPNManagerErrorCode,
    val error: Error? = null,
) : Throwable()

/**
 * Enum representing the list of possible API response statuses.
 */
public enum class VPNManagerErrorCode {
    PERMISSIONS_NOT_GRANTED,
    FAILED_SERVER_SELECTION,
    UNKNOWN_SERVER_OBJECT,
    UNKNOWN_CLIENT_CONFIGURATION,
    FAILED,
}

/**
 * Enum representing the supported target protocols to connect to.
 */
public enum class VPNManagerProtocolTarget {
    OPENVPN,
    WIREGUARD,
}

/**
 * It defines the callback structure for an API method without a response object.
 */
public typealias VPNManagerCallback = (Result<Unit>) -> Unit

/**
 * It defines the callback structure for an API method requiring an object in its response.
 */
public typealias VPNManagerResultCallback<T> = (Result<T>) -> Unit
