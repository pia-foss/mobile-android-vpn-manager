package com.kape.vpnprotocol.presenters

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnprotocol.data.models.VPNProtocolConfiguration
import com.kape.vpnprotocol.data.models.VPNProtocolServer
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

/**
 * Interface defining the API available to the clients.
 */
public interface VPNProtocolAPI {

    /**
     * Starts the vpn connection attempt with given configuration. Callback invoked on main thread.
     *
     * @param vpnService `VPNProtocolService`.
     * @param protocolConfiguration `VPNProtocolConfiguration`.
     * @param serviceConfigurationFileDescriptorProvider `ServiceConfigurationFileDescriptorProvider`.
     * @param callback `VPNProtocolResultCallback<VPNProtocolServerPeerInformation>`.
     */
    fun startConnection(
        vpnService: VPNProtocolService,
        protocolConfiguration: VPNProtocolConfiguration,
        serviceConfigurationFileDescriptorProvider: ServiceConfigurationFileDescriptorProvider,
        callback: VPNProtocolResultCallback<VPNProtocolServerPeerInformation>,
    )

    /**
     * It starts the reconnection process, based on the known state. Callback invoked on main thread.
     *
     * @param callback `VPNProtocolCallback`.
     */
    fun startReconnection(callback: VPNProtocolCallback)

    /**
     * Stops an active vpn connection. Callback invoked on main thread.
     *
     * @param protocolTarget `VPNProtocolTarget`.
     * @param disconnectReason `DisconnectReason`.
     * @param callback `VPNProtocolCallback`.
     */
    fun stopConnection(
        protocolTarget: VPNProtocolTarget,
        disconnectReason: DisconnectReason,
        callback: VPNProtocolCallback,
    )

    /**
     * It gets the known VPN protocol via the callback as a collection of lines, as read by the process.
     * Callback invoked on the CoroutineContext set on the VPNManagerBuilder.
     *
     * @param protocolTarget `VPNProtocolTarget`.
     * @param callback `VPNProtocolResultCallback<List<String>>`.
     */
    fun getVpnProtocolLogs(
        protocolTarget: VPNProtocolTarget,
        callback: VPNProtocolResultCallback<List<String>>,
    )

    /**
     * It gets the target server we are connecting to. A failure will be returned otherwise.
     *
     * @param callback `VPNProtocolResultCallback<VPNProtocolServer>`.
     */
    fun getTargetServer(callback: VPNProtocolResultCallback<VPNProtocolServer>)
}

/**
 * Interface defining the scope of Android's service methods to expose to the protocol module.
 */
public interface VPNProtocolService {

    /**
     * Protect a socket from VPN connections. After protecting, data sent through this socket will
     * go directly to the underlying network, so its traffic will not be forwarded through the VPN.
     * This method is useful if some connections need to be kept outside of VPN.
     * For example, a VPN tunnel should protect itself if its destination is covered by VPN routes.
     * Otherwise its outgoing packets will be sent back to the VPN interface and cause an infinite
     * loop. This method will fail if the application is not prepared or is revoked.
     *
     * @param socket: Int.
     *
     * @return `Result<Boolean>`.
     */
    fun serviceProtect(socket: Int): Result<Boolean>
}

/**
 * Interface defining the service's configuration file descriptor provider.
 * In order to prepare the service's configuration, we need to pass it the address to use as
 * `Service.Builder.addAddress` which comes as part of the `vpnprotocol` flow for all supported
 * protocols.
 */
public interface ServiceConfigurationFileDescriptorProvider {

    /**
     * @param peerIp: String.
     * @param dnsIp: String?.
     * @param mtu: Int?.
     * @param gateway: String,
     *
     * @return `Result<Int>`. Native fd for the Parcel's File Descriptor VPN interface.
     */
    fun establish(peerIp: String, dnsIp: String? = null, mtu: Int? = null, gateway: String): Result<Int>
}

/**
 * Enum representing the supported target protocols to connect to.
 */
public enum class VPNProtocolTarget {
    OPENVPN,
    WIREGUARD,
}

/**
 * Object containing the details of an API failure.
 *
 * @param code `VPNProtocolErrorCode`.
 * @param error `Error`.
 */
public data class VPNProtocolError(
    val code: VPNProtocolErrorCode,
    val error: Error? = null,
) : Throwable()

/**
 * Enum representing the list of possible API response statuses.
 */
public enum class VPNProtocolErrorCode {
    NETWORK_NOT_REACHABLE,
    NETWORK_REQUEST_ERROR,
    PROTOCOL_CONFIGURATION_NOT_READY,
    PROTOCOL_PEER_INFORMATION_NOT_READY,
    STATE_HANDLER_NOT_READY,
    EVENT_HANDLER_NOT_READY,
    CONFIGURATION_HANDLER_NOT_READY,
    PROTECT_FD_HANDLER_NOT_READY,
    TUNNEL_HANDLE_NOT_READY,
    KEY_PAIR_NOT_READY,
    KEY_RESPONSE_NOT_READY,
    PRIVATE_KEY_NOT_READY,
    ADD_KEY_RESPONSE_NOT_READY,
    FILE_DESCRIPTOR_PROVIDER_ERROR,
    PROTOCOL_SERVICE_ERROR,
    DESERIALIZATION_FAILURE,
    CONNECTION_FAILURE,
    NETWORK_INTERFACE_NAME_ERROR,
    NO_VPN_LOGS_FOUND,
    NO_BYTECOUNT_JOB_FOUND,
    NO_KEEPALIVE_JOB_FOUND,
    NO_NETWORK_CALLBACK_FOUND,
    INVALID_BYTECOUNT,
    UNSUPPORTED_CIPHER_ERROR,
}

/**
 * It defines the callback structure for an API method without a response object.
 */
public typealias VPNProtocolCallback = (Result<Unit>) -> Unit

/**
 * It defines the callback structure for an API method requiring an object in its response.
 */
public typealias VPNProtocolResultCallback<T> = (Result<T>) -> Unit
