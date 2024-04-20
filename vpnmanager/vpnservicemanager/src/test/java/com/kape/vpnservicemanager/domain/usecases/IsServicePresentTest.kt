package com.kape.vpnservicemanager.domain.usecases

import com.kape.vpnservicemanager.data.externals.ICacheService
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
internal class IsServicePresentTest {

    @Test
    fun `return no error if service present`() = runTest {
        val cacheServiceMock: ICacheService = CacheMock(
            mockedResponses = mapOf(CacheMock.MethodSignature.GET_SERVICE to GivenExternal.service())
        )
        val isServicePresent: IIsServicePresent = IsServicePresent(
            cacheService = cacheServiceMock
        )
        val result = isServicePresent()

        assert(result.isSuccess)
    }

    @Test
    fun `return error if service is missing`() = runTest {
        val cacheServiceMock: ICacheService = CacheMock(
            mockedResponses = emptyMap()
        )
        val isServicePresent: IIsServicePresent = IsServicePresent(
            cacheService = cacheServiceMock
        )
        val result = isServicePresent()

        assert(result.isFailure)
        assert(result.exceptionOrNull() != null)
    }
}
