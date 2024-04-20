package com.kape.vpnservicemanager.data.externals

import com.kape.vpnservicemanager.presenters.VPNServiceManagerError
import com.kape.vpnservicemanager.presenters.VPNServiceManagerErrorCode
import java.io.IOException
import java.net.InetAddress

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

internal class Connectivity : IConnectivity {

    companion object {
        private const val PING_TIMEOUT = 3000
    }

    // region IConnectivity
    override suspend fun isNetworkReachable(host: String): Result<Unit> {
        try {
            val isReachable = InetAddress.getByName(host).isReachable(PING_TIMEOUT)
            return if (isReachable) {
                Result.success(Unit)
            } else {
                Result.failure(VPNServiceManagerError(code = VPNServiceManagerErrorCode.NETWORK_UNREACHABLE))
            }
        } catch (exception: IOException) {
            return Result.failure(exception)
        }
    }
    // endregion
}
