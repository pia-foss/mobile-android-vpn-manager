package com.kape.vpnservicemanager.presenters

import android.content.Context
import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnmanager.api.data.externals.CoroutineContext
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import com.kape.vpnprotocol.presenters.VPNProtocolAPI
import com.kape.vpnprotocol.presenters.VPNProtocolBuilder
import com.kape.vpnservicemanager.data.externals.Cache
import com.kape.vpnservicemanager.data.externals.ConnectionEventCallback
import com.kape.vpnservicemanager.data.externals.Connectivity
import com.kape.vpnservicemanager.data.externals.ICache
import com.kape.vpnservicemanager.data.externals.IConnectionEventCallback
import com.kape.vpnservicemanager.data.externals.IConnectivity
import com.kape.vpnservicemanager.data.externals.IProtocol
import com.kape.vpnservicemanager.data.externals.IProtocolByteCountAnnouncer
import com.kape.vpnservicemanager.data.externals.IServiceConnection
import com.kape.vpnservicemanager.data.externals.ISubnet
import com.kape.vpnservicemanager.data.externals.Protocol
import com.kape.vpnservicemanager.data.externals.ProtocolByteCountAnnouncer
import com.kape.vpnservicemanager.data.externals.ServiceConnection
import com.kape.vpnservicemanager.data.externals.Subnet
import com.kape.vpnservicemanager.domain.controllers.IStartConnectionController
import com.kape.vpnservicemanager.domain.controllers.IStopConnectionController
import com.kape.vpnservicemanager.domain.controllers.StartConnectionController
import com.kape.vpnservicemanager.domain.controllers.StopConnectionController
import com.kape.vpnservicemanager.domain.datasources.ServiceGateway
import com.kape.vpnservicemanager.domain.usecases.ClearCache
import com.kape.vpnservicemanager.domain.usecases.GetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.GetVpnProtocolLogs
import com.kape.vpnservicemanager.domain.usecases.IClearCache
import com.kape.vpnservicemanager.domain.usecases.IGetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.IGetVpnProtocolLogs
import com.kape.vpnservicemanager.domain.usecases.IIsServiceCleared
import com.kape.vpnservicemanager.domain.usecases.IIsServicePresent
import com.kape.vpnservicemanager.domain.usecases.ISetProtocolConfiguration
import com.kape.vpnservicemanager.domain.usecases.ISetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.IStartConnection
import com.kape.vpnservicemanager.domain.usecases.IStartReconnectionHandler
import com.kape.vpnservicemanager.domain.usecases.IStopConnection
import com.kape.vpnservicemanager.domain.usecases.IsServiceCleared
import com.kape.vpnservicemanager.domain.usecases.IsServicePresent
import com.kape.vpnservicemanager.domain.usecases.SetProtocolConfiguration
import com.kape.vpnservicemanager.domain.usecases.SetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.StartConnection
import com.kape.vpnservicemanager.domain.usecases.StartReconnectionHandler
import com.kape.vpnservicemanager.domain.usecases.StopConnection

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
 * Builder class responsible for creating an instance of an object conforming to the `VPNServiceAPI`
 * interface.
 */
public class VPNServiceManagerBuilder {
    private var context: Context? = null
    private var clientCoroutineContext: kotlin.coroutines.CoroutineContext? = null
    private var protocolByteCountDependency: VPNServiceProtocolByteCountDependency? = null
    private var connectivityStatusChangeCallback: VPNServiceConnectivityStatusChangeCallback? = null

    /**
     * It sets the context to be used within the module.
     *
     * @param context `Context`.
     */
    fun setContext(context: Context): VPNServiceManagerBuilder = apply {
        this.context = context
    }

    /**
     * Sets the coroutine context to use when invoking the API callbacks.
     *
     * @param clientCoroutineContext `CoroutineContext`.
     */
    fun setClientCoroutineContext(
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
    ): VPNServiceManagerBuilder = apply {
        this.clientCoroutineContext = clientCoroutineContext
    }

