package com.kape.vpnmanager.testutils

import com.kape.vpnmanager.presenters.VPNManagerDebugLoggingDependency
import com.kape.vpnmanager.presenters.VPNManagerPermissionsDependency
import com.kape.vpnmanager.presenters.VPNManagerProtocolByteCountDependency
import com.kape.vpnmanager.testutils.mocks.DebugDependencyMock
import com.kape.vpnmanager.testutils.mocks.PermissionsDependencyMock
import com.kape.vpnmanager.testutils.mocks.ProtocolByteCountDependencyMock

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

internal object GivenClientDependency {

    fun protocolByteCountDependency(): VPNManagerProtocolByteCountDependency =
        ProtocolByteCountDependencyMock()

    fun debugLoggingDependency(): VPNManagerDebugLoggingDependency =
        DebugDependencyMock()

    fun permissionsDependency(shouldSucceed: Boolean = true): VPNManagerPermissionsDependency =
        PermissionsDependencyMock(shouldSucceed = shouldSucceed)
}
