package com.kape.vpnservicemanager.domain.usecases

import com.kape.vpnservicemanager.data.externals.ICacheService
import com.kape.vpnservicemanager.presenters.VPNServiceManagerError
import com.kape.vpnservicemanager.presenters.VPNServiceManagerErrorCode

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

internal class IsServiceCleared(
    private val cacheService: ICacheService,
) : IIsServiceCleared {

    // region IIsServiceCleared
    override suspend fun invoke(): Result<Unit> =
        if (cacheService.getService().isSuccess) {
            Result.failure(
                VPNServiceManagerError(
                    code = VPNServiceManagerErrorCode.KNOWN_SERVICE_PRESENT,
                    error = Error("Service running. Please stop the service first.")
                )
            )
        } else {
            Result.success(Unit)
        }
    // endregion
}
