package com.kape.vpnprotocol.testutils

import android.content.Context
import com.kape.vpnmanager.api.data.externals.IJob
import com.kape.vpnprotocol.data.externals.common.ICache
import com.kape.vpnprotocol.data.externals.common.ICacheProtocol
import com.kape.vpnprotocol.data.externals.common.ICacheService
import com.kape.vpnprotocol.data.externals.common.IConnectivity
import com.kape.vpnprotocol.data.externals.common.IFile
import com.kape.vpnprotocol.data.externals.common.ILogsProcessor
import com.kape.vpnprotocol.data.externals.common.INetworkClient
import com.kape.vpnprotocol.data.externals.common.IProcess
import com.kape.vpnprotocol.data.externals.common.ISerializer
import com.kape.vpnprotocol.data.externals.openvpn.ICacheOpenVpn
import com.kape.vpnprotocol.data.externals.openvpn.IOpenVpn
import com.kape.vpnprotocol.data.externals.wireguard.ICacheWireguard
import com.kape.vpnprotocol.data.externals.wireguard.ICacheWireguardKeys
import com.kape.vpnprotocol.data.externals.wireguard.IWireguard
import com.kape.vpnprotocol.data.externals.wireguard.IWireguardKeyPair
import com.kape.vpnprotocol.domain.usecases.common.ClearCache
import com.kape.vpnprotocol.domain.usecases.common.GetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.GetServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.common.GetTargetProtocol
import com.kape.vpnprotocol.domain.usecases.common.GetVpnProtocolLogs
import com.kape.vpnprotocol.domain.usecases.common.IClearCache
import com.kape.vpnprotocol.domain.usecases.common.IGetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.IGetServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.common.IGetTargetProtocol
import com.kape.vpnprotocol.domain.usecases.common.IGetVpnProtocolLogs
import com.kape.vpnprotocol.domain.usecases.common.IIsNetworkAvailable
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.common.ISetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.ISetServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.common.ISetServiceFileDescriptor
import com.kape.vpnprotocol.domain.usecases.common.ISetVpnService
import com.kape.vpnprotocol.domain.usecases.common.IsNetworkAvailable
import com.kape.vpnprotocol.domain.usecases.common.ReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.common.SetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.SetServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.common.SetServiceFileDescriptor
import com.kape.vpnprotocol.domain.usecases.common.SetVpnService
import com.kape.vpnprotocol.domain.usecases.openvpn.CreateOpenVpnCertificateFile
import com.kape.vpnprotocol.domain.usecases.openvpn.CreateOpenVpnProcessConnectedDeferrable
import com.kape.vpnprotocol.domain.usecases.openvpn.FilterAdditionalOpenVpnParams
import com.kape.vpnprotocol.domain.usecases.openvpn.GenerateOpenVpnServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.openvpn.GenerateOpenVpnSettings
import com.kape.vpnprotocol.domain.usecases.openvpn.ICreateOpenVpnCertificateFile
import com.kape.vpnprotocol.domain.usecases.openvpn.ICreateOpenVpnProcessConnectedDeferrable
import com.kape.vpnprotocol.domain.usecases.openvpn.IFilterAdditionalOpenVpnParams
import com.kape.vpnprotocol.domain.usecases.openvpn.IGenerateOpenVpnServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.openvpn.IGenerateOpenVpnSettings
import com.kape.vpnprotocol.domain.usecases.openvpn.ISetGeneratedOpenVpnSettings
import com.kape.vpnprotocol.domain.usecases.openvpn.IStartOpenVpnEventHandler
import com.kape.vpnprotocol.domain.usecases.openvpn.IStartOpenVpnProcess
import com.kape.vpnprotocol.domain.usecases.openvpn.IStopOpenVpnProcess
import com.kape.vpnprotocol.domain.usecases.openvpn.IWaitForOpenVpnProcessConnectedDeferrable
import com.kape.vpnprotocol.domain.usecases.openvpn.SetGeneratedOpenVpnSettings
import com.kape.vpnprotocol.domain.usecases.openvpn.StartOpenVpnEventHandler
import com.kape.vpnprotocol.domain.usecases.openvpn.StartOpenVpnProcess
import com.kape.vpnprotocol.domain.usecases.openvpn.StopOpenVpnProcess
import com.kape.vpnprotocol.domain.usecases.openvpn.WaitForOpenVpnProcessConnectedDeferrable
import com.kape.vpnprotocol.domain.usecases.wireguard.CreateWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.DestroyWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.GenerateWireguardKeyPair
import com.kape.vpnprotocol.domain.usecases.wireguard.GenerateWireguardServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.wireguard.GenerateWireguardSettings
import com.kape.vpnprotocol.domain.usecases.wireguard.GetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.ICreateWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.IDestroyWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.IGenerateWireguardKeyPair
import com.kape.vpnprotocol.domain.usecases.wireguard.IGenerateWireguardServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.wireguard.IGenerateWireguardSettings
import com.kape.vpnprotocol.domain.usecases.wireguard.IGetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.IPerformWireguardAddKeyRequest
import com.kape.vpnprotocol.domain.usecases.wireguard.IProtectWireguardTunnelSocket
import com.kape.vpnprotocol.domain.usecases.wireguard.ISetWireguardAddKeyResponse
import com.kape.vpnprotocol.domain.usecases.wireguard.ISetWireguardKeyPair
import com.kape.vpnprotocol.domain.usecases.wireguard.ISetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.IStartWireguardByteCountJob
import com.kape.vpnprotocol.domain.usecases.wireguard.IStopWireguardByteCountJob
import com.kape.vpnprotocol.domain.usecases.wireguard.PerformWireguardAddKeyRequest
import com.kape.vpnprotocol.domain.usecases.wireguard.ProtectWireguardTunnelSocket
import com.kape.vpnprotocol.domain.usecases.wireguard.SetWireguardAddKeyResponse
import com.kape.vpnprotocol.domain.usecases.wireguard.SetWireguardKeyPair
import com.kape.vpnprotocol.domain.usecases.wireguard.SetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.StartWireguardByteCountJob
import com.kape.vpnprotocol.domain.usecases.wireguard.StopWireguardByteCountJob

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

