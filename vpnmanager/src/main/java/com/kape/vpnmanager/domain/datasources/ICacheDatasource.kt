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

package com.kape.vpnmanager.domain.datasources

import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.data.models.State

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

internal interface ICacheDatasource : ICacheLoggingDatasource, ICacheConnectionListenerDatasource {

    /**
     * @return `Result<State>`.
     */
    fun getState(): Result<State>

    /**
     * @param server `ServerList.Server`.
     *
     * @return `Result<Unit>`.
     */
    fun setServer(server: ServerList.Server): Result<Unit>

    /**
     * @param clientConfiguration `ClientConfiguration`.
     *
     * @return `Result<State>`.
     */
    fun setClientConfiguration(clientConfiguration: ClientConfiguration): Result<Unit>

    /**
     * @param hasRequiredPermissionsGranted `Boolean`.
     *
     * @return `Result<State>`.
     */
    fun setHasRequiredPermissionsGranted(hasRequiredPermissionsGranted: Boolean): Result<Unit>
}
