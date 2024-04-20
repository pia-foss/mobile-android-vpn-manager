package com.kape.vpnprotocol.presenters

import android.content.Context
import com.kape.openvpn.presenters.OpenVpnAPI
import com.kape.openvpn.presenters.OpenVpnBuilder
import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnprotocol.data.externals.common.Cache
import com.kape.vpnprotocol.data.externals.common.Connectivity
import com.kape.vpnprotocol.data.externals.common.CoroutineContext
import com.kape.vpnprotocol.data.externals.common.File
import com.kape.vpnprotocol.data.externals.common.IConnectivity
import com.kape.vpnprotocol.data.externals.common.ICoroutineContext
import com.kape.vpnprotocol.data.externals.common.IFile
import com.kape.vpnprotocol.data.externals.common.IJob
import com.kape.vpnprotocol.data.externals.common.ILogsProcessor
import com.kape.vpnprotocol.data.externals.common.INetworkClient
import com.kape.vpnprotocol.data.externals.common.IProcess
import com.kape.vpnprotocol.data.externals.common.ISerializer
import com.kape.vpnprotocol.data.externals.common.Job
import com.kape.vpnprotocol.data.externals.common.LogsProcessor
import com.kape.vpnprotocol.data.externals.common.NetworkClient
import com.kape.vpnprotocol.data.externals.common.Process
import com.kape.vpnprotocol.data.externals.common.Serializer
import com.kape.vpnprotocol.data.externals.openvpn.IOpenVpn
import com.kape.vpnprotocol.data.externals.openvpn.OpenVpn
import com.kape.vpnprotocol.data.externals.wireguard.IWireguard
import com.kape.vpnprotocol.data.externals.wireguard.IWireguardKeyPair
import com.kape.vpnprotocol.data.externals.wireguard.Wireguard
import com.kape.vpnprotocol.data.externals.wireguard.WireguardKeyPair
import com.kape.vpnprotocol.domain.controllers.common.IStartConnectionController
import com.kape.vpnprotocol.domain.controllers.common.IStartReconnectionController
import com.kape.vpnprotocol.domain.controllers.common.IStopConnectionController
import com.kape.vpnprotocol.domain.controllers.common.StartConnectionController
import com.kape.vpnprotocol.domain.controllers.common.StartReconnectionController
import com.kape.vpnprotocol.domain.controllers.common.StopConnectionController
import com.kape.vpnprotocol.domain.controllers.openvpn.IStartOpenVpnConnectionController
import com.kape.vpnprotocol.domain.controllers.openvpn.IStartOpenVpnReconnectionController
import com.kape.vpnprotocol.domain.controllers.openvpn.IStopOpenVpnConnectionController
import com.kape.vpnprotocol.domain.controllers.openvpn.StartOpenVpnConnectionController
import com.kape.vpnprotocol.domain.controllers.openvpn.StartOpenVpnReconnectionController
import com.kape.vpnprotocol.domain.controllers.openvpn.StopOpenVpnConnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.IStartWireguardConnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.IStartWireguardReconnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.IStopWireguardConnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.StartWireguardConnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.StartWireguardReconnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.StopWireguardConnectionController
import com.kape.vpnprotocol.domain.usecases.common.ClearCache
import com.kape.vpnprotocol.domain.usecases.common.GetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.GetServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.common.GetTargetProtocol
import com.kape.vpnprotocol.domain.usecases.common.GetTargetServer
import com.kape.vpnprotocol.domain.usecases.common.GetVpnProtocolLogs
import com.kape.vpnprotocol.domain.usecases.common.IClearCache
import com.kape.vpnprotocol.domain.usecases.common.IGetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.IGetServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.common.IGetTargetProtocol
import com.kape.vpnprotocol.domain.usecases.common.IGetTargetServer
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
import com.kape.vpnprotocol.domain.usecases.openvpn.OpenVpnMtuTestResultAnnouncer
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
import com.kape.wireguard.presenters.WireguardAPI
import com.kape.wireguard.presenters.WireguardBuilder

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
 * Builder class responsible for creating an instance of an object conforming to the `VPNProtocolAPI`
 * interface.
 */
