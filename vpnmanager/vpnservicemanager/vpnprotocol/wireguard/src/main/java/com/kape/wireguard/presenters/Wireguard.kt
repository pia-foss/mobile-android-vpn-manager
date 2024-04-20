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

internal class Wireguard : WireguardAPI {

    private external fun wgGetConfig(handle: Int): String?
    private external fun wgGetSocketV4(handle: Int): Int
    private external fun wgGetSocketV6(handle: Int): Int
    private external fun wgTurnOff(handle: Int)
    private external fun wgTurnOn(ifName: String, tunFd: Int, settings: String): Int
    private external fun wgVersion(): String?

    companion object {
        private const val WG_LIBRARY_NAME = "wg-go"
    }

    init {
        System.loadLibrary(WG_LIBRARY_NAME)
    }

    // region WireguardAPI
    override fun turnOn(
        tunnelName: String,
        builderParcelFileDescriptorFd: Int,
        settings: String,
    ): Result<Int> {
        val tunnelHandle = wgTurnOn(tunnelName, builderParcelFileDescriptorFd, settings)
        return if (tunnelHandle >= 0) {
            Result.success(tunnelHandle)
        } else {
            Result.failure(WireguardError(WireguardErrorCode.ACTIVATION_ERROR))
        }
    }

    override fun turnOff(tunnelHandle: Int): Result<Unit> {
        wgTurnOff(tunnelHandle)
        return Result.success(Unit)
    }

    override fun socketV4(tunnelHandle: Int): Result<Int> {
        val socket = wgGetSocketV4(tunnelHandle)
        return if (socket >= 0) {
            Result.success(socket)
        } else {
            Result.failure(WireguardError(WireguardErrorCode.SOCKET_ERROR))
        }
    }

    override fun socketV6(tunnelHandle: Int): Result<Int> {
        val socket = wgGetSocketV6(tunnelHandle)
        return if (socket >= 0) {
            Result.success(socket)
        } else {
            Result.failure(WireguardError(WireguardErrorCode.SOCKET_ERROR))
        }
    }

    override fun configuration(tunnelHandle: Int): Result<String> {
        return wgGetConfig(tunnelHandle)?.let {
            Result.success(it)
        } ?: Result.failure(WireguardError(WireguardErrorCode.CONFIGURATION_ERROR))
    }

    override fun version(): Result<String> {
        return wgVersion()?.let {
            Result.success(it)
        } ?: Result.failure(WireguardError(WireguardErrorCode.VERSION_ERROR))
    }
    // endregion
}
