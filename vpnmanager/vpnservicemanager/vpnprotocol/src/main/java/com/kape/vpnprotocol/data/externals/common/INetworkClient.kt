package com.kape.vpnprotocol.data.externals.common

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

internal interface INetworkClient {

    /**
     * @param host `String`
     * @param port `Int`
     * @param path `String`
     * @param headers `List<Pair<String, String>>`
     * @param parameters `List<Pair<String, String>>`
     * @param certificate `String`
     * @param commonName `String`
     *
     * @return `Result<String>`.
     */
    suspend fun performGetRequest(
        host: String,
        port: Int,
        path: String,
        headers: List<Pair<String, String>>,
        parameters: List<Pair<String, String>>,
        certificate: String?,
        commonName: String,
    ): Result<String>
}
