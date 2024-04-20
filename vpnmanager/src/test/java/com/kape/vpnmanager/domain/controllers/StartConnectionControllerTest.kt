package com.kape.vpnmanager.domain.controllers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kape.vpnmanager.testutils.GivenController
import com.kape.vpnmanager.testutils.GivenModel
import com.kape.vpnmanager.testutils.mocks.GetServerListMock
import com.kape.vpnmanager.testutils.mocks.GrantPermissionsMock
import com.kape.vpnmanager.testutils.mocks.SetClientConfigurationMock
import com.kape.vpnmanager.testutils.mocks.StartIteratingConnectionMock
import com.kape.vpnmanager.usecases.IGetServerList
import com.kape.vpnmanager.usecases.IGrantPermissions
import com.kape.vpnmanager.usecases.ISetClientConfiguration
import com.kape.vpnmanager.usecases.IStartIteratingConnection
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
class StartConnectionControllerTest {

    @Test
    fun `successfully start connection with a valid set of pre requirements`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val clientConfiguration = GivenModel.clientConfiguration(context)
        val grantPermissionsMock: IGrantPermissions = GrantPermissionsMock(
            shouldSucceed = true
        )
        val setClientConfigurationMock: ISetClientConfiguration = SetClientConfigurationMock(
            shouldSucceed = true
        )
        val getServerListMock: IGetServerList = GetServerListMock(
            shouldSucceed = true
        )
        val startIteratingConnection: IStartIteratingConnection = StartIteratingConnectionMock(
            shouldSucceed = true
        )
        val startConnectionController: IStartConnectionController =
            GivenController.startConnectionController(
                context = context,
                grantPermissions = grantPermissionsMock,
                setClientConfiguration = setClientConfigurationMock,
                getServerList = getServerListMock,
                startIteratingConnection = startIteratingConnection
            )

        // when
        val result = startConnectionController(clientConfiguration = clientConfiguration)

        // then
        assert(result.isSuccess)
    }

    @Test
    fun `fail start connection when there are no permissions granted`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val clientConfiguration = GivenModel.clientConfiguration(context)
        val grantPermissionsMock: IGrantPermissions = GrantPermissionsMock(
            shouldSucceed = false
        )
        val setClientConfigurationMock: ISetClientConfiguration = SetClientConfigurationMock(
            shouldSucceed = true
        )
        val getServerListMock: IGetServerList = GetServerListMock(
            shouldSucceed = true
        )
        val startConnectionController: IStartConnectionController =
            GivenController.startConnectionController(
                context = context,
                grantPermissions = grantPermissionsMock,
                getServerList = getServerListMock,
                setClientConfiguration = setClientConfigurationMock
            )

        // when
        val result = startConnectionController(clientConfiguration = clientConfiguration)

        // then
        assert(result.isFailure)
    }

    @Test
    fun `fail start connection when get server list fails`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val clientConfiguration = GivenModel.clientConfiguration(context)
        val grantPermissionsMock: IGrantPermissions = GrantPermissionsMock(
            shouldSucceed = true
        )
        val setClientConfigurationMock: ISetClientConfiguration = SetClientConfigurationMock(
            shouldSucceed = true
        )
        val getServerListMock: IGetServerList = GetServerListMock(
            shouldSucceed = false
        )
        val startConnectionController: IStartConnectionController =
            GivenController.startConnectionController(
                context = context,
                grantPermissions = grantPermissionsMock,
                getServerList = getServerListMock,
                setClientConfiguration = setClientConfigurationMock
            )

        // when
        val result = startConnectionController(clientConfiguration = clientConfiguration)

        // then
        assert(result.isFailure)
    }

    @Test
    fun `fail start connection when no client configuration was provided`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val clientConfiguration = GivenModel.clientConfiguration(context)
        val grantPermissionsMock: IGrantPermissions = GrantPermissionsMock(
            shouldSucceed = true
        )
        val setClientConfigurationMock: ISetClientConfiguration = SetClientConfigurationMock(
            shouldSucceed = false
        )
        val getServerListMock: IGetServerList = GetServerListMock(
            shouldSucceed = true
        )
        val startConnectionController: IStartConnectionController =
            GivenController.startConnectionController(
                context = context,
                grantPermissions = grantPermissionsMock,
                getServerList = getServerListMock,
                setClientConfiguration = setClientConfigurationMock
            )

        // when
        val result = startConnectionController(clientConfiguration = clientConfiguration)

        // then
        assert(result.isFailure)
    }
}
