package com.kape.vpnservicemanager.domain.usecases

import com.kape.vpnservicemanager.data.externals.ICacheService
import com.kape.vpnservicemanager.presenters.VPNServiceManagerError
import com.kape.vpnservicemanager.presenters.VPNServiceManagerErrorCode
import com.kape.vpnservicemanager.testutils.GivenExternal
import com.kape.vpnservicemanager.testutils.mocks.CacheMock
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
internal class IsServiceClearedTest {

    @Test
    fun `return proper error if clearing fails`() = runTest {
        // given
        val cacheServiceMock: ICacheService = CacheMock(
            mockedResponses = mapOf(CacheMock.MethodSignature.GET_SERVICE to GivenExternal.service())
        )
        val isServiceCleared: IIsServiceCleared = IsServiceCleared(
            cacheService = cacheServiceMock
        )

        // when
        val result = isServiceCleared()

        // then
        assert(result.isFailure)
        assert((result.exceptionOrNull() as VPNServiceManagerError).code == VPNServiceManagerErrorCode.KNOWN_SERVICE_PRESENT)
    }
}
