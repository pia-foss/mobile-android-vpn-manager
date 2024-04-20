package com.kape.myapplication

import android.app.Activity
import android.net.VpnService
import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnmanager.data.models.TransportProtocol
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.kape.vpnmanager.presenters.VPNManagerDebugLoggingDependency
import com.kape.vpnmanager.presenters.VPNManagerPermissionsDependency
import com.kape.vpnmanager.presenters.VPNManagerProtocolByteCountDependency
import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget
import com.kape.vpnmanager.presenters.VPNManagerResultCallback

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

class VPNManagerDependencies(
    private val activity: Activity,
    private val uiLogger: UILogger,
) : VPNManagerDebugLoggingDependency,
    VPNManagerProtocolByteCountDependency,
    VPNManagerPermissionsDependency,
    VPNManagerConnectionListener {

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 1234
    }

    // region VPNManagerDebugLoggingDependency
    override fun debugLog(log: String) { }
    // endregion

    // region VPNManagerProtocolLoggingDependency
    override fun byteCount(tx: Long, rx: Long) {
        uiLogger.log("ByteCount Tx: $tx Rx: $rx")
    }
    // endregion

    // region VPNManagerPermissionsDependency
    override fun requestNecessaryPermissions(callback: VPNManagerResultCallback<Boolean>) {
        val intent = VpnService.prepare(activity)
        activity.startActivityForResult(intent, PERMISSIONS_REQUEST_CODE)
        callback(Result.success(false))
    }
    // endregion

    // region VPNManagerConnectionListener
    override fun handleConnectionStatusChange(status: VPNManagerConnectionStatus) {
        uiLogger.log("HandleConnectionStatusChange received: $status")
    }
    // endregion

    override fun handleMtuTestResult(localToRemote: Int, remoteToLocal: Int) {
        uiLogger.log("MtuTestResult: LocalToRemote: $localToRemote, RemoteToLocal: $remoteToLocal")
    }

    override fun handleServerConnectAttemptSucceeded(
        serverIp: String,
        transportMode: TransportProtocol,
        vpnProtocol: VPNManagerProtocolTarget,
    ) {
        uiLogger.log("ServerConnectAttemptSucceeded: $serverIp, $transportMode, $vpnProtocol")
    }

    override fun handleServerConnectAttemptFailed(
        serverIp: String,
        transportMode: TransportProtocol,
        vpnProtocol: VPNManagerProtocolTarget,
        throwable: Throwable,
    ) {
        uiLogger.log("ServerConnectAttemptFailed: $serverIp, $transportMode, $vpnProtocol, ${throwable.localizedMessage}")
    }
}
