package com.kape.vpnmanager.data.models

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
 * Object representing the list of available servers to connect to. All of these servers form part
 * of a pre-selected region by the client.
 *
 * @param servers `List<Server>`.
 */
data class ServerList(
    val servers: List<Server>,
) {
    /**
     * @param ip `String`.
     * @param port `Int`.
     * @param commonOrDistinguishedName `String`.
     * @param transport `TransportProtocol`.
     * @param ciphers `List<ProtocolCipher>`.
     * @param latency `Long?`.
     * @param dnsInformation `DnsInformation`.
     */
    data class Server(
        val ip: String,
        val port: Int,
        val commonOrDistinguishedName: String,
        val transport: TransportProtocol,
        val ciphers: List<ProtocolCipher>,
        val latency: Long? = null,
        val dnsInformation: DnsInformation,
    )
}
