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

import com.kape.vpnmanager.domain.datasources.ICacheDatasource
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.kape.vpnmanager.testutils.GivenGateway
import com.kape.vpnmanager.testutils.GivenUsecase
import com.kape.vpnmanager.testutils.mocks.VPNManagerConnectionListenerMock
import com.kape.vpnmanager.usecases.IAddConnectionListener
import com.kape.vpnmanager.usecases.IRemoveConnectionListener
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
class RemoveConnectionListenerTest {

    @Test
    fun `successfully remove an added connection listener`() = runTest {
        // given
        val connectionListenerMock: VPNManagerConnectionListener = VPNManagerConnectionListenerMock()
        val cacheDatasource: ICacheDatasource = GivenGateway.cacheDatasource()
        val addConnectionListener: IAddConnectionListener = GivenUsecase.addConnectionListener(
            cacheDatasource = cacheDatasource
        )
        val removeConnectionListener: IRemoveConnectionListener = GivenUsecase.removeConnectionListener(
            cacheDatasource = cacheDatasource
        )

        // when
        val addResult = addConnectionListener(connectionListenerMock)
        val removeResult = removeConnectionListener(connectionListenerMock)

        // then
        assert(addResult.isSuccess)
        assert(removeResult.isSuccess)
        assert(cacheDatasource.getConnectionListeners().getOrThrow().isEmpty())
    }

    @Test
    fun `successfully remove only the instance being passed`() = runTest {
        // given
        val firstConnectionListenerMock: VPNManagerConnectionListener = VPNManagerConnectionListenerMock()
        val secondConnectionListenerMock: VPNManagerConnectionListener = VPNManagerConnectionListenerMock()
        val cacheDatasource: ICacheDatasource = GivenGateway.cacheDatasource()
        val addConnectionListener: IAddConnectionListener = GivenUsecase.addConnectionListener(
            cacheDatasource = cacheDatasource
        )
        val removeConnectionListener: IRemoveConnectionListener = GivenUsecase.removeConnectionListener(
            cacheDatasource = cacheDatasource
        )

        // when
        val firstAddResult = addConnectionListener(firstConnectionListenerMock)
        val secondAddResult = addConnectionListener(secondConnectionListenerMock)
        val removeResult = removeConnectionListener(firstConnectionListenerMock)

        // then
        assert(firstAddResult.isSuccess)
        assert(secondAddResult.isSuccess)
        assert(removeResult.isSuccess)
        assert(cacheDatasource.getConnectionListeners().getOrThrow().size == 1)
    }

    @Test
    fun `successfully remove all instances when the same instance is present more than once`() = runTest {
        // given
        val connectionListenerMock: VPNManagerConnectionListener = VPNManagerConnectionListenerMock()
        val cacheDatasource: ICacheDatasource = GivenGateway.cacheDatasource()
        val addConnectionListener: IAddConnectionListener = GivenUsecase.addConnectionListener(
            cacheDatasource = cacheDatasource
        )
        val removeConnectionListener: IRemoveConnectionListener = GivenUsecase.removeConnectionListener(
            cacheDatasource = cacheDatasource
        )

        // when
        val firstAddResult = addConnectionListener(connectionListenerMock)
        val secondAddResult = addConnectionListener(connectionListenerMock)
        val removeResult = removeConnectionListener(connectionListenerMock)

        // then
        assert(firstAddResult.isSuccess)
        assert(secondAddResult.isSuccess)
        assert(removeResult.isSuccess)
        assert(cacheDatasource.getConnectionListeners().getOrThrow().isEmpty())
    }
}
