package com.kape.vpnprotocol.data.externals.wireguard

import com.kape.vpnmanager.api.data.externals.IJob

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

internal interface ICacheWireguard {

    /**
     * @param tunnelHandle `Int`.
     *
     * @return `Result<Unit>`
     */
    fun setWireguardTunnelHandle(tunnelHandle: Int): Result<Unit>

    /**
     * @return `Result<Int>`
     */
    fun getWireguardTunnelHandle(): Result<Int>

    /**
     * @return `Result<Unit>`
     */
    fun clearWireguardTunnelHandle(): Result<Unit>

    /**
     * @param job `IJob`.
     *
     * @return `Result<Unit>`
     */
    fun setWireguardByteCountJob(job: IJob): Result<Unit>

    /**
     * @return `Result<IJob>`
     */
    fun getWireguardByteCountJob(): Result<IJob>

    /**
     * @return `Result<Unit>`
     */
    fun clearWireguardByteCountJob(): Result<Unit>
}
