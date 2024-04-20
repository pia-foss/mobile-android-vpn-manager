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

package com.kape.vpnmanager.domain.usecases

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kape.vpnmanager.data.externals.IPermissions
import com.kape.vpnmanager.domain.datasources.CacheDatasource
import com.kape.vpnmanager.domain.datasources.ICacheDatasource
import com.kape.vpnmanager.testutils.GivenExternal
import com.kape.vpnmanager.testutils.GivenUsecase
import com.kape.vpnmanager.testutils.mocks.PermissionsMock
import com.kape.vpnmanager.usecases.IGrantPermissions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

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

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
internal class GrantPermissionsTest {

    @Test
    fun `report success if we have permissions`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val permissionsMock: IPermissions =
            PermissionsMock(
                hasVpnPermissionsGranted = true,
                shouldSucceedRequestingPermissions = true
            )
        val grantPermissions: IGrantPermissions = GivenUsecase.grantPermissions(
            context = context,
            permissions = permissionsMock
        )

        // when
        val result = grantPermissions()

        // then
        assert(result.isSuccess)
    }

    @Test
    fun `report failure if we do not have permissions and client declines them`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val permissionsMock: IPermissions =
            PermissionsMock(
                hasVpnPermissionsGranted = false,
                shouldSucceedRequestingPermissions = false
            )
        val grantPermissions: IGrantPermissions = GivenUsecase.grantPermissions(
            context = context,
            permissions = permissionsMock
        )

        // when
        val result = grantPermissions()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `on success updates the 'hasRequiredPermissionsGranted' value on cache`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val permissionsMock: IPermissions = PermissionsMock(
            hasVpnPermissionsGranted = true,
            shouldSucceedRequestingPermissions = true
        )
        val cacheDatasource: ICacheDatasource = CacheDatasource(GivenExternal.cache())
        val grantPermissions: IGrantPermissions = GivenUsecase.grantPermissions(
            context = context,
            permissions = permissionsMock,
            cacheDatasource = cacheDatasource
        )

        // when
        val result = grantPermissions()

        // then
        assert(result.isSuccess)
        assert(cacheDatasource.getState().getOrThrow().hasRequiredPermissionsGranted == true)
    }

    @Test
    fun `on failure updates the 'hasRequiredPermissionsGranted' value on cache`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val permissionsMock: IPermissions = PermissionsMock(
            hasVpnPermissionsGranted = false,
            shouldSucceedRequestingPermissions = false
        )
        val cacheDatasource: ICacheDatasource = CacheDatasource(GivenExternal.cache())
        val grantPermissions: IGrantPermissions = GivenUsecase.grantPermissions(
            context = context,
            permissions = permissionsMock,
            cacheDatasource = cacheDatasource
        )

        // when
        val result = grantPermissions()

        // then
        assert(result.isFailure)
        assert(cacheDatasource.getState().getOrThrow().hasRequiredPermissionsGranted == false)
    }

    @Test
    fun `on failure uses the permissions dependency to request the client for permissions`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val permissionsMock: IPermissions = PermissionsMock(
            hasVpnPermissionsGranted = false,
            shouldSucceedRequestingPermissions = false
        )
        val cacheDatasource: ICacheDatasource = CacheDatasource(GivenExternal.cache())
        val grantPermissions: IGrantPermissions = GivenUsecase.grantPermissions(
            context = context,
            permissions = permissionsMock,
            cacheDatasource = cacheDatasource
        )

        // when
        val result = grantPermissions()

        // then
        assert(result.isFailure)
        assert(cacheDatasource.getState().getOrThrow().hasRequiredPermissionsGranted == false)
        assert((permissionsMock as PermissionsMock).requestVpnPermissionsInvoked == true)
    }
}
