package com.kape.vpnservicemanager.testutils

import android.content.Context
import com.kape.vpnservicemanager.domain.controllers.IStartConnectionController
import com.kape.vpnservicemanager.domain.controllers.IStopConnectionController
import com.kape.vpnservicemanager.domain.controllers.StartConnectionController
import com.kape.vpnservicemanager.domain.controllers.StopConnectionController
import com.kape.vpnservicemanager.domain.usecases.IClearCache
import com.kape.vpnservicemanager.domain.usecases.IGetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.IIsServiceCleared
import com.kape.vpnservicemanager.domain.usecases.IIsServicePresent
import com.kape.vpnservicemanager.domain.usecases.ISetProtocolConfiguration
import com.kape.vpnservicemanager.domain.usecases.ISetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.IStartConnection
import com.kape.vpnservicemanager.domain.usecases.IStartReconnectionHandler
import com.kape.vpnservicemanager.domain.usecases.IStopConnection

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
        isServiceCleared: IIsServiceCleared = GivenUsecase.isServiceCleared(),
        setProtocolConfiguration: ISetProtocolConfiguration = GivenUsecase.setProtocolConfiguration(),
        setServerPeerInformation: ISetServerPeerInformation = GivenUsecase.setServerPeerInformation(),
        startConnection: IStartConnection = GivenUsecase.startConnection(context = context),
        startReconnectionHandler: IStartReconnectionHandler = GivenUsecase.startReconnectionHandler(context = context),
        getServerPeerInformation: IGetServerPeerInformation = GivenUsecase.getServerPeerInformation(),
        stopConnection: IStopConnection = GivenUsecase.stopConnection(context = context),
        clearCache: IClearCache = GivenUsecase.clearCache(),
    ): IStartConnectionController = StartConnectionController(
        isServiceCleared = isServiceCleared,
        setProtocolConfiguration = setProtocolConfiguration,
        setServerPeerInformation = setServerPeerInformation,
        startConnection = startConnection,
        startReconnectionHandler = startReconnectionHandler,
        getServerPeerInformation = getServerPeerInformation,
        stopConnection = stopConnection,
        clearCache = clearCache
    )

    fun stopConnectionController(
        context: Context,
        isServicePresent: IIsServicePresent = GivenUsecase.isServicePresent(),
        stopConnection: IStopConnection = GivenUsecase.stopConnection(context = context),
        clearCache: IClearCache = GivenUsecase.clearCache(),
    ): IStopConnectionController =
        StopConnectionController(
            isServicePresent = isServicePresent,
            stopConnection = stopConnection,
            clearCache = clearCache
        )
}