public class VPNProtocolBuilder {
    private var context: Context? = null
    private var clientCoroutineContext: kotlin.coroutines.CoroutineContext? = null
    private var protocolByteCountDependency: VPNProtocolByteCountDependency? = null
    private var connectivityStatusChangeCallback: VPNProtocolConnectivityStatusChangeCallback? = null

    /**
     * It sets the context to be used within the module.
     *
     * @param context `Context`.
     */
    fun setContext(context: Context): VPNProtocolBuilder = apply {
        this.context = context
    }

    /**
     * Sets the coroutine context to use when invoking the API callbacks.
     *
     * @param clientCoroutineContext `CoroutineContext`.
     */
    fun setClientCoroutineContext(
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
    ): VPNProtocolBuilder = apply {
        this.clientCoroutineContext = clientCoroutineContext
    }

    /**
     * It sets protocol byte count dependency.
     *
     * @param protocolByteCountDependency `VPNProtocolByteCountDependency`.
     */
    fun setProtocolByteCountDependency(
        protocolByteCountDependency: VPNProtocolByteCountDependency,
    ): VPNProtocolBuilder = apply {
        this.protocolByteCountDependency = protocolByteCountDependency
    }

    /**
     * It sets the connectivity status change callback. All changes on the connection status
     * will be reported over this callback.
     *
     * @param connectivityStatusChangeCallback `VPNProtocolConnectivityStatusChangeCallback`.
     */
    fun setConnectivityStatusChangeCallback(
        connectivityStatusChangeCallback: VPNProtocolConnectivityStatusChangeCallback,
    ): VPNProtocolBuilder = apply {
        this.connectivityStatusChangeCallback = connectivityStatusChangeCallback
    }

    /**
     * @return `VPNManagerAPI`.
     */
    fun build(): VPNProtocolAPI {
        val context = this.context
            ?: throw Exception("Context dependency missing.")
        val clientCoroutineContext = this.clientCoroutineContext
            ?: throw Exception("Client Coroutine Context missing.")
        val protocolByteCountDependency = this.protocolByteCountDependency
            ?: throw Exception("Protocol byte count dependency missing.")
        val connectivityStatusChangeCallback = this.connectivityStatusChangeCallback
            ?: throw Exception("Connectivity status change callback missing.")

        return initializeModule(
            context = context,
            clientCoroutineContext = clientCoroutineContext,
            protocolByteCountDependency = protocolByteCountDependency,
            connectivityStatusChangeCallback = connectivityStatusChangeCallback
        )
    }

    // region private
    private fun initializeModule(
        context: Context,
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
        protocolByteCountDependency: VPNProtocolByteCountDependency,
        connectivityStatusChangeCallback: VPNProtocolConnectivityStatusChangeCallback,
    ): VPNProtocolAPI {
        return initializeExternals(
            context = context,
            clientCoroutineContext = clientCoroutineContext,
            protocolByteCountDependency = protocolByteCountDependency,
            connectivityStatusChangeCallback = connectivityStatusChangeCallback
        )
    }

