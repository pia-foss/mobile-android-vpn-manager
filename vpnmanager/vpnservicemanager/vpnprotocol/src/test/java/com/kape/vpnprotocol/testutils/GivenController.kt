package com.kape.vpnprotocol.testutils

import android.content.Context
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
import com.kape.vpnprotocol.domain.usecases.common.IClearCache
import com.kape.vpnprotocol.domain.usecases.common.IGetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.IGetServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.common.IGetTargetProtocol
import com.kape.vpnprotocol.domain.usecases.common.IIsNetworkAvailable
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.common.ISetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.ISetServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.common.ISetServiceFileDescriptor
import com.kape.vpnprotocol.domain.usecases.common.ISetVpnService
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

internal object GivenController {

    fun startConnectionController(
        context: Context,
        startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            GivenController.startOpenVpnConnectionController(context = context),
        startWireguardConnectionController: IStartWireguardConnectionController =
            GivenController.startWireguardConnectionController(context = context),
    ): IStartConnectionController =
        StartConnectionController(
            startOpenVpnConnectionController = startOpenVpnConnectionController,
            startWireguardConnectionController = startWireguardConnectionController
        )

    fun startOpenVpnConnectionController(
        context: Context,
        reportConnectivityStatus: IReportConnectivityStatus =
            GivenUsecase.reportConnectivityStatus(context = context),
        isNetworkAvailable: IIsNetworkAvailable =
            GivenUsecase.isNetworkAvailable(),
        setVpnService: ISetVpnService =
            GivenUsecase.setVpnService(context = context),
        filterAdditionalOpenVpnParams: IFilterAdditionalOpenVpnParams =
            GivenUsecase.filterAdditionalOpenVpnParams(),
        setProtocolConfiguration: ISetProtocolConfiguration =
            GivenUsecase.setProtocolConfiguration(context = context),
        setServiceFileDescriptor: ISetServiceFileDescriptor =
            GivenUsecase.setServiceFileDescriptor(context = context),
        createOpenVpnCertificateFile: ICreateOpenVpnCertificateFile =
            GivenUsecase.createOpenVpnCertificateFile(context = context),
        generateOpenVpnSettings: IGenerateOpenVpnSettings =
            GivenUsecase.generateOpenVpnSettings(context = context),
        setGeneratedOpenVpnSettings: ISetGeneratedOpenVpnSettings =
            GivenUsecase.setGeneratedOpenVpnSettings(context = context),
        createOpenVpnProcessConnectedDeferrable: ICreateOpenVpnProcessConnectedDeferrable =
            GivenUsecase.createOpenVpnProcessConnectedDeferrable(context = context),
        startOpenVpnEventHandler: IStartOpenVpnEventHandler =
            GivenUsecase.startOpenVpnEventHandler(context = context),
        startOpenVpnProcess: IStartOpenVpnProcess =
            GivenUsecase.startOpenVpnProcess(context = context),
        waitForOpenVpnProcessConnectedDeferrable: IWaitForOpenVpnProcessConnectedDeferrable =
            GivenUsecase.waitForOpenVpnProcessConnectedDeferrable(context = context),
        generateOpenVpnServerPeerInformation: IGenerateOpenVpnServerPeerInformation =
            GivenUsecase.generateOpenVpnServerPeerInformation(context = context),
        setServerPeerInformation: ISetServerPeerInformation =
            GivenUsecase.setServerPeerInformation(context = context),
        getServerPeerInformation: IGetServerPeerInformation =
            GivenUsecase.getServerPeerInformation(context = context),
        clearCache: IClearCache =
            GivenUsecase.clearCache(context = context),
    ): IStartOpenVpnConnectionController =
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

    fun startWireguardConnectionController(
        context: Context,
        reportConnectivityStatus: IReportConnectivityStatus =
            GivenUsecase.reportConnectivityStatus(context = context),
        isNetworkAvailable: IIsNetworkAvailable =
            GivenUsecase.isNetworkAvailable(),
        setVpnService: ISetVpnService =
            GivenUsecase.setVpnService(context = context),
        setProtocolConfiguration: ISetProtocolConfiguration =
            GivenUsecase.setProtocolConfiguration(context = context),
        setServiceFileDescriptor: ISetServiceFileDescriptor =
            GivenUsecase.setServiceFileDescriptor(context = context),
        generateWireguardKeyPair: IGenerateWireguardKeyPair =
            GivenUsecase.generateWireguardKeyPair(),
        setWireguardKeyPair: ISetWireguardKeyPair =
            GivenUsecase.setWireguardKeyPair(context = context),
        performWireguardAddKeyRequest: IPerformWireguardAddKeyRequest =
            GivenUsecase.performWireguardAddKeyRequest(context = context),
        setWireguardAddKeyResponse: ISetWireguardAddKeyResponse =
            GivenUsecase.setWireguardAddKeyResponse(context = context),
        generateWireguardSettings: IGenerateWireguardSettings =
            GivenUsecase.generateWireguardSettings(context = context),
        createWireguardTunnel: ICreateWireguardTunnel =
            GivenUsecase.createWireguardTunnel(context = context),
        setWireguardTunnelHandle: ISetWireguardTunnelHandle =
            GivenUsecase.setWireguardTunnelHandle(context = context),
        protectWireguardTunnelSocket: IProtectWireguardTunnelSocket =
            GivenUsecase.protectWireguardTunnelSocket(context = context),
        generateWireguardServerPeerInformation: IGenerateWireguardServerPeerInformation =
            GivenUsecase.generateWireguardServerPeerInformation(context = context),
        startWireguardByteCountJob: IStartWireguardByteCountJob =
            GivenUsecase.startWireguardByteCountJob(context = context),
        setServerPeerInformation: ISetServerPeerInformation =
            GivenUsecase.setServerPeerInformation(context = context),
        getServerPeerInformation: IGetServerPeerInformation =
            GivenUsecase.getServerPeerInformation(context = context),
        clearCache: IClearCache =
            GivenUsecase.clearCache(context = context),
    ): IStartWireguardConnectionController =
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

