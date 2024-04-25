/*
 *
 *  *  Copyright (c) "2023" Private Internet Access, Inc.
 *  *
 *  *  This file is part of the Private Internet Access Android Client.
 *  *
 *  *  The Private Internet Access Android Client is free software: you can redistribute it and/or
 *  *  modify it under the terms of the GNU General Public License as published by the Free
 *  *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  *  The Private Internet Access Android Client is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  *  details.
 *  *
 *  *  You should have received a copy of the GNU General Public License along with the Private
 *  *  Internet Access Android Client.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.kape.vpnservicemanager.data.externals

import com.kape.vpnservicemanager.data.models.VPNServiceManagerConfiguration
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation
import com.kape.vpnservicemanager.presenters.VPNServiceManagerError
import com.kape.vpnservicemanager.presenters.VPNServiceManagerErrorCode

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

internal class Cache : ICache {

    private var service: Service? = null
    private var serviceBound = false
    private var protocolConfiguration: VPNServiceManagerConfiguration? = null
    private var serverPeerInformation: VPNServiceServerPeerInformation? = null
    private var gateway: String? = null

    // region ICache
    override fun clear(): Result<Unit> {
        return clearProtocolConfiguration()
            .mapCatching { clearService().getOrThrow() }
            .mapCatching { clearServiceBound().getOrThrow() }
            .mapCatching { clearServerPeerInformation().getOrThrow() }
            .mapCatching { clearGateway() }
    }
    // endregion

    // region ICacheService
    override fun setService(service: Service): Result<Unit> {
        this.service = service
        return Result.success(Unit)
    }

    override fun getService(): Result<Service> =
        service?.let {
            Result.success(it)
        } ?: Result.failure(
            VPNServiceManagerError(
                code = VPNServiceManagerErrorCode.SERVICE_NOT_READY
            )
        )

    override fun clearService(): Result<Unit> {
        service = null
        return Result.success(Unit)
    }

    override fun setServiceBound(): Result<Unit> {
        serviceBound = true
        return Result.success(Unit)
    }

    override fun isServiceBound(): Boolean =
        serviceBound

    override fun clearServiceBound(): Result<Unit> {
        serviceBound = false
        return Result.success(Unit)
    }
    // endregion

    // region ICacheProtocol
    override fun setProtocolConfiguration(
        protocolConfiguration: VPNServiceManagerConfiguration,
    ): Result<Unit> {
        this.protocolConfiguration = protocolConfiguration
        return Result.success(Unit)
    }

    override fun getProtocolConfiguration(): Result<VPNServiceManagerConfiguration> =
        protocolConfiguration?.let {
            Result.success(it)
        } ?: Result.failure(
            VPNServiceManagerError(
                code = VPNServiceManagerErrorCode.PROTOCOL_CONFIGURATION_NOT_READY
            )
        )

    override fun clearProtocolConfiguration(): Result<Unit> {
        protocolConfiguration = null
        return Result.success(Unit)
    }

    override fun setServerPeerInformation(
        serverPeerInformation: VPNServiceServerPeerInformation,
    ): Result<Unit> {
        this.serverPeerInformation = serverPeerInformation
        return Result.success(Unit)
    }

    override fun getServerPeerInformation(): Result<VPNServiceServerPeerInformation> =
        serverPeerInformation?.let {
            Result.success(it)
        } ?: Result.failure(
            VPNServiceManagerError(
                code = VPNServiceManagerErrorCode.PROTOCOL_PEER_INFORMATION_NOT_READY
            )
        )

    override fun clearServerPeerInformation(): Result<Unit> {
        serverPeerInformation = null
        return Result.success(Unit)
    }

    override fun setGateway(gateway: String): Result<Unit> {
        this.gateway = gateway
        return Result.success(Unit)
    }

    override fun getGateway(): Result<String> {
        return gateway?.let {
            Result.success(it)
        } ?: Result.failure(
            VPNServiceManagerError(
                code = VPNServiceManagerErrorCode.PROTOCOL_PEER_INFORMATION_NOT_READY
            )
        )
    }

    override fun clearGateway(): Result<Unit> {
        this.gateway = null
        return Result.success(Unit)
    }
    // endregion
}