    private fun initializeExternals(
        context: Context,
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
        protocolByteCountDependency: VPNProtocolByteCountDependency,
        connectivityStatusChangeCallback: VPNProtocolConnectivityStatusChangeCallback,
    ): VPNProtocolAPI {
        val coroutineContext: ICoroutineContext =
            CoroutineContext(
                clientCoroutineContext = clientCoroutineContext
            )
        val cache = Cache(
            context = context,
            coroutineContext = coroutineContext,
            protocolByteCountDependency = protocolByteCountDependency,
            connectivityStatusChangeCallback = connectivityStatusChangeCallback
        )
        val file: IFile = File(
            context = context
        )
        val wireguard: IWireguard = Wireguard(
            wireguardApi = initializeWireguardApi()
        )
        val openVpn: IOpenVpn = OpenVpn(
            openVpnApi = initializeOpenVpnApi(
                context = context,
                clientCoroutineContext = clientCoroutineContext,
                connectivityStatusChangeCallback = connectivityStatusChangeCallback
            )
        )
        val job: IJob = Job(
            coroutineContext = coroutineContext
        )
        val process: IProcess = Process()
        val serializer: ISerializer = Serializer()
        val connectivity: IConnectivity = Connectivity()
        val networkClient: INetworkClient = NetworkClient()
        val logsProcessor: ILogsProcessor = LogsProcessor()
        val wireguardKeyPair: IWireguardKeyPair = WireguardKeyPair()
        return initializeUseCases(
            job = job,
            cache = cache,
            connectivity = connectivity,
            file = file,
            process = process,
            wireguard = wireguard,
            openVpn = openVpn,
            serializer = serializer,
            networkClient = networkClient,
            logsProcessor = logsProcessor,
            wireguardKeyPair = wireguardKeyPair,
            coroutineContext = coroutineContext
        )
    }

