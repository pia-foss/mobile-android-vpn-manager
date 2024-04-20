package com.kape.vpnmanager.domain.datasources

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.kape.vpnmanager.testutils.GivenGateway
import com.kape.vpnmanager.testutils.GivenModel
import com.kape.vpnmanager.testutils.mocks.VPNManagerConnectionListenerMock
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
internal class CacheDatasourceTest {

    @Test
    fun `when setting the server the state of the world should be updated with it`() = runTest {
        // given
        val server: ServerList.Server = GivenModel.server(
            ip = "8.8.8.8",
            commonOrDistinguishedName = "madrid401"
        )
        val cacheDatasource: ICacheDatasource = GivenGateway.cacheDatasource()

        // when
        val result = cacheDatasource.setServer(server)

        // then
        assert(result.isSuccess)
        assert(cacheDatasource.getState().getOrThrow().configuration.server == server)
    }

    @Test
    fun `when setting the client configuration the state of the world should be updated with it`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val clientConfiguration: ClientConfiguration = GivenModel.clientConfiguration(
            context = context,
            mtu = 1000,
            allowedApplicationPackages = listOf("com.kape.android")
        )
        val cacheDatasource: ICacheDatasource = GivenGateway.cacheDatasource()

        // when
        val result = cacheDatasource.setClientConfiguration(clientConfiguration)

        // then
        assert(result.isSuccess)
        assert(cacheDatasource.getState().getOrThrow().configuration.clientConfiguration == clientConfiguration)
    }

    @Test
    fun `when setting the granted permissions flag to true the state of the world should be updated with it`() = runTest {
        // given
        val cacheDatasource: ICacheDatasource = GivenGateway.cacheDatasource()
        val hasRequiredPermissionsGranted = true

        // when
        val result = cacheDatasource.setHasRequiredPermissionsGranted(hasRequiredPermissionsGranted)

        // then
        assert(result.isSuccess)
        assert(cacheDatasource.getState().getOrThrow().hasRequiredPermissionsGranted == hasRequiredPermissionsGranted)
    }

    @Test
    fun `when setting the granted permissions flag to false the state of the world should be updated with it`() = runTest {
        // given
        val cacheDatasource: ICacheDatasource = GivenGateway.cacheDatasource()
        val hasRequiredPermissionsGranted = false

        // when
        val result = cacheDatasource.setHasRequiredPermissionsGranted(hasRequiredPermissionsGranted)

        // then
        assert(result.isSuccess)
        assert(cacheDatasource.getState().getOrThrow().hasRequiredPermissionsGranted == hasRequiredPermissionsGranted)
    }

    @Test
    fun `listeners cached via its setter must be retrieved via its getter`() = runTest {
        // given
        val cacheDatasource: ICacheDatasource = GivenGateway.cacheDatasource()
        val firstConnectionListenerMock: VPNManagerConnectionListener = VPNManagerConnectionListenerMock()
        val lastConnectionListenerMock: VPNManagerConnectionListener = VPNManagerConnectionListenerMock()
        val connectionListeners: List<VPNManagerConnectionListener> = listOf(
            firstConnectionListenerMock,
            lastConnectionListenerMock
        )

        // when
        val result = cacheDatasource.setConnectionListeners(connectionListeners = connectionListeners)

        // then
        assert(result.isSuccess)
        assert(cacheDatasource.getConnectionListeners().getOrThrow().size == 2)
        assert(cacheDatasource.getConnectionListeners().getOrThrow().first() == firstConnectionListenerMock)
        assert(cacheDatasource.getConnectionListeners().getOrThrow().last() == lastConnectionListenerMock)
    }
}
