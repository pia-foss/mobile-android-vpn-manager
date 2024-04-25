package com.kape.vpnmanager.presenters

import android.content.Context
import com.kape.targetprovider.presenters.TargetProviderAPI
import com.kape.targetprovider.presenters.TargetProviderBuilder
import com.kape.vpnmanager.api.data.externals.CoroutineContext
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import com.kape.vpnmanager.data.externals.Cache
import com.kape.vpnmanager.data.externals.ConnectionEventAnnouncer
import com.kape.vpnmanager.data.externals.ICache
import com.kape.vpnmanager.data.externals.IConnectionEventAnnouncer
import com.kape.vpnmanager.data.externals.IPermissions
import com.kape.vpnmanager.data.externals.IProtocolByteCountAnnouncer
import com.kape.vpnmanager.data.externals.IServiceManager
import com.kape.vpnmanager.data.externals.ITargetProvider
import com.kape.vpnmanager.data.externals.Permissions
import com.kape.vpnmanager.data.externals.ProtocolByteCountAnnouncer
import com.kape.vpnmanager.data.externals.ServiceManager
import com.kape.vpnmanager.data.externals.TargetProvider
import com.kape.vpnmanager.domain.controllers.IStartConnectionController
import com.kape.vpnmanager.domain.controllers.StartConnectionController
import com.kape.vpnmanager.domain.datasources.CacheDatasource
import com.kape.vpnmanager.domain.datasources.ICacheDatasource
import com.kape.vpnmanager.usecases.AddConnectionListener
import com.kape.vpnmanager.usecases.GetServerList
import com.kape.vpnmanager.usecases.GetVpnProtocolLogs
import com.kape.vpnmanager.usecases.GrantPermissions
import com.kape.vpnmanager.usecases.IAddConnectionListener
import com.kape.vpnmanager.usecases.IGetServerList
import com.kape.vpnmanager.usecases.IGetVpnProtocolLogs
import com.kape.vpnmanager.usecases.IGrantPermissions
import com.kape.vpnmanager.usecases.IRemoveConnectionListener
import com.kape.vpnmanager.usecases.ISetClientConfiguration
import com.kape.vpnmanager.usecases.ISetServer
import com.kape.vpnmanager.usecases.IStartConnection
import com.kape.vpnmanager.usecases.IStartIteratingConnection
import com.kape.vpnmanager.usecases.IStopConnection
import com.kape.vpnmanager.usecases.RemoveConnectionListener
import com.kape.vpnmanager.usecases.SetClientConfiguration
import com.kape.vpnmanager.usecases.SetServer
import com.kape.vpnmanager.usecases.StartConnection
import com.kape.vpnmanager.usecases.StartIteratingConnection
import com.kape.vpnmanager.usecases.StopConnection
import com.kape.vpnservicemanager.presenters.VPNServiceConnectivityStatusChangeCallback
import com.kape.vpnservicemanager.presenters.VPNServiceManagerAPI
import com.kape.vpnservicemanager.presenters.VPNServiceManagerBuilder

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
 * Interface defining the interface to conform when setting the permissions dependency.
 */
public interface VPNManagerPermissionsDependency {

    /**
     * It indicates the client to request for the necessary permissions.
     *
     * @param callback `VPNManagerResultCallback<Boolean>` It indicates with a boolean whether
     * the permissions have been granted or not.
     */
    fun requestNecessaryPermissions(callback: VPNManagerResultCallback<Boolean>)
}

/**
 * Interface defining the interface to conform when setting the protocol byte count dependency.
 */
public interface VPNManagerProtocolByteCountDependency {

    /**
     * It sends to the client the protocol's session Tx/Rx in bytes.
     *
     * @param tx `Long`.
     * @param rx `Long`.
     */
    fun byteCount(tx: Long, rx: Long)
}

/**
 * Interface defining the interface to conform when setting the debug logging dependency.
 */
public interface VPNManagerDebugLoggingDependency {

    /**
     * It sends to the client any debug specific logging. It is the task of the client
     * whether to log it or not.
     *
     * @param log `String`.
     */
    fun debugLog(log: String)
}

/**
 * Builder class responsible for creating an instance of an object conforming to the `VPNManagerAPI`
 * interface.
 */
public class VPNManagerBuilder {
    private var context: Context? = null
    private var permissionsDependency: VPNManagerPermissionsDependency? = null
    private var protocolByteCountDependency: VPNManagerProtocolByteCountDependency? = null
    private var debugLoggingDependency: VPNManagerDebugLoggingDependency? = null
    private var clientCoroutineContext: kotlin.coroutines.CoroutineContext? = null

    /**
     * It sets the context to be used within the module. Whether for checking permissions or
     * for persistence purposes.
     *
     * @param context `Context`.
     */
    fun setContext(context: Context): VPNManagerBuilder = apply {
        this.context = context
    }