    /**
     * It sets protocol byte count dependency.
     *
     * @param protocolByteCountDependency `VPNServiceProtocolByteCountDependency`.
     */
    fun setProtocolByteCountDependency(
        protocolByteCountDependency: VPNServiceProtocolByteCountDependency,
    ): VPNServiceManagerBuilder = apply {
        this.protocolByteCountDependency = protocolByteCountDependency
    }

    /**
     * It sets the connectivity status change callback. All changes on the connection status
     * will be reported over this callback.
     *
     * @param connectivityStatusChangeCallback `VPNProtocolConnectivityStatusChangeCallback`.
     */
    fun setConnectivityStatusChangeCallback(
        connectivityStatusChangeCallback: VPNServiceConnectivityStatusChangeCallback,
    ): VPNServiceManagerBuilder = apply {
        this.connectivityStatusChangeCallback = connectivityStatusChangeCallback
    }

    /**
     * @return `VPNServiceManagerAPI`.
     */
    fun build(): VPNServiceManagerAPI {
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
        protocolByteCountDependency: VPNServiceProtocolByteCountDependency,
        connectivityStatusChangeCallback: VPNServiceConnectivityStatusChangeCallback,
    ): VPNServiceManagerAPI {
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
        protocolByteCountDependency: VPNServiceProtocolByteCountDependency,
        connectivityStatusChangeCallback: VPNServiceConnectivityStatusChangeCallback,
    ): VPNServiceManagerAPI {
        val cache: ICache = Cache()
        val subnet: ISubnet = Subnet()
        val connectivity: IConnectivity = Connectivity()
        val coroutineContext: ICoroutineContext = CoroutineContext(
            clientCoroutineContext = clientCoroutineContext
        )
        val connectionEventCallback: IConnectionEventCallback = ConnectionEventCallback(
            connectivityStatusChangeCallback = connectivityStatusChangeCallback
        )
        val protocolByteCountAnnouncer: IProtocolByteCountAnnouncer = ProtocolByteCountAnnouncer(
            vpnServiceProtocolByteCountDependency = protocolByteCountDependency
        )
        val protocol: IProtocol = Protocol(
            cache = cache,
            vpnProtocolApi = initializeProtocolApi(
                context = context,
                clientCoroutineContext = clientCoroutineContext,
                protocolByteCountAnnouncer = protocolByteCountAnnouncer,
                connectionEventCallback = connectionEventCallback
            )
        )
        val serviceConnection: IServiceConnection = ServiceConnection(
            context = context,
            cache = cache,
            subnet = subnet,
            protocol = protocol,
            coroutineContext = coroutineContext
        )

        return initializeGateways(
            context = context,
            cache = cache,
            protocol = protocol,
            connectivity = connectivity,
            serviceConnection = serviceConnection,
            coroutineContext = coroutineContext
        )
    }

    private fun initializeGateways(
        context: Context,
        cache: ICache,
        protocol: IProtocol,
        connectivity: IConnectivity,
        serviceConnection: IServiceConnection,
        coroutineContext: ICoroutineContext,
    ): VPNServiceManagerAPI {
        val serviceGateway = ServiceGateway(
            cache = cache,
            context = context,
            serviceConnection = serviceConnection
        )

        return initializeUseCases(
            cache = cache,
            protocol = protocol,
            connectivity = connectivity,
            serviceGateway = serviceGateway,
            coroutineContext = coroutineContext
        )
    }

