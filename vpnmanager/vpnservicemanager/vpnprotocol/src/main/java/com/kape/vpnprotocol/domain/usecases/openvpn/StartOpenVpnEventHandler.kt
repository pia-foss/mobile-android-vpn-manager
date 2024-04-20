package com.kape.vpnprotocol.domain.usecases.openvpn

import com.kape.openvpn.data.models.OpenVpnServerPeerInformation
import com.kape.openvpn.presenters.OpenVpnProcessEventHandler
import com.kape.openvpn.presenters.OpenVpnUserCredentials
import com.kape.vpnprotocol.data.externals.common.ICacheProtocol
import com.kape.vpnprotocol.data.externals.common.ICacheService
import com.kape.vpnprotocol.data.externals.openvpn.ICacheOpenVpn
import com.kape.vpnprotocol.presenters.ServiceConfigurationFileDescriptorProvider
import com.kape.vpnprotocol.presenters.VPNProtocolService

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

internal class StartOpenVpnEventHandler(
    private val cacheProtocol: ICacheProtocol,
    private val cacheOpenVpn: ICacheOpenVpn,
    private val cacheService: ICacheService,
) : IStartOpenVpnEventHandler, OpenVpnProcessEventHandler {

    private lateinit var vpnService: VPNProtocolService
    private lateinit var serviceConfigurationFileDescriptorProvider: ServiceConfigurationFileDescriptorProvider

    // region IStartOpenVpnEventHandler
    override suspend fun invoke(): Result<OpenVpnProcessEventHandler> {
        this.vpnService = cacheService.getVpnProtocolService().getOrElse {
            return Result.failure(it)
        }
        this.serviceConfigurationFileDescriptorProvider =
            cacheService.getServiceFileDescriptorProvider().getOrElse {
                return Result.failure(it)
            }

        return Result.success(this)
    }
    // endregion

    // region OpenVpnProcessEventHandler
    override fun serviceProtect(fd: Int): Result<Boolean> =
        vpnService.serviceProtect(fd)

    override fun serviceEstablish(serverPeerInformation: OpenVpnServerPeerInformation): Result<Int> {
        cacheOpenVpn.setOpenVpnServerPeerInformation(
            openVpnServerPeerInformation = serverPeerInformation
        ).getOrElse {
            return Result.failure(it)
        }
        return serviceConfigurationFileDescriptorProvider.establish(
            peerIp = serverPeerInformation.address
        )
    }

    override fun getUserCredentials(): Result<OpenVpnUserCredentials> {
        val protocolConfiguration = cacheProtocol.getProtocolConfiguration().getOrElse {
            return Result.failure(it)
        }
        return Result.success(
            OpenVpnUserCredentials(
                username = protocolConfiguration.openVpnClientConfiguration.username,
                password = protocolConfiguration.openVpnClientConfiguration.password
            )
        )
    }

    override fun processConnected(): Result<Unit> {
        val deferred = cacheOpenVpn.getOpenVpnProcessConnectedDeferrable().getOrElse {
            return Result.failure(it)
        }

        deferred.complete(Unit)
        return Result.success(Unit)
    }

    override fun processByteCountReceived(tx: Long, rx: Long): Result<Unit> =
        cacheProtocol.reportByteCount(tx = tx, rx = rx)
    // endregion
}