    /**
     * Sets the coroutine context to use when invoking the API callbacks and builder dependencies.
     *
     * @param clientCoroutineContext `CoroutineContext`.
     */
    fun setClientCoroutineContext(
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
    ): VPNManagerBuilder = apply {
        this.clientCoroutineContext = clientCoroutineContext
    }

    /**
     * It sets the permission dependency. Needed as part of the vpn connection flow.
     * The lack of granted permission will result in a connection failure.
     *
     * @param permissionsDependency `VPNManagerPermissionsDependency`.
     */
    fun setPermissionsDependency(
        permissionsDependency: VPNManagerPermissionsDependency,
    ): VPNManagerBuilder = apply {
        this.permissionsDependency = permissionsDependency
    }

    /**
     * It sets protocol byte count dependency.
     *
     * @param protocolByteCountDependency `VPNManagerProtocolByteCountDependency`.
     */
    fun setProtocolByteCountDependency(
        protocolByteCountDependency: VPNManagerProtocolByteCountDependency,
    ): VPNManagerBuilder = apply {
        this.protocolByteCountDependency = protocolByteCountDependency
    }

    /**
     * It sets the debug logging dependency. It is the task of the client to decide whether
     * to log them or not.
     *
     * @param debugLoggingDependency `VPNManagerDebugLoggingDependency`.
     */
    fun setDebugLoggingDependency(
        debugLoggingDependency: VPNManagerDebugLoggingDependency,
    ): VPNManagerBuilder = apply {
        this.debugLoggingDependency = debugLoggingDependency
    }

    /**
     * @return `VPNManagerAPI`.
     */
    fun build(): VPNManagerAPI {
        val context = this.context
            ?: throw Exception("Context dependency missing.")
        val permissionsDependency = this.permissionsDependency
            ?: throw Exception("Permissions dependency missing.")
        val clientCoroutineContext = this.clientCoroutineContext
            ?: throw Exception("Client CoroutineContext missing.")
        val protocolByteCountDependency = this.protocolByteCountDependency
            ?: throw Exception("Protocol byte count dependency missing.")
        val debugLoggingDependency = this.debugLoggingDependency
            ?: throw Exception("Debug logging dependency missing.")

        return initializeModule(
            context = context,
            clientCoroutineContext = clientCoroutineContext,
            protocolByteCountDependency = protocolByteCountDependency,
            debugLoggingDependency = debugLoggingDependency,
            permissionsDependency = permissionsDependency
        )
    }

    // region private
    private fun initializeModule(
        context: Context,
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
        protocolByteCountDependency: VPNManagerProtocolByteCountDependency,
        debugLoggingDependency: VPNManagerDebugLoggingDependency,
        permissionsDependency: VPNManagerPermissionsDependency,
    ): VPNManagerAPI {
        return initializeExternals(
            context = context,
            clientCoroutineContext = clientCoroutineContext,
            protocolByteCountDependency = protocolByteCountDependency,
            debugLoggingDependency = debugLoggingDependency,
            permissionsDependency = permissionsDependency
        )
    }

    private fun initializeExternals(
        context: Context,
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
        protocolByteCountDependency: VPNManagerProtocolByteCountDependency,
        debugLoggingDependency: VPNManagerDebugLoggingDependency,
        permissionsDependency: VPNManagerPermissionsDependency,
    ): VPNManagerAPI {
        val coroutineContext = CoroutineContext(
            clientCoroutineContext = clientCoroutineContext
        )
        val cache: ICache = Cache(
            protocolByteCountDependency = protocolByteCountDependency,
            debugLoggingDependency = debugLoggingDependency
        )
        val permissions: IPermissions = Permissions(
            context = context,
            coroutineContext = coroutineContext,
            permissionsDependency = permissionsDependency
        )
        val targetProvider: ITargetProvider = TargetProvider(
            targetProviderApi = initializeTargetProviderApi(
                clientCoroutineContext = clientCoroutineContext
            )
        )
        val connectionEventAnnouncer: IConnectionEventAnnouncer = ConnectionEventAnnouncer(
            cache = cache
        )
        val protocolByteCountAnnouncer: IProtocolByteCountAnnouncer = ProtocolByteCountAnnouncer(
            vpnManagerProtocolByteCountDependency = protocolByteCountDependency
        )
        val serviceManager: IServiceManager = ServiceManager(
            serviceManagerApi = initializeServiceManagerApi(
                context = context,
                clientCoroutineContext = clientCoroutineContext,
                protocolByteCountAnnouncer = protocolByteCountAnnouncer,
                connectionEventAnnouncer = connectionEventAnnouncer
            ),
            cache = cache
        )

        return initializeGateways(
            cache = cache,
            permissions = permissions,
            targetProvider = targetProvider,
            serviceManager = serviceManager,
            coroutineContext = coroutineContext,
            connectionEventAnnouncer = connectionEventAnnouncer
        )
    }