internal object GivenUsecase {

    fun clearCache(
        context: Context,
        cache: ICache =
            GivenExternal.cache(context = context),
    ): IClearCache =
        ClearCache(
            cache = cache
        )

    fun getProtocolConfiguration(
        context: Context,
        cacheProtocol: ICacheProtocol =
            GivenExternal.cache(context = context),
    ): IGetProtocolConfiguration =
        GetProtocolConfiguration(
            cacheProtocol = cacheProtocol
        )

    fun getServerPeerInformation(
        context: Context,
        cacheProtocol: ICacheProtocol =
            GivenExternal.cache(context = context),
    ): IGetServerPeerInformation =
        GetServerPeerInformation(
            cacheProtocol = cacheProtocol
        )

    fun getTargetProtocol(
        context: Context,
        cacheProtocol: ICacheProtocol =
            GivenExternal.cache(context = context),
    ): IGetTargetProtocol =
        GetTargetProtocol(
            cacheProtocol = cacheProtocol
        )

    fun isNetworkAvailable(
        connectivity: IConnectivity =
            GivenExternal.connectivity(),
    ): IIsNetworkAvailable =
        IsNetworkAvailable(
            connectivity = connectivity
        )

    fun reportConnectivityStatus(
        context: Context,
        cacheProtocol: ICacheProtocol =
            GivenExternal.cache(context = context),
    ): IReportConnectivityStatus =
        ReportConnectivityStatus(
            cacheProtocol = cacheProtocol
        )

    fun setProtocolConfiguration(
        context: Context,
        cacheProtocol: ICacheProtocol =
            GivenExternal.cache(context = context),
    ): ISetProtocolConfiguration =
        SetProtocolConfiguration(
            cacheProtocol = cacheProtocol
        )

    fun setServerPeerInformation(
        context: Context,
        cacheProtocol: ICacheProtocol =
            GivenExternal.cache(context = context),
    ): ISetServerPeerInformation =
        SetServerPeerInformation(
            cacheProtocol = cacheProtocol
        )

    fun setServiceFileDescriptor(
        context: Context,
        cacheService: ICacheService =
            GivenExternal.cache(context = context),
    ): ISetServiceFileDescriptor =
        SetServiceFileDescriptor(
            cacheService = cacheService
        )

    fun setVpnService(
        context: Context,
        cacheService: ICacheService =
            GivenExternal.cache(context = context),
    ): ISetVpnService =
        SetVpnService(
            cacheService = cacheService
        )

