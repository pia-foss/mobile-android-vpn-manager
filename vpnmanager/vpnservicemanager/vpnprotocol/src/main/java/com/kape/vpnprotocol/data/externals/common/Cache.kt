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

package com.kape.vpnprotocol.data.externals.common

import android.content.Context
import com.kape.openvpn.data.models.OpenVpnServerPeerInformation
import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import com.kape.vpnmanager.api.data.externals.IJob
import com.kape.vpnprotocol.data.externals.openvpn.ICacheOpenVpn
import com.kape.vpnprotocol.data.externals.wireguard.ICacheWireguard
import com.kape.vpnprotocol.data.externals.wireguard.ICacheWireguardKeys
import com.kape.vpnprotocol.data.externals.wireguard.IWireguardKeyPair
import com.kape.vpnprotocol.data.models.VPNProtocolConfiguration
import com.kape.vpnprotocol.data.models.VPNProtocolServerPeerInformation
import com.kape.vpnprotocol.data.models.WireguardAddKeyResponse
import com.kape.vpnprotocol.presenters.ServiceConfigurationFileDescriptorProvider
import com.kape.vpnprotocol.presenters.VPNProtocolByteCountDependency
import com.kape.vpnprotocol.presenters.VPNProtocolConnectivityStatusChangeCallback
import com.kape.vpnprotocol.presenters.VPNProtocolError
import com.kape.vpnprotocol.presenters.VPNProtocolErrorCode
import com.kape.vpnprotocol.presenters.VPNProtocolService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