    private fun initializeGateways(
        cache: ICache,
        permissions: IPermissions,
        targetProvider: ITargetProvider,
        serviceManager: IServiceManager,
        coroutineContext: ICoroutineContext,
        connectionEventAnnouncer: IConnectionEventAnnouncer,
    ): VPNManagerAPI {
        val cacheDatasource: ICacheDatasource = CacheDatasource(cache = cache)

        return initializeUseCases(
            permissions = permissions,
            targetProvider = targetProvider,
            serviceManager = serviceManager,
            cacheDatasource = cacheDatasource,
            coroutineContext = coroutineContext,
            connectionEventAnnouncer = connectionEventAnnouncer
        )
    }

    private fun initializeUseCases(
        permissions: IPermissions,
        targetProvider: ITargetProvider,
        serviceManager: IServiceManager,
        cacheDatasource: ICacheDatasource,
        coroutineContext: ICoroutineContext,
        connectionEventAnnouncer: IConnectionEventAnnouncer,
    ): VPNManagerAPI {
        val grantPermissions: IGrantPermissions = GrantPermissions(
            permissions = permissions,
            cacheDatasource = cacheDatasource
        )
        val setClientConfiguration: ISetClientConfiguration = SetClientConfiguration(
            cacheDatasource = cacheDatasource
        )
        val getServerList: IGetServerList = GetServerList(
            cacheDatasource = cacheDatasource
        )
        val setServer: ISetServer = SetServer(
            cacheDatasource = cacheDatasource
        )
        val startConnection: IStartConnection = StartConnection(
            serviceManager = serviceManager
        )
        val startIteratingConnection: IStartIteratingConnection = StartIteratingConnection(
            targetProvider = targetProvider,
            setServer = setServer,
            startConnection = startConnection,
            connectionEventAnnouncer = connectionEventAnnouncer,
            coroutineContext = coroutineContext
        )
        val stopConnection: IStopConnection = StopConnection(
            serviceManager = serviceManager
        )
        val getVpnProtocolLogs: IGetVpnProtocolLogs = GetVpnProtocolLogs(
            serviceManager = serviceManager
        )
        val addConnectionListener: IAddConnectionListener = AddConnectionListener(
            cacheDatasource = cacheDatasource
        )
        val removeConnectionListener: IRemoveConnectionListener = RemoveConnectionListener(
            cacheDatasource = cacheDatasource
        )

        return initializeControllers(
            grantPermissions = grantPermissions,
            setClientConfiguration = setClientConfiguration,
            getServerList = getServerList,
            startIteratingConnection = startIteratingConnection,
            stopConnection = stopConnection,
            getVpnProtocolLogs = getVpnProtocolLogs,
            addConnectionListener = addConnectionListener,
            removeConnectionListener = removeConnectionListener,
            coroutineContext = coroutineContext
        )
    }

    private fun initializeControllers(
        grantPermissions: IGrantPermissions,
        setClientConfiguration: ISetClientConfiguration,
        getServerList: IGetServerList,
        startIteratingConnection: IStartIteratingConnection,
        stopConnection: IStopConnection,
        getVpnProtocolLogs: IGetVpnProtocolLogs,
        addConnectionListener: IAddConnectionListener,
        removeConnectionListener: IRemoveConnectionListener,
        coroutineContext: ICoroutineContext,
    ): VPNManagerAPI {
        val startConnectionController: IStartConnectionController = StartConnectionController(
            grantPermissions = grantPermissions,
            setClientConfiguration = setClientConfiguration,
            getServerList = getServerList,
            startIteratingConnection = startIteratingConnection
        )

        return VPNManager(
            startConnectionController = startConnectionController,
            stopConnectionUseCase = stopConnection,
            addConnectionListenerUseCase = addConnectionListener,
            removeConnectionListenerUseCase = removeConnectionListener,
            getVpnProtocolLogsUseCase = getVpnProtocolLogs,
            coroutineContext = coroutineContext
        )
    }

    private fun initializeServiceManagerApi(
        context: Context,
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
        protocolByteCountAnnouncer: IProtocolByteCountAnnouncer,
        connectionEventAnnouncer: VPNServiceConnectivityStatusChangeCallback,
    ): VPNServiceManagerAPI =
        VPNServiceManagerBuilder()
            .setContext(context)
            .setClientCoroutineContext(
                clientCoroutineContext = clientCoroutineContext
            )
            .setProtocolByteCountDependency(
                protocolByteCountDependency = protocolByteCountAnnouncer
            )
            .setConnectivityStatusChangeCallback(
                connectivityStatusChangeCallback = connectionEventAnnouncer
            )
            .build()

    private fun initializeTargetProviderApi(
        clientCoroutineContext: kotlin.coroutines.CoroutineContext,
    ): TargetProviderAPI =
        TargetProviderBuilder()
            .setClientCoroutineContext(
                clientCoroutineContext = clientCoroutineContext
            )
            .build()
    // endregion
}
