package com.kape.vpnmanager.usecases

import com.kape.vpnmanager.data.externals.IPermissions
import com.kape.vpnmanager.domain.datasources.ICacheDatasource

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
 * It checks whether the required permissions for a vpn connection are granted.
 */
internal class GrantPermissions(
    private val permissions: IPermissions,
    private val cacheDatasource: ICacheDatasource,
) : IGrantPermissions {

    // region IGrantPermissions
    override suspend operator fun invoke(): Result<Unit> {
        permissions.hasVpnPermissionsGranted().fold(
            onSuccess = {
                cacheDatasource.setHasRequiredPermissionsGranted(true)
                return Result.success(Unit)
            },
            onFailure = {
                val grantedResult = permissions.requestVpnPermissions()
                cacheDatasource.setHasRequiredPermissionsGranted(grantedResult.isSuccess)
                return grantedResult
            }
        )
    }
    // endregion
}
