package com.kape.vpnmanager.testutils.mocks

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnmanager.testutils.GivenModel
import com.kape.vpnservicemanager.data.models.VPNServiceManagerConfiguration
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation
import com.kape.vpnservicemanager.presenters.VPNServiceManagerAPI
import com.kape.vpnservicemanager.presenters.VPNServiceManagerCallback
import com.kape.vpnservicemanager.presenters.VPNServiceManagerProtocolTarget
import com.kape.vpnservicemanager.presenters.VPNServiceManagerResultCallback

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

internal class VPNServiceManagerApiMock(
    private val serverPeerInformation: VPNServiceServerPeerInformation = GivenModel.serviceServerPeerInformation(),
) : VPNServiceManagerAPI {

    override fun startConnection(
        protocolConfiguration: VPNServiceManagerConfiguration,
        callback: VPNServiceManagerResultCallback<VPNServiceServerPeerInformation>,
    ) {
        callback(Result.success(serverPeerInformation))
    }

    override fun stopConnection(disconnectReason: DisconnectReason, callback: VPNServiceManagerCallback) {
        callback(Result.success(Unit))
    }

    override fun getVpnProtocolLogs(
        protocolTarget: VPNServiceManagerProtocolTarget,
        callback: VPNServiceManagerResultCallback<List<String>>,
    ) {
        callback(Result.success(emptyList()))
    }
}