    private fun initializeUseCases(
        job: IJob,
        cache: Cache,
        connectivity: IConnectivity,
        file: IFile,
        process: IProcess,
        wireguard: IWireguard,
        openVpn: IOpenVpn,
        serializer: ISerializer,
        networkClient: INetworkClient,
        logsProcessor: ILogsProcessor,
        wireguardKeyPair: IWireguardKeyPair,
        coroutineContext: ICoroutineContext,
    ): VPNProtocolAPI {
        val reportConnectivityStatus: IReportConnectivityStatus = ReportConnectivityStatus(
            cacheProtocol = cache
        )
        val clearCache: IClearCache = ClearCache(
            cache = cache
        )
        val isNetworkAvailable: IIsNetworkAvailable = IsNetworkAvailable(
            connectivity = connectivity
        )
        val getTargetProtocol: IGetTargetProtocol = GetTargetProtocol(
            cacheProtocol = cache
        )
        val filterAdditionalOpenVpnParams: IFilterAdditionalOpenVpnParams = FilterAdditionalOpenVpnParams()
        val setVpnService: ISetVpnService = SetVpnService(
            cacheService = cache
        )
        val setProtocolConfiguration: ISetProtocolConfiguration = SetProtocolConfiguration(
            cacheProtocol = cache
        )
        val getProtocolConfiguration: IGetProtocolConfiguration = GetProtocolConfiguration(
            cacheProtocol = cache
        )
        val setServiceFileDescriptor: ISetServiceFileDescriptor = SetServiceFileDescriptor(
            cacheService = cache
        )
        val createWireguardTunnel: ICreateWireguardTunnel = CreateWireguardTunnel(
            cacheProtocol = cache,
            cacheKeys = cache,
            cacheService = cache,
            wireguard = wireguard
        )
        val destroyWireguardTunnel: IDestroyWireguardTunnel = DestroyWireguardTunnel(
            wireguard = wireguard
        )
        val setWireguardTunnelHandle: ISetWireguardTunnelHandle = SetWireguardTunnelHandle(
            cacheWireguard = cache
        )
        val getWireguardTunnelHandle: IGetWireguardTunnelHandle = GetWireguardTunnelHandle(
            cacheWireguard = cache
        )
        val stopWireguardByteCountJob: IStopWireguardByteCountJob = StopWireguardByteCountJob(
            cacheWireguard = cache
        )
        val protectWireguardTunnelSocket: IProtectWireguardTunnelSocket = ProtectWireguardTunnelSocket(
            cacheWireguard = cache,
            cacheService = cache,
            wireguard = wireguard
        )
        val generateWireguardKeyPair: IGenerateWireguardKeyPair = GenerateWireguardKeyPair(
            wireguardKeyPair = wireguardKeyPair
        )
        val performWireguardAddKeyRequest: IPerformWireguardAddKeyRequest = PerformWireguardAddKeyRequest(
            networkClient = networkClient,
            serializer = serializer,
            cacheProtocol = cache,
            cacheKeys = cache
        )
        val setWireguardAddKeyResponse: ISetWireguardAddKeyResponse = SetWireguardAddKeyResponse(
            cacheKeys = cache
        )
        val setWireguardKeyPair: ISetWireguardKeyPair = SetWireguardKeyPair(
            cacheKeys = cache
        )
        val generateWireguardServerPeerInformation: IGenerateWireguardServerPeerInformation =
            GenerateWireguardServerPeerInformation(
                cacheKeys = cache
            )
        val startWireguardByteCountJob: IStartWireguardByteCountJob = StartWireguardByteCountJob(
            job = job,
            wireguard = wireguard,
            cacheProtocol = cache,
            cacheWireguard = cache
        )
        val setServerPeerInformation: ISetServerPeerInformation =
            SetServerPeerInformation(
                cacheProtocol = cache
            )
        val generateWireguardSettings: IGenerateWireguardSettings = GenerateWireguardSettings(
            cacheProtocol = cache,
            cacheKeys = cache
        )
        val createOpenVpnCertificateFile: ICreateOpenVpnCertificateFile = CreateOpenVpnCertificateFile(
            cacheProtocol = cache,
            file = file
        )
        val generateOpenVpnSettings: IGenerateOpenVpnSettings = GenerateOpenVpnSettings(
            cacheProtocol = cache,
            file = file
        )
        val setGeneratedOpenVpnSettings: ISetGeneratedOpenVpnSettings = SetGeneratedOpenVpnSettings(
            cacheOpenVpn = cache
        )
        val createOpenVpnProcessConnectedDeferrable: ICreateOpenVpnProcessConnectedDeferrable =
            CreateOpenVpnProcessConnectedDeferrable(
                cacheOpenVpn = cache
            )
        val startOpenVpnEventHandler: IStartOpenVpnEventHandler = StartOpenVpnEventHandler(
            cacheProtocol = cache,
            cacheOpenVpn = cache,
            cacheService = cache
        )
        val startOpenVpnProcess: IStartOpenVpnProcess = StartOpenVpnProcess(
            cacheOpenVpn = cache,
            openVpn = openVpn
        )
        val waitForOpenVpnProcessConnectedDeferrable: IWaitForOpenVpnProcessConnectedDeferrable =
            WaitForOpenVpnProcessConnectedDeferrable(
                cacheOpenVpn = cache
            )
        val generateOpenVpnServerPeerInformation: IGenerateOpenVpnServerPeerInformation =
            GenerateOpenVpnServerPeerInformation(
                cacheOpenVpn = cache
            )
        val stopOpenVpnProcess: IStopOpenVpnProcess = StopOpenVpnProcess(
            openVpn = openVpn
        )
        val getServerPeerInformation: IGetServerPeerInformation = GetServerPeerInformation(
            cacheProtocol = cache
        )
        val getVpnProtocolLogs: IGetVpnProtocolLogs = GetVpnProtocolLogs(
            process = process,
            logsProcessor = logsProcessor
        )
        val getTargetServer: IGetTargetServer = GetTargetServer(
            cacheProtocol = cache
        )
        return initializeControllers(
            reportConnectivityStatus = reportConnectivityStatus,
            isNetworkAvailable = isNetworkAvailable,
            getTargetProtocol = getTargetProtocol,
            filterAdditionalOpenVpnParams = filterAdditionalOpenVpnParams,
            setVpnService = setVpnService,
            setProtocolConfiguration = setProtocolConfiguration,
            getProtocolConfiguration = getProtocolConfiguration,
            setServiceFileDescriptor = setServiceFileDescriptor,
            createOpenVpnCertificateFile = createOpenVpnCertificateFile,
            generateOpenVpnSettings = generateOpenVpnSettings,
            generateOpenVpnServerPeerInformation = generateOpenVpnServerPeerInformation,
            setGeneratedOpenVpnSettings = setGeneratedOpenVpnSettings,
            createOpenVpnProcessConnectedDeferrable = createOpenVpnProcessConnectedDeferrable,
            startOpenVpnEventHandler = startOpenVpnEventHandler,
            startOpenVpnProcess = startOpenVpnProcess,
            waitForOpenVpnProcessConnectedDeferrable = waitForOpenVpnProcessConnectedDeferrable,
            stopOpenVpnProcess = stopOpenVpnProcess,
            generateWireguardSettings = generateWireguardSettings,
            createWireguardTunnel = createWireguardTunnel,
            setWireguardTunnelHandle = setWireguardTunnelHandle,
            getWireguardTunnelHandle = getWireguardTunnelHandle,
            stopWireguardByteCountJob = stopWireguardByteCountJob,
            generateWireguardKeyPair = generateWireguardKeyPair,
            generateWireguardServerPeerInformation = generateWireguardServerPeerInformation,
            startWireguardByteCountJob = startWireguardByteCountJob,
            setServerPeerInformation = setServerPeerInformation,
            performWireguardAddKeyRequest = performWireguardAddKeyRequest,
            setWireguardAddKeyResponse = setWireguardAddKeyResponse,
            setWireguardKeyPair = setWireguardKeyPair,
            protectWireguardTunnelSocket = protectWireguardTunnelSocket,
            destroyWireguardTunnel = destroyWireguardTunnel,
            getServerPeerInformation = getServerPeerInformation,
            getVpnProtocolLogs = getVpnProtocolLogs,
            getTargetServer = getTargetServer,
            clearCache = clearCache,
            coroutineContext = coroutineContext
        )
    }