    fun startReconnectionController(
        context: Context,
        getTargetProtocol: IGetTargetProtocol =
            GivenUsecase.getTargetProtocol(context = context),
        startOpenVpnReconnectionController: IStartOpenVpnReconnectionController =
            GivenController.startOpenVpnReconnectionController(context = context),
        startWireguardReconnectionController: IStartWireguardReconnectionController =
            GivenController.startWireguardReconnectionController(context = context),
    ): IStartReconnectionController =
        StartReconnectionController(
            getTargetProtocol = getTargetProtocol,
            startOpenVpnReconnectionController = startOpenVpnReconnectionController,
            startWireguardReconnectionController = startWireguardReconnectionController
        )

    fun startOpenVpnReconnectionController(
        context: Context,
        reportConnectivityStatus: IReportConnectivityStatus =
            GivenUsecase.reportConnectivityStatus(context = context),
        getProtocolConfiguration: IGetProtocolConfiguration =
            GivenUsecase.getProtocolConfiguration(context = context),
        isNetworkAvailable: IIsNetworkAvailable =
            GivenUsecase.isNetworkAvailable(),
        stopOpenVpnProcess: IStopOpenVpnProcess =
            GivenUsecase.stopOpenVpnProcess(),
        createOpenVpnProcessConnectedDeferrable: ICreateOpenVpnProcessConnectedDeferrable =
            GivenUsecase.createOpenVpnProcessConnectedDeferrable(context = context),
        startOpenVpnEventHandler: IStartOpenVpnEventHandler =
            GivenUsecase.startOpenVpnEventHandler(context = context),
        startOpenVpnProcess: IStartOpenVpnProcess =
            GivenUsecase.startOpenVpnProcess(context = context),
        waitForOpenVpnProcessConnectedDeferrable: IWaitForOpenVpnProcessConnectedDeferrable =
            GivenUsecase.waitForOpenVpnProcessConnectedDeferrable(context = context),
    ): IStartOpenVpnReconnectionController =
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

    fun startWireguardReconnectionController(
        context: Context,
        reportConnectivityStatus: IReportConnectivityStatus =
            GivenUsecase.reportConnectivityStatus(context = context),
        getProtocolConfiguration: IGetProtocolConfiguration =
            GivenUsecase.getProtocolConfiguration(context = context),
        isNetworkAvailable: IIsNetworkAvailable =
            GivenUsecase.isNetworkAvailable(),
        getWireguardTunnelHandle: IGetWireguardTunnelHandle =
            GivenUsecase.getWireguardTunnelHandle(context = context),
        destroyWireguardTunnel: IDestroyWireguardTunnel =
            GivenUsecase.destroyWireguardTunnel(),
        generateWireguardSettings: IGenerateWireguardSettings =
            GivenUsecase.generateWireguardSettings(context = context),
        createWireguardTunnel: ICreateWireguardTunnel =
            GivenUsecase.createWireguardTunnel(context = context),
        setWireguardTunnelHandle: ISetWireguardTunnelHandle =
            GivenUsecase.setWireguardTunnelHandle(context = context),
        protectWireguardTunnelSocket: IProtectWireguardTunnelSocket =
            GivenUsecase.protectWireguardTunnelSocket(context = context),
    ): IStartWireguardReconnectionController =
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

    fun stopConnectionController(
        context: Context,
        stopOpenVpnConnectionController: IStopOpenVpnConnectionController =
            GivenController.stopOpenVpnConnectionController(context = context),
        stopWireguardConnectionController: IStopWireguardConnectionController =
            GivenController.stopWireguardConnectionController(context = context),
    ): IStopConnectionController =
        StopConnectionController(
            stopOpenVpnConnectionController = stopOpenVpnConnectionController,
            stopWireguardConnectionController = stopWireguardConnectionController
        )

    fun stopOpenVpnConnectionController(
        context: Context,
        reportConnectivityStatus: IReportConnectivityStatus =
            GivenUsecase.reportConnectivityStatus(context = context),
        stopOpenVpnProcess: IStopOpenVpnProcess =
            GivenUsecase.stopOpenVpnProcess(),
        clearCache: IClearCache =
            GivenUsecase.clearCache(context = context),
    ): IStopOpenVpnConnectionController =
        StopOpenVpnConnectionController(
            reportConnectivityStatus = reportConnectivityStatus,
            stopOpenVpnProcess = stopOpenVpnProcess,
            clearCache = clearCache
        )

    fun stopWireguardConnectionController(
        context: Context,
        reportConnectivityStatus: IReportConnectivityStatus =
            GivenUsecase.reportConnectivityStatus(context = context),
        getWireguardTunnelHandle: IGetWireguardTunnelHandle =
            GivenUsecase.getWireguardTunnelHandle(context = context),
        stopWireguardByteCountJob: IStopWireguardByteCountJob =
            GivenUsecase.stopWireguardByteCountJob(context = context),
        destroyWireguardTunnel: IDestroyWireguardTunnel =
            GivenUsecase.destroyWireguardTunnel(),
        clearCache: IClearCache =
            GivenUsecase.clearCache(context = context),
    ): IStopWireguardConnectionController =
        StopWireguardConnectionController(
            reportConnectivityStatus = reportConnectivityStatus,
            getWireguardTunnelHandle = getWireguardTunnelHandle,
            stopWireguardByteCountJob = stopWireguardByteCountJob,
            destroyWireguardTunnel = destroyWireguardTunnel,
            clearCache = clearCache
        )
}
