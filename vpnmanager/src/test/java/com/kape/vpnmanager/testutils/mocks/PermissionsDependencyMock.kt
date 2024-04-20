package com.kape.vpnmanager.testutils.mocks

import com.kape.vpnmanager.presenters.VPNManagerError
import com.kape.vpnmanager.presenters.VPNManagerErrorCode
import com.kape.vpnmanager.presenters.VPNManagerPermissionsDependency
import com.kape.vpnmanager.presenters.VPNManagerResultCallback

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

class PermissionsDependencyMock(val shouldSucceed: Boolean) : VPNManagerPermissionsDependency {
    override fun requestNecessaryPermissions(callback: VPNManagerResultCallback<Boolean>) {
        if (shouldSucceed) {
            callback(Result.success(shouldSucceed))
        } else {
            callback(Result.failure(VPNManagerError(VPNManagerErrorCode.PERMISSIONS_NOT_GRANTED)))
        }
    }
}