    private fun initializeControllers(
        reportConnectivityStatus: IReportConnectivityStatus,
        isNetworkAvailable: IIsNetworkAvailable,
        getTargetProtocol: IGetTargetProtocol,
        filterAdditionalOpenVpnParams: IFilterAdditionalOpenVpnParams,
        setVpnService: ISetVpnService,
        setProtocolConfiguration: ISetProtocolConfiguration,
        getProtocolConfiguration: IGetProtocolConfiguration,
        setServiceFileDescriptor: ISetServiceFileDescriptor,
        createOpenVpnCertificateFile: ICreateOpenVpnCertificateFile,
        generateOpenVpnSettings: IGenerateOpenVpnSettings,
        generateOpenVpnServerPeerInformation: IGenerateOpenVpnServerPeerInformation,
        setGeneratedOpenVpnSettings: ISetGeneratedOpenVpnSettings,
        createOpenVpnProcessConnectedDeferrable: ICreateOpenVpnProcessConnectedDeferrable,
        startOpenVpnEventHandler: IStartOpenVpnEventHandler,
        startOpenVpnProcess: IStartOpenVpnProcess,
        waitForOpenVpnProcessConnectedDeferrable: IWaitForOpenVpnProcessConnectedDeferrable,
        stopOpenVpnProcess: IStopOpenVpnProcess,
        generateWireguardSettings: IGenerateWireguardSettings,
        createWireguardTunnel: ICreateWireguardTunnel,
        setWireguardTunnelHandle: ISetWireguardTunnelHandle,
        getWireguardTunnelHandle: IGetWireguardTunnelHandle,
        stopWireguardByteCountJob: IStopWireguardByteCountJob,
        generateWireguardKeyPair: IGenerateWireguardKeyPair,
        generateWireguardServerPeerInformation: IGenerateWireguardServerPeerInformation,
        startWireguardByteCountJob: IStartWireguardByteCountJob,
        setServerPeerInformation: ISetServerPeerInformation,
        performWireguardAddKeyRequest: IPerformWireguardAddKeyRequest,
        setWireguardAddKeyResponse: ISetWireguardAddKeyResponse,
        setWireguardKeyPair: ISetWireguardKeyPair,
        protectWireguardTunnelSocket: IProtectWireguardTunnelSocket,
        destroyWireguardTunnel: IDestroyWireguardTunnel,
        getServerPeerInformation: IGetServerPeerInformation,
        getVpnProtocolLogs: IGetVpnProtocolLogs,
        getTargetServer: IGetTargetServer,
        clearCache: IClearCache,
        coroutineContext: ICoroutineContext,
    ): VPNProtocolAPI {
        val startWireguardConnectionController: IStartWireguardConnectionController =
            StartWireguardConnectionController(
                reportConnectivityStatus = reportConnectivityStatus,
                isNetworkAvailable = isNetworkAvailable,
                setVpnService = setVpnService,
                setProtocolConfiguration = setProtocolConfiguration,
                setServiceFileDescriptor = setServiceFileDescriptor,
                generateWireguardKeyPair = generateWireguardKeyPair,
                setWireguardKeyPair = setWireguardKeyPair,
                performWireguardAddKeyRequest = performWireguardAddKeyRequest,
                setWireguardAddKeyResponse = setWireguardAddKeyResponse,
                generateWireguardSettings = generateWireguardSettings,
                createWireguardTunnel = createWireguardTunnel,
                setWireguardTunnelHandle = setWireguardTunnelHandle,
                protectWireguardTunnelSocket = protectWireguardTunnelSocket,
                generateWireguardServerPeerInformation = generateWireguardServerPeerInformation,
                startWireguardByteCountJob = startWireguardByteCountJob,
                setServerPeerInformation = setServerPeerInformation,
                getServerPeerInformation = getServerPeerInformation,
                clearCache = clearCache
            )
        val startWireguardReconnectionController: IStartWireguardReconnectionController =
            StartWireguardReconnectionController(
                reportConnectivityStatus = reportConnectivityStatus,
                getProtocolConfiguration = getProtocolConfiguration,
                isNetworkAvailable = isNetworkAvailable,
                getWireguardTunnelHandle = getWireguardTunnelHandle,
                destroyWireguardTunnel = destroyWireguardTunnel,
                generateWireguardSettings = generateWireguardSettings,
                createWireguardTunnel = createWireguardTunnel,
                setWireguardTunnelHandle = setWireguardTunnelHandle,
                protectWireguardTunnelSocket = protectWireguardTunnelSocket
            )
        val stopWireguardConnectionController: IStopWireguardConnectionController =
            StopWireguardConnectionController(
                reportConnectivityStatus = reportConnectivityStatus,
                getWireguardTunnelHandle = getWireguardTunnelHandle,
                stopWireguardByteCountJob = stopWireguardByteCountJob,
                destroyWireguardTunnel = destroyWireguardTunnel,
                clearCache = clearCache
            )
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            StartOpenVpnConnectionController(
                reportConnectivityStatus = reportConnectivityStatus,
                isNetworkAvailable = isNetworkAvailable,
                setVpnService = setVpnService,
                filterAdditionalOpenVpnParams = filterAdditionalOpenVpnParams,
                setProtocolConfiguration = setProtocolConfiguration,
                setServiceFileDescriptor = setServiceFileDescriptor,
                createOpenVpnCertificateFile = createOpenVpnCertificateFile,
                generateOpenVpnSettings = generateOpenVpnSettings,
                setGeneratedOpenVpnSettings = setGeneratedOpenVpnSettings,
                createOpenVpnProcessConnectedDeferrable = createOpenVpnProcessConnectedDeferrable,
                startOpenVpnEventHandler = startOpenVpnEventHandler,
                startOpenVpnProcess = startOpenVpnProcess,
                waitForOpenVpnProcessConnectedDeferrable = waitForOpenVpnProcessConnectedDeferrable,
                generateOpenVpnServerPeerInformation = generateOpenVpnServerPeerInformation,
                setServerPeerInformation = setServerPeerInformation,
                getServerPeerInformation = getServerPeerInformation,
                clearCache = clearCache
            )
        val startOpenVpnReconnectionController: IStartOpenVpnReconnectionController =
            StartOpenVpnReconnectionController(
                reportConnectivityStatus = reportConnectivityStatus,
                getProtocolConfiguration = getProtocolConfiguration,
                isNetworkAvailable = isNetworkAvailable,
                stopOpenVpnProcess = stopOpenVpnProcess,
                createOpenVpnProcessConnectedDeferrable = createOpenVpnProcessConnectedDeferrable,
                startOpenVpnEventHandler = startOpenVpnEventHandler,
                startOpenVpnProcess = startOpenVpnProcess,
                waitForOpenVpnProcessConnectedDeferrable = waitForOpenVpnProcessConnectedDeferrable
            )
        val stopOpenVpnConnectionController: IStopOpenVpnConnectionController = StopOpenVpnConnectionController(
            reportConnectivityStatus = reportConnectivityStatus,
            stopOpenVpnProcess = stopOpenVpnProcess,
            clearCache = clearCache
        )
        val startConnectionController: IStartConnectionController = StartConnectionController(
            startOpenVpnConnectionController = startOpenVpnConnectionController,
            startWireguardConnectionController = startWireguardConnectionController
        )
        val startReconnectionController: IStartReconnectionController = StartReconnectionController(
            getTargetProtocol = getTargetProtocol,
            startOpenVpnReconnectionController = startOpenVpnReconnectionController,
            startWireguardReconnectionController = startWireguardReconnectionController
        )
        val stopConnectionController: IStopConnectionController = StopConnectionController(
            stopOpenVpnConnectionController = stopOpenVpnConnectionController,
            stopWireguardConnectionController = stopWireguardConnectionController
        )
        return VPNProtocol(
            startConnectionController = startConnectionController,
            startReconnectionController = startReconnectionController,
            stopConnectionController = stopConnectionController,
            getVpnProtocolLogs = getVpnProtocolLogs,
            getTargetServer = getTargetServer,
            coroutineContext = coroutineContext
        )
    }

