package com.kape.openvpn.presenters

import com.kape.openvpn.data.models.OpenVpnServerPeerInformation

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
public interface OpenVpnAPI {

    /**
     * @param commandLineParams `List<String>`. String representation of the parameters to be used
     * when starting the process. e.g. `--status-version 3 --ping-restart 60`
     * @param openVpnProcessEventHandler `OpenVpnProcessOutputHandler`
     * @param callback `OpenVpnCallback`.
     */
    fun start(
        commandLineParams: List<String>,
        openVpnProcessEventHandler: OpenVpnProcessEventHandler,
        callback: OpenVpnCallback,
    )

    /**
     * @param callback `OpenVpnCallback`.
     */
    fun stop(callback: OpenVpnCallback)
}

/**
 * Interface defining the handler of openvpn's process relevant events.
 */
public interface OpenVpnProcessEventHandler {

    /**
     * @param fd `Int`.
     *
     * @return `Result<Boolean>`
     */
    fun serviceProtect(fd: Int): Result<Boolean>

    /**
     * @param serverPeerInformation `OpenVpnServerPeerInformation`.
     *
     * @return `Result<Int>`
     */
    fun serviceEstablish(serverPeerInformation: OpenVpnServerPeerInformation): Result<Int>

    /**
     * @return `Result<OpenVpnUserCredentials>`
     */
    fun getUserCredentials(): Result<OpenVpnUserCredentials>

    /**
     * @return `Result<Unit>`
     */
    fun processConnected(): Result<Unit>

    /**
     * @param tx `Long`.
     * @param rx `Long`.
     *
     * @return `Result<Unit>`
     */
    fun processByteCountReceived(tx: Long, rx: Long): Result<Unit>
}

/**
 * Object containing the user credentials to be used in the protocol connection.
 *
 * @param username `String`.
 * @param password `String`.
 */
public data class OpenVpnUserCredentials(
    val username: String,
    val password: String,
)

/**
 * Object containing the details of an API failure.
 *
 * @param code `OpenVpnErrorCode`.
 * @param error `Error`.
 */
public data class OpenVpnError(
    val code: OpenVpnErrorCode,
    val error: Error? = null,
) : Throwable()

/**
 * Enum representing the list of possible API response statuses.
 */
public enum class OpenVpnErrorCode {
    INVALID_PID,
    PROCESS_UNKNOWN,
    PROCESS_NOT_RUNNING,
    PROCESS_COULD_NOT_START,
    PROCESS_COULD_NOT_STOP,
    PROCESS_RUNNING_ALREADY,
    HOLD_RELEASE_JOB_UNKNOWN,
    UNKNOWN_LIBRARY_PATH,
    PROCESS_OUTPUT_HANDLER_UNKNOWN,
    SOCKET_CONNECTION_ERROR,
    OPENVPN_PUSH_COMMAND_ERROR,
}

/**
 * It defines the callback structure for an API method without a response object.
 */
public typealias OpenVpnCallback = (Result<Unit>) -> Unit
