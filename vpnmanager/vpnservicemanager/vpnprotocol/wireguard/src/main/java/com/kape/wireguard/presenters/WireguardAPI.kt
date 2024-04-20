package com.kape.wireguard.presenters

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
public interface WireguardAPI {

    /**
     * @param tunnelName `String`.
     * @param builderParcelFileDescriptorFd `Int`. Android's vpn service builder parcel descriptor
     * as return by the `establish` API. @see VpnService.Builder.establish.
     * @param settings `String`. A multiline string representation of the client settings in
     * the format `key=value`.
     *
     * @return `Result<Int>`. Tunnel handle.
     */
    fun turnOn(
        tunnelName: String,
        builderParcelFileDescriptorFd: Int,
        settings: String,
    ): Result<Int>

    /**
     * @param tunnelHandle `Int`.
     *
     * @return `Result<Unit>`.
     */
    fun turnOff(tunnelHandle: Int): Result<Unit>

    /**
     * @param tunnelHandle `Int`.
     *
     * @return `Int`. Socket.
     */
    fun socketV4(tunnelHandle: Int): Result<Int>

    /**
     * @param tunnelHandle `Int`.
     *
     * @return `Result<Int>`. Socket.
     */
    fun socketV6(tunnelHandle: Int): Result<Int>

    /**
     * @param tunnelHandle `Int`.
     *
     * @return `Result<String>`. Configuration.
     */
    fun configuration(tunnelHandle: Int): Result<String>

    /**
     * @return `Result<String>`. Version.
     */
    fun version(): Result<String>
}

/**
 * Object containing the details of an API failure.
 *
 * @param code `WireguardErrorCode`.
 * @param error `Error`.
 */
public data class WireguardError(
    val code: WireguardErrorCode,
    val error: Error? = null,
) : Throwable()

/**
 * Enum representing the list of possible API response statuses.
 */
public enum class WireguardErrorCode {
    ACTIVATION_ERROR,
    SOCKET_ERROR,
    CONFIGURATION_ERROR,
    VERSION_ERROR,
}
