package com.kape.vpnservicemanager.data.externals

import android.net.VpnService
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

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

internal class Connectivity(
    private val cacheService: ICacheService,
) : IConnectivity {

    companion object {
        private const val PING_TIMEOUT = 3000
        private const val PING_PORT = 443
    }

    // region IConnectivity
    override suspend fun isNetworkReachable(host: String): Result<Unit> {
        // First, attempt a normal (tunneled) connection. This is the happy path —
        // no socket protection needed, no VPN bypass, no risk of leaking traffic.
        val tunnelResult = attemptConnection(host, protect = false, service = null)
        if (tunnelResult.isSuccess) return tunnelResult

        // The tunneled attempt failed. This likely means the VPN tunnel itself is
        // broken (e.g. server restart). Fall back to a protected socket that
        // bypasses the tunnel so we can confirm whether the underlying network is
        // reachable at all.
        val service = cacheService.getService().getOrNull()
            ?: return Result.failure(IOException("VPN service unavailable — cannot protect socket"))

        return attemptConnection(host, protect = true, service = service)
    }

    private fun attemptConnection(
        host: String,
        protect: Boolean,
        service: VpnService?,
    ): Result<Unit> {
        val socket = Socket()
        return try {
            socket.tcpNoDelay = true
            // bind() forces the OS to create the underlying socket file descriptor before
            // we call protect(), ensuring the protection actually takes effect. Without this,
            // the fd may not exist yet (lazy creation) and protect() would be a no-op.
            socket.bind(InetSocketAddress(0))

            if (protect) {
                // Protect the socket so the ping bypasses the VPN tunnel and reaches
                // the server over the real network interface.
                if (service == null || !service.protect(socket)) {
                    return Result.failure(IOException("Failed to protect socket — traffic would leak"))
                }
            }

            socket.connect(InetSocketAddress(host, PING_PORT), PING_TIMEOUT)
            Result.success(Unit)
        } catch (exception: IOException) {
            Result.failure(exception)
        } finally {
            // Always close the socket, even if connect() or protect() throws.
            // Closing an already-closed socket is a no-op, so this is safe.
            runCatching { socket.close() }
        }
    }
    // endregion
}