    fun filterAdditionalOpenVpnParams(): IFilterAdditionalOpenVpnParams =
        FilterAdditionalOpenVpnParams()

    fun createOpenVpnCertificateFile(
        context: Context,
        cacheProtocol: ICacheProtocol =
            GivenExternal.cache(context = context),
        file: IFile =
            GivenExternal.file(context = context),
    ): ICreateOpenVpnCertificateFile =
        CreateOpenVpnCertificateFile(
            cacheProtocol = cacheProtocol,
            file = file
        )

    fun createOpenVpnProcessConnectedDeferrable(
        context: Context,
        cacheOpenVpn: ICacheOpenVpn =
            GivenExternal.cache(context = context),
    ): ICreateOpenVpnProcessConnectedDeferrable =
        CreateOpenVpnProcessConnectedDeferrable(
            cacheOpenVpn = cacheOpenVpn
        )

    fun generateOpenVpnServerPeerInformation(
        context: Context,
        cacheOpenVpn: ICacheOpenVpn =
            GivenExternal.cache(context = context),
    ): IGenerateOpenVpnServerPeerInformation =
        GenerateOpenVpnServerPeerInformation(
            cacheOpenVpn = cacheOpenVpn
        )

    fun generateOpenVpnSettings(
        context: Context,
        cacheProtocol: ICacheProtocol =
            GivenExternal.cache(context = context),
        file: IFile =
            GivenExternal.file(context = context),
    ): IGenerateOpenVpnSettings =
        GenerateOpenVpnSettings(
            cacheProtocol = cacheProtocol,
            file = file
        )

    fun setGeneratedOpenVpnSettings(
        context: Context,
        cacheOpenVpn: ICacheOpenVpn =
            GivenExternal.cache(context = context),
    ): ISetGeneratedOpenVpnSettings =
        SetGeneratedOpenVpnSettings(
            cacheOpenVpn = cacheOpenVpn
        )

    fun startOpenVpnEventHandler(
        context: Context,
        cacheProtocol: ICacheProtocol =
            GivenExternal.cache(context = context),
        cacheOpenVpn: ICacheOpenVpn =
            GivenExternal.cache(context = context),
        cacheService: ICacheService =
            GivenExternal.cache(context = context),
    ): IStartOpenVpnEventHandler =
        StartOpenVpnEventHandler(
            cacheProtocol = cacheProtocol,
            cacheOpenVpn = cacheOpenVpn,
            cacheService = cacheService
        )

    fun startOpenVpnProcess(
        context: Context,
        cacheOpenVpn: ICacheOpenVpn =
            GivenExternal.cache(context = context),
        openVpn: IOpenVpn =
            GivenExternal.openVpn(),
    ): IStartOpenVpnProcess =
        StartOpenVpnProcess(
            cacheOpenVpn = cacheOpenVpn,
            openVpn = openVpn
        )

    fun stopOpenVpnProcess(
        openVpn: IOpenVpn =
            GivenExternal.openVpn(),
    ): IStopOpenVpnProcess =
        StopOpenVpnProcess(
            openVpn = openVpn
        )

    fun waitForOpenVpnProcessConnectedDeferrable(
        context: Context,
        cacheOpenVpn: ICacheOpenVpn =
            GivenExternal.cache(context = context),
    ): IWaitForOpenVpnProcessConnectedDeferrable =
        WaitForOpenVpnProcessConnectedDeferrable(
            cacheOpenVpn = cacheOpenVpn
        )

    fun createWireguardTunnel(
        context: Context,
        cacheProtocol: ICacheProtocol =
            GivenExternal.cache(context = context),
        cacheKeys: ICacheWireguardKeys =
            GivenExternal.cache(context = context),
        cacheService: ICacheService =
            GivenExternal.cache(context = context),
        wireguard: IWireguard =
            GivenExternal.wireguard(),
    ): ICreateWireguardTunnel =
        CreateWireguardTunnel(
            cacheProtocol = cacheProtocol,
            cacheKeys = cacheKeys,
            cacheService = cacheService,
            wireguard = wireguard
        )

    fun destroyWireguardTunnel(
        wireguard: IWireguard =
            GivenExternal.wireguard(),
    ): IDestroyWireguardTunnel =
        DestroyWireguardTunnel(
            wireguard = wireguard
        )