    private fun initializeWireguardApi(): WireguardAPI {
        return WireguardBuilder().build()
    }

    private fun initializeOpenVpnApi(
        context: Context,
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
        connectivityStatusChangeCallback: VPNProtocolConnectivityStatusChangeCallback,
    ): OpenVpnAPI =
        OpenVpnBuilder()
            .setContext(
                context = context
            )
            .setClientCoroutineContext(
                clientCoroutineContext = clientCoroutineContext
            )
            .setOpenVpnMtuTestResultAnnouncer(
                openVpnMtuTestResultAnnouncer = OpenVpnMtuTestResultAnnouncer(
                    connectivityStatusChangeCallback = connectivityStatusChangeCallback,
                    clientCoroutineContext = clientCoroutineContext
                )
            )
            .build()
    // endregion
}

/**
 * Interface to conform when setting up the connectivity status change callback.
 */
public interface VPNProtocolConnectivityStatusChangeCallback {

    /**
     * It reports changes on the vpn connection status.
     *
     * @param status `VPNManagerConnectionStatus`.
     */
    fun handleConnectivityStatusChange(status: VPNManagerConnectionStatus)

    /**
     * It reports the results of the MTU test.
     *
     * @param localToRemote `Int`.
     * @param remoteToLocal `Int`.
     */
    fun handleMtuTestResult(localToRemote: Int, remoteToLocal: Int)
}

/**
 * Interface defining the interface to conform when setting the protocol byte count dependency.
 */
public interface VPNProtocolByteCountDependency {

    /**
     * It sends to the client the protocol's session Tx/Rx in bytes.
     *
     * @param tx `Long`.
     * @param rx `Long`.
     */
    fun byteCount(tx: Long, rx: Long)
}
