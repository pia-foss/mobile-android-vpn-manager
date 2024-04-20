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

package com.kape.vpnmanager.domain.controllers

import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.ServerPeerInformation
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

internal class StartConnectionController(
    private val grantPermissions: IGrantPermissions,
    private val setClientConfiguration: ISetClientConfiguration,
    private val getServerList: IGetServerList,
    private val startIteratingConnection: IStartIteratingConnection,
) : IStartConnectionController {

    // region IStartConnectionController
    override suspend fun invoke(
        clientConfiguration: ClientConfiguration,
    ): Result<ServerPeerInformation> =
        grantPermissions()
            .mapCatching {
                setClientConfiguration(clientConfiguration = clientConfiguration).getOrThrow()
            }
            .mapCatching {
                getServerList().getOrThrow()
            }
            .mapCatching {
                startIteratingConnection(
                    serverList = it,
                    vpnProtocol = clientConfiguration.protocolTarget
                ).getOrThrow()
            }
    // endregion
}