    fun generateWireguardKeyPair(
        wireguardKeyPair: IWireguardKeyPair =
            GivenExternal.wireguardKeyPair(),
    ): IGenerateWireguardKeyPair =
        GenerateWireguardKeyPair(
            wireguardKeyPair = wireguardKeyPair
        )

    fun generateWireguardServerPeerInformation(
        context: Context,
        cacheKeys: ICacheWireguardKeys =
            GivenExternal.cache(context = context),
    ): IGenerateWireguardServerPeerInformation =
        GenerateWireguardServerPeerInformation(
            cacheKeys = cacheKeys
        )

    fun generateWireguardSettings(
        context: Context,
        cacheProtocol: ICacheProtocol =
            GivenExternal.cache(context = context),
        cacheKeys: ICacheWireguardKeys =
            GivenExternal.cache(context = context),
    ): IGenerateWireguardSettings =
        GenerateWireguardSettings(
            cacheProtocol = cacheProtocol,
            cacheKeys = cacheKeys
        )

    fun getWireguardTunnelHandle(
        context: Context,
        cacheWireguard: ICacheWireguard =
            GivenExternal.cache(context = context),
    ): IGetWireguardTunnelHandle =
        GetWireguardTunnelHandle(
            cacheWireguard = cacheWireguard
        )

    fun performWireguardAddKeyRequest(
        context: Context,
        networkClient: INetworkClient =
            GivenExternal.networkClient(),
        serializer: ISerializer =
            GivenExternal.serializer(),
        cacheProtocol: ICacheProtocol =
            GivenExternal.cache(context = context),
        cacheKeys: ICacheWireguardKeys =
            GivenExternal.cache(context = context),
    ): IPerformWireguardAddKeyRequest =
        PerformWireguardAddKeyRequest(
            networkClient = networkClient,
            serializer = serializer,
            cacheProtocol = cacheProtocol,
            cacheKeys = cacheKeys
        )

    fun protectWireguardTunnelSocket(
        context: Context,
        cacheWireguard: ICacheWireguard =
            GivenExternal.cache(context = context),
        cacheService: ICacheService =
            GivenExternal.cache(context = context),
        wireguard: IWireguard =
            GivenExternal.wireguard(),
    ): IProtectWireguardTunnelSocket =
        ProtectWireguardTunnelSocket(
            cacheWireguard = cacheWireguard,
            cacheService = cacheService,
            wireguard = wireguard
        )

    fun setWireguardAddKeyResponse(
        context: Context,
        cacheKeys: ICacheWireguardKeys =
            GivenExternal.cache(context = context),
    ): ISetWireguardAddKeyResponse =
        SetWireguardAddKeyResponse(
            cacheKeys = cacheKeys
        )

    fun setWireguardKeyPair(
        context: Context,
        cacheKeys: ICacheWireguardKeys =
            GivenExternal.cache(context = context),
    ): ISetWireguardKeyPair =
        SetWireguardKeyPair(
            cacheKeys = cacheKeys
        )

    fun setWireguardTunnelHandle(
        context: Context,
        cacheWireguard: ICacheWireguard =
            GivenExternal.cache(context = context),
    ): ISetWireguardTunnelHandle =
        SetWireguardTunnelHandle(
            cacheWireguard = cacheWireguard
        )

    fun getVpnProtocolLogs(
        process: IProcess =
            GivenExternal.process(),
        logsProcessor: ILogsProcessor =
            GivenExternal.logsProcessor(),
    ): IGetVpnProtocolLogs =
        GetVpnProtocolLogs(
            process = process,
            logsProcessor = logsProcessor
        )

    fun startWireguardByteCountJob(
        context: Context,
        job: IJob =
            GivenExternal.job(),
        wireguard: IWireguard =
            GivenExternal.wireguard(),
        cacheProtocol: ICacheProtocol =
            GivenExternal.cache(context = context),
        cacheWireguard: ICacheWireguard =
            GivenExternal.cache(context = context),
    ): IStartWireguardByteCountJob =
        StartWireguardByteCountJob(
            job = job,
            wireguard = wireguard,
            cacheProtocol = cacheProtocol,
            cacheWireguard = cacheWireguard
        )

    fun stopWireguardByteCountJob(
        context: Context,
        cacheWireguard: ICacheWireguard =
            GivenExternal.cache(context = context),
    ): IStopWireguardByteCountJob =
        StopWireguardByteCountJob(
            cacheWireguard = cacheWireguard
        )
}
