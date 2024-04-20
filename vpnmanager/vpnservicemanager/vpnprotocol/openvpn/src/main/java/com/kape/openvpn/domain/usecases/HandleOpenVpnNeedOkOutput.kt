package com.kape.openvpn.domain.usecases

import android.os.Build
import com.kape.openvpn.data.externals.IOpenVpnProcessSocket
import com.kape.openvpn.data.models.OpenVpnServerPeerInformation
import com.kape.openvpn.presenters.OpenVpnProcessEventHandler

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

internal class HandleOpenVpnNeedOkOutput(
    private val openVpnProcessSocket: IOpenVpnProcessSocket,
) : IHandleOpenVpnNeedOkOutput {

    private enum class OpenVpnNeedOkLinesOfInterest(val line: String) {
        PROTECT_FD("need 'protectfd' confirmation"),
        IFCONFIG("need 'ifconfig' confirmation"),
        IFCONFIG6("need 'ifconfig6' confirmation"),
        ROUTE("need 'route' confirmation"),
        ROUTE6("need 'route6' confirmation"),
        DNSSERVER("need 'dnsserver' confirmation"),
        PERSIST_TUN_ACTION("need 'persist_tun_action' confirmation"),
        OPENTUN("need 'opentun' confirmation"),
    }

    // region IHandleOpenVpnNeedOkOutput
    override fun invoke(
        line: String,
        serverPeerInformation: OpenVpnServerPeerInformation?,
        openVpnProcessEventHandler: OpenVpnProcessEventHandler,
    ): Result<Unit> {
        return runCatching {
            when {
                line.contains(OpenVpnNeedOkLinesOfInterest.PROTECT_FD.line) -> {
                    val fd = openVpnProcessSocket.getSocketFd().getOrThrow()
                    val protected = openVpnProcessEventHandler.serviceProtect(fd = fd).getOrThrow()
                    val response = if (protected) "ok" else "cancel"
                    openVpnProcessSocket.write("needok 'PROTECTFD' $response\n").getOrThrow()
                }
                line.contains(OpenVpnNeedOkLinesOfInterest.IFCONFIG.line) -> {
                    openVpnProcessSocket.write("needok 'IFCONFIG' ok\n").getOrThrow()
                }
                line.contains(OpenVpnNeedOkLinesOfInterest.IFCONFIG6.line) -> {
                    openVpnProcessSocket.write("needok 'IFCONFIG6' ok\n").getOrThrow()
                }
                line.contains(OpenVpnNeedOkLinesOfInterest.ROUTE.line) -> {
                    openVpnProcessSocket.write("needok 'ROUTE' ok\n").getOrThrow()
                }
                line.contains(OpenVpnNeedOkLinesOfInterest.ROUTE6.line) -> {
                    openVpnProcessSocket.write("needok 'ROUTE6' ok\n").getOrThrow()
                }
                line.contains(OpenVpnNeedOkLinesOfInterest.DNSSERVER.line) -> {
                    openVpnProcessSocket.write("needok 'DNSSERVER' ok\n").getOrThrow()
                }
                line.contains(OpenVpnNeedOkLinesOfInterest.PERSIST_TUN_ACTION.line) -> {
                    if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                        openVpnProcessSocket.write("needok 'PERSIST_TUN_ACTION' OPEN_AFTER_CLOSE\n").getOrThrow()
                    } else {
                        openVpnProcessSocket.write("needok 'PERSIST_TUN_ACTION' OPEN_BEFORE_CLOSE\n").getOrThrow()
                    }
                }
                line.contains(OpenVpnNeedOkLinesOfInterest.OPENTUN.line) -> {
                    serverPeerInformation?.let {
                        val serviceFd = openVpnProcessEventHandler.serviceEstablish(
                            serverPeerInformation = it
                        ).getOrThrow()
                        openVpnProcessSocket.setFileDescriptorsForSend(fd = serviceFd)
                        openVpnProcessSocket.write("needok 'OPENTUN' ok\n").getOrThrow()
                    } ?: openVpnProcessSocket.write("needok 'OPENTUN' cancel\n").getOrThrow()
                }
            }
        }
    }
    // endregion
}
