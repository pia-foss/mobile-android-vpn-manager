package com.kape.openvpn.domain.usecases

import android.util.Log
import com.kape.openvpn.data.externals.ICache
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

internal class StartOpenVpnOutputHandler(
    private val cache: ICache,
    private val handleOpenVpnManagementOutput: IHandleOpenVpnManagementOutput,
    private val handleOpenVpnPasswordOutput: IHandleOpenVpnPasswordOutput,
    private val handleOpenVpnNeedOkOutput: IHandleOpenVpnNeedOkOutput,
    private val handleOpenVpnHoldOutput: IHandleOpenVpnHoldOutput,
    private val handleOpenVpnPushOutput: IHandleOpenVpnPushOutput,
    private val handleOpenVpnStateOutput: IHandleOpenVpnStateOutput,
    private val handleOpenVpnByteCountOutput: IHandleOpenVpnByteCountOutput,
    private val handleOpenVpnMtuTestResultOutput: IHandleOpenVpnMtuTestResultOutput,
) : IStartOpenVpnOutputHandler, IOpenVpnProcessOutputHandler {

    private lateinit var openVpnProcessEventHandler: OpenVpnProcessEventHandler
    private var serverPeerInformation: OpenVpnServerPeerInformation? = null

    companion object {
        private const val OPENVPN_TAG = "OpenVPN/Process"
    }

    private enum class OpenVpnProcessCommands(val command: String) {
        MANAGEMENT_OUTPUT("management:"),
        PASSWORD_OUTPUT("password:"),
        NEED_OK_OUTPUT("need-ok:"),
        PUSH_OUTPUT("push:"),
        HOLD_OUTPUT("hold:"),
        STATE_OUTPUT("state:"),
        BYTECOUNT_OUTPUT("bytecount:"),
        MTU_TEST_RESULT_OUTPUT("empirical mtu test completed"),
    }

    // region IStartOpenVpnOutputHandler
    override suspend fun invoke(openVpnProcessEventHandler: OpenVpnProcessEventHandler): Result<Unit> {
        this.openVpnProcessEventHandler = openVpnProcessEventHandler
        return cache.setProcessOutputHandler(this)
    }
    // endregion

    // region IOpenVpnProcessOutputHandler
    override fun output(line: String) {
        Log.d(OPENVPN_TAG, line)
        val sanitizedString = line.lowercase()
        when {
            sanitizedString.contains(OpenVpnProcessCommands.MANAGEMENT_OUTPUT.command) ->
                handleOpenVpnManagementOutput(
                    line = sanitizedString,
                    openVpnProcessOutputHandler = this
                )
            sanitizedString.contains(OpenVpnProcessCommands.PASSWORD_OUTPUT.command) ->
                handleOpenVpnPasswordOutput(
                    line = sanitizedString,
                    openVpnProcessEventHandler = openVpnProcessEventHandler
                )
            sanitizedString.contains(OpenVpnProcessCommands.NEED_OK_OUTPUT.command) ->
                handleOpenVpnNeedOkOutput(
                    line = sanitizedString,
                    serverPeerInformation = serverPeerInformation,
                    openVpnProcessEventHandler = openVpnProcessEventHandler
                )
            sanitizedString.contains(OpenVpnProcessCommands.PUSH_OUTPUT.command) ->
                serverPeerInformation = handleOpenVpnPushOutput(
                    line = sanitizedString
                ).getOrNull()
            sanitizedString.contains(OpenVpnProcessCommands.HOLD_OUTPUT.command) ->
                handleOpenVpnHoldOutput(
                    line = sanitizedString
                )
            sanitizedString.contains(OpenVpnProcessCommands.STATE_OUTPUT.command) ->
                handleOpenVpnStateOutput(
                    line = sanitizedString,
                    openVpnProcessEventHandler = openVpnProcessEventHandler
                )
            sanitizedString.contains(OpenVpnProcessCommands.BYTECOUNT_OUTPUT.command) ->
                handleOpenVpnByteCountOutput(
                    line = sanitizedString,
                    openVpnProcessEventHandler = openVpnProcessEventHandler
                )
            sanitizedString.contains(OpenVpnProcessCommands.MTU_TEST_RESULT_OUTPUT.command) ->
                handleOpenVpnMtuTestResultOutput(
                    line = sanitizedString
                )
        }
    }
    // endregion
}