    private fun initializeUseCases(
        cache: ICache,
        protocol: IProtocol,
        connectivity: IConnectivity,
        serviceGateway: ServiceGateway,
        coroutineContext: ICoroutineContext,
    ): VPNServiceManagerAPI {
        val startConnection: IStartConnection = StartConnection(
            serviceGateway = serviceGateway
        )
        val startReconnectionHandler: IStartReconnectionHandler = StartReconnectionHandler(
            protocol = protocol,
            connectivity = connectivity,
            cacheService = cache,
            serviceGatewayProtocol = serviceGateway,
            coroutineContext = coroutineContext
        )
        val stopConnection: IStopConnection = StopConnection(
            serviceGateway = serviceGateway
        )
        val getVpnProtocolLogs: IGetVpnProtocolLogs = GetVpnProtocolLogs(
            protocol = protocol
        )
        val setProtocolConfiguration: ISetProtocolConfiguration = SetProtocolConfiguration(
            cacheProtocol = cache
        )
        val setServerPeerInformation: ISetServerPeerInformation = SetServerPeerInformation(
            cacheProtocol = cache
        )
        val getServerPeerInformation: IGetServerPeerInformation = GetServerPeerInformation(
            cacheProtocol = cache
        )
        val clearCache: IClearCache = ClearCache(
            cache = cache
        )
        val isServiceCleared: IIsServiceCleared = IsServiceCleared(
            cacheService = cache
        )
        val isServicePresent: IIsServicePresent = IsServicePresent(
            cacheService = cache
        )

        return initializeControllers(
            isServicePresent = isServicePresent,
            clearCache = clearCache,
            isServiceCleared = isServiceCleared,
            setProtocolConfiguration = setProtocolConfiguration,
            setServerPeerInformation = setServerPeerInformation,
            startConnection = startConnection,
            getServerPeerInformation = getServerPeerInformation,
            startReconnectionHandler = startReconnectionHandler,
            stopConnection = stopConnection,
            getVpnProtocolLogs = getVpnProtocolLogs,
            coroutineContext = coroutineContext
        )
    }

    private fun initializeControllers(
        isServicePresent: IIsServicePresent,
        isServiceCleared: IIsServiceCleared,
        setProtocolConfiguration: ISetProtocolConfiguration,
        startConnection: IStartConnection,
        setServerPeerInformation: ISetServerPeerInformation,
        startReconnectionHandler: IStartReconnectionHandler,
        getServerPeerInformation: IGetServerPeerInformation,
        stopConnection: IStopConnection,
        getVpnProtocolLogs: IGetVpnProtocolLogs,
        clearCache: IClearCache,
        coroutineContext: ICoroutineContext,
    ): VPNServiceManagerAPI {
        val startConnectionController: IStartConnectionController = StartConnectionController(
            isServiceCleared = isServiceCleared,
            setProtocolConfiguration = setProtocolConfiguration,
            setServerPeerInformation = setServerPeerInformation,
            startConnection = startConnection,
            startReconnectionHandler = startReconnectionHandler,
            getServerPeerInformation = getServerPeerInformation,
            stopConnection = stopConnection,
            clearCache = clearCache
        )
        val stopConnectionController: IStopConnectionController = StopConnectionController(
            isServicePresent = isServicePresent,
            stopConnection = stopConnection,
            clearCache = clearCache
        )

        return VPNServiceManager(
            startConnectionController = startConnectionController,
            stopConnectionController = stopConnectionController,
            getVpnProtocolLogsUseCase = getVpnProtocolLogs,
            coroutineContext = coroutineContext
        )
    }

    private fun initializeProtocolApi(
        context: Context,
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
        protocolByteCountAnnouncer: IProtocolByteCountAnnouncer,
        connectionEventCallback: IConnectionEventCallback,
    ): VPNProtocolAPI =
        VPNProtocolBuilder()
            .setContext(context)
            .setClientCoroutineContext(
                clientCoroutineContext = clientCoroutineContext
            )
            .setProtocolByteCountDependency(
                protocolByteCountDependency = protocolByteCountAnnouncer
            )
            .setConnectivityStatusChangeCallback(
                connectivityStatusChangeCallback = connectionEventCallback
            )
            .build()
    // endregion
}

/**
 * Interface to conform when setting up the connectivity status change callback.
 */
public interface VPNServiceConnectivityStatusChangeCallback {

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
public interface VPNServiceProtocolByteCountDependency {

    /**
     * It sends to the client the protocol's session Tx/Rx in bytes.
     *
     * @param tx `Long`.
     * @param rx `Long`.
     */
    fun byteCount(tx: Long, rx: Long)
}
