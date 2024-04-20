package com.kape.vpnmanager.usecases

import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.domain.datasources.ICacheDatasource
import com.kape.vpnmanager.presenters.VPNManagerError
import com.kape.vpnmanager.presenters.VPNManagerErrorCode

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

internal class GetServerList(
    private val cacheDatasource: ICacheDatasource,
) : IGetServerList {

    // region IGetServerList
    override suspend fun invoke(): Result<ServerList> =
        cacheDatasource.getState().mapCatching {
            it.configuration.clientConfiguration?.serverList
                ?: throw VPNManagerError(code = VPNManagerErrorCode.FAILED_SERVER_SELECTION)
        }
    // endregion
}