internal class Cache(
    private val context: Context,
    private val coroutineContext: ICoroutineContext,
    private val protocolByteCountDependency: VPNProtocolByteCountDependency,
    private val connectivityStatusChangeCallback: VPNProtocolConnectivityStatusChangeCallback,
) : ICache, ICacheWireguard, ICacheOpenVpn, ICacheProtocol, ICacheWireguardKeys, ICacheService {

    private var wireguardTunnelHandle: Int? = null
    private var wireguardByteCountJob: IJob? = null
    private var wireguardKeyPair: IWireguardKeyPair? = null
    private var wireguardAddKeyResponse: WireguardAddKeyResponse? = null
    private var openVpnGeneratedSettings: List<String>? = null
    private var openVpnServerPeerInformation: OpenVpnServerPeerInformation? = null
    private var openVpnProcessConnectedCompletableDeferred: CompletableDeferred<Unit>? = null
    private var vpnProtocolService: VPNProtocolService? = null
    private var protocolConfiguration: VPNProtocolConfiguration? = null
    private var protocolServerPeerInformation: VPNProtocolServerPeerInformation? = null
    private var serviceFileDescriptorProvider: ServiceConfigurationFileDescriptorProvider? = null

    // region ICache
    override fun clear(): Result<Unit> {
        return clearWireguardTunnelHandle()
            .mapCatching { clearWireguardByteCountJob().getOrThrow() }
            .mapCatching { clearKeyPair().getOrThrow() }
            .mapCatching { clearAddKeyResponse().getOrThrow() }
            .mapCatching { clearOpenVpnGeneratedSettings().getOrThrow() }
            .mapCatching { clearOpenVpnServerPeerInformation().getOrThrow() }
            .mapCatching { clearOpenVpnProcessConnectedDeferrable().getOrThrow() }
            .mapCatching { clearVpnProtocolService().getOrThrow() }
            .mapCatching { clearProtocolConfiguration().getOrThrow() }
            .mapCatching { clearProtocolServerPeerInformation().getOrThrow() }
            .mapCatching { clearServiceFileDescriptorProvider().getOrThrow() }
    }
    // endregion

    // region ICacheWireguard
    override fun setWireguardTunnelHandle(tunnelHandle: Int): Result<Unit> {
        this.wireguardTunnelHandle = tunnelHandle
        return Result.success(Unit)
    }

    override fun getWireguardTunnelHandle(): Result<Int> =
        wireguardTunnelHandle?.let {
            Result.success(it)
        } ?: Result.failure(
            VPNProtocolError(
                code = VPNProtocolErrorCode.TUNNEL_HANDLE_NOT_READY
            )
        )

    override fun clearWireguardTunnelHandle(): Result<Unit> {
        wireguardTunnelHandle = null
        return Result.success(Unit)
    }

    override fun setWireguardByteCountJob(job: IJob): Result<Unit> {
        this.wireguardByteCountJob = job
        return Result.success(Unit)
    }

    override fun getWireguardByteCountJob(): Result<IJob> =
        wireguardByteCountJob?.let {
            Result.success(it)
        } ?: Result.failure(
            VPNProtocolError(
                code = VPNProtocolErrorCode.NO_BYTECOUNT_JOB_FOUND
            )
        )

    override fun clearWireguardByteCountJob(): Result<Unit> {
        wireguardByteCountJob = null
        return Result.success(Unit)
    }
    // endregion

    // region ICacheOpenVpn
    override fun setOpenVpnGeneratedSettings(generatedSettings: List<String>): Result<Unit> {
        this.openVpnGeneratedSettings = generatedSettings
        return Result.success(Unit)
    }

    override fun getOpenVpnGeneratedSettings(): Result<List<String>> =
        openVpnGeneratedSettings?.let {
            Result.success(it)
        } ?: Result.failure(
            VPNProtocolError(
                code = VPNProtocolErrorCode.PROTOCOL_CONFIGURATION_NOT_READY
            )
        )

    override fun clearOpenVpnGeneratedSettings(): Result<Unit> {
        openVpnGeneratedSettings = null
        return Result.success(Unit)
    }

    override fun createOpenVpnProcessConnectedDeferrable(): Result<Unit> {
        this.openVpnProcessConnectedCompletableDeferred = CompletableDeferred()
        return Result.success(Unit)
    }

    override fun getOpenVpnProcessConnectedDeferrable(): Result<CompletableDeferred<Unit>> =
        openVpnProcessConnectedCompletableDeferred?.let {
            Result.success(it)
        } ?: Result.failure(
            VPNProtocolError(
                code = VPNProtocolErrorCode.PROTOCOL_CONFIGURATION_NOT_READY
            )
        )

    override fun clearOpenVpnProcessConnectedDeferrable(): Result<Unit> {
        openVpnProcessConnectedCompletableDeferred = null
        return Result.success(Unit)
    }

    override fun setOpenVpnServerPeerInformation(
        openVpnServerPeerInformation: OpenVpnServerPeerInformation,
    ): Result<Unit> {
        this.openVpnServerPeerInformation = openVpnServerPeerInformation
        return Result.success(Unit)
    }

    override fun getOpenVpnServerPeerInformation(): Result<OpenVpnServerPeerInformation> =
        openVpnServerPeerInformation?.let {
            Result.success(it)
        } ?: Result.failure(
            VPNProtocolError(
                code = VPNProtocolErrorCode.PROTOCOL_PEER_INFORMATION_NOT_READY
            )
        )

    override fun clearOpenVpnServerPeerInformation(): Result<Unit> {
        openVpnServerPeerInformation = null
        return Result.success(Unit)
    }
    // endregion

    // region ICacheProtocol
    override fun getManagementPath(): Result<String> =
        Result.success(context.applicationInfo.dataDir)

    override fun reportConnectivityStatus(
        connectivityStatus: VPNManagerConnectionStatus,
    ): Result<Unit> {
        val clientCoroutineContext = coroutineContext.getClientCoroutineContext().getOrThrow()
        CoroutineScope(clientCoroutineContext).launch {
            connectivityStatusChangeCallback.handleConnectivityStatusChange(connectivityStatus)
        }
        return Result.success(Unit)
    }

    override fun reportByteCount(tx: Long, rx: Long): Result<Unit> {
        val clientCoroutineContext = coroutineContext.getClientCoroutineContext().getOrThrow()
        CoroutineScope(clientCoroutineContext).launch {
            protocolByteCountDependency.byteCount(tx = tx, rx = rx)
        }
        return Result.success(Unit)
    }

    override fun setProtocolConfiguration(
        protocolConfiguration: VPNProtocolConfiguration,
    ): Result<Unit> {
        this.protocolConfiguration = protocolConfiguration
        return Result.success(Unit)
    }

    override fun getProtocolConfiguration(): Result<VPNProtocolConfiguration> =
        protocolConfiguration?.let {
            Result.success(it)
        } ?: Result.failure(
            VPNProtocolError(
                code = VPNProtocolErrorCode.PROTOCOL_CONFIGURATION_NOT_READY
            )
        )

    override fun clearProtocolConfiguration(): Result<Unit> {
        protocolConfiguration = null
        return Result.success(Unit)
    }

    override fun setProtocolServerPeerInformation(
        serverPeerInformation: VPNProtocolServerPeerInformation,
    ): Result<Unit> {
        this.protocolServerPeerInformation = serverPeerInformation
        return Result.success(Unit)
    }

    override fun getProtocolServerPeerInformation(): Result<VPNProtocolServerPeerInformation> =
        protocolServerPeerInformation?.let {
            Result.success(it)
        } ?: Result.failure(
            VPNProtocolError(
                code = VPNProtocolErrorCode.PROTOCOL_PEER_INFORMATION_NOT_READY
            )
        )

    override fun clearProtocolServerPeerInformation(): Result<Unit> {
        protocolServerPeerInformation = null
        return Result.success(Unit)
    }
    // endregion

    // region ICacheWireguardKeys
    override fun setKeyPair(wireguardKeyPair: IWireguardKeyPair): Result<Unit> {
        this.wireguardKeyPair = wireguardKeyPair
        return Result.success(Unit)
    }

    override fun getKeyPair(): Result<IWireguardKeyPair> =
        wireguardKeyPair?.let {
            Result.success(it)
        } ?: Result.failure(
            VPNProtocolError(
                code = VPNProtocolErrorCode.KEY_PAIR_NOT_READY
            )
        )

    override fun clearKeyPair(): Result<Unit> {
        wireguardKeyPair = null
        return Result.success(Unit)
    }

    override fun setAddKeyResponse(wireguardAddKeyResponse: WireguardAddKeyResponse): Result<Unit> {
        this.wireguardAddKeyResponse = wireguardAddKeyResponse
        return Result.success(Unit)
    }

    override fun getAddKeyResponse(): Result<WireguardAddKeyResponse> =
        wireguardAddKeyResponse?.let {
            Result.success(it)
        } ?: Result.failure(
            VPNProtocolError(
                code = VPNProtocolErrorCode.ADD_KEY_RESPONSE_NOT_READY
            )
        )

    override fun clearAddKeyResponse(): Result<Unit> {
        wireguardAddKeyResponse = null
        return Result.success(Unit)
    }
    // endregion

    // region ICacheService
    override fun setServiceFileDescriptorProvider(
        serviceFileDescriptorProvider: ServiceConfigurationFileDescriptorProvider,
    ): Result<Unit> {
        this.serviceFileDescriptorProvider = serviceFileDescriptorProvider
        return Result.success(Unit)
    }

    override fun getServiceFileDescriptorProvider(): Result<ServiceConfigurationFileDescriptorProvider> =
        serviceFileDescriptorProvider?.let {
            Result.success(it)
        } ?: Result.failure(
            VPNProtocolError(
                code = VPNProtocolErrorCode.FILE_DESCRIPTOR_PROVIDER_ERROR
            )
        )

    override fun clearServiceFileDescriptorProvider(): Result<Unit> {
        serviceFileDescriptorProvider = null
        return Result.success(Unit)
    }

    override fun setVpnProtocolService(vpnProtocolService: VPNProtocolService): Result<Unit> {
        this.vpnProtocolService = vpnProtocolService
        return Result.success(Unit)
    }

    override fun getVpnProtocolService(): Result<VPNProtocolService> =
        vpnProtocolService?.let {
            Result.success(it)
        } ?: Result.failure(
            VPNProtocolError(
                code = VPNProtocolErrorCode.PROTOCOL_SERVICE_ERROR
            )
        )

    override fun clearVpnProtocolService(): Result<Unit> {
        vpnProtocolService = null
        return Result.success(Unit)
    }
    // endregion
}
