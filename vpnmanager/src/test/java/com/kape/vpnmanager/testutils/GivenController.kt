package com.kape.vpnmanager.testutils

import android.content.Context
import com.kape.vpnmanager.domain.controllers.IStartConnectionController
import com.kape.vpnmanager.domain.controllers.StartConnectionController
import com.kape.vpnmanager.usecases.IGetServerList
import com.kape.vpnmanager.usecases.IGrantPermissions
import com.kape.vpnmanager.usecases.ISetClientConfiguration
import com.kape.vpnmanager.usecases.IStartIteratingConnection

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
        grantPermissions: IGrantPermissions = GivenUsecase.grantPermissions(context = context),
        setClientConfiguration: ISetClientConfiguration = GivenUsecase.setClientConfiguration(),
        getServerList: IGetServerList = GivenUsecase.getServerList(),
        startIteratingConnection: IStartIteratingConnection = GivenUsecase.startIteratingConnection(),
    ): IStartConnectionController =
        StartConnectionController(
            grantPermissions = grantPermissions,
            getServerList = getServerList,
            setClientConfiguration = setClientConfiguration,
            startIteratingConnection = startIteratingConnection
        )
}
