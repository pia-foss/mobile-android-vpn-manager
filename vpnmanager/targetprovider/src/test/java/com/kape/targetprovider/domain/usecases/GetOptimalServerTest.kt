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

package com.kape.targetprovider.domain.usecases

import com.kape.targetprovider.data.externals.ICache
import com.kape.targetprovider.data.models.TargetProviderServer
import com.kape.targetprovider.data.models.TargetProviderServerList
import com.kape.targetprovider.testutils.GivenExternal
import com.kape.targetprovider.testutils.GivenModel
import com.kape.targetprovider.testutils.GivenUsecase
import kotlinx.coroutines.runBlocking
import org.junit.Test

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

class GetOptimalServerTest {

    @Test
    fun `successfully retrieves the server with the lowest latency when the server list is known`() = runBlocking {
        // given
        val cache: ICache = GivenExternal.cache()
        val setServerList: ISetServerList = SetServerList(cache = cache)
        val getOptimalServer: IGetOptimalServer = GivenUsecase.getOptimalServer(cache = cache)
        val serverList: TargetProviderServerList = GivenModel.serverList(
            servers = listOf(
                TargetProviderServer(ip = "8.8.8.8", latency = 12L),
                TargetProviderServer(ip = "8.8.4.4", latency = 11L),
                TargetProviderServer(ip = "1.1.1.1", latency = 13L)
            )
        )

        // when
        setServerList(serverList = serverList)
        val result = getOptimalServer()

        // then
        assert(result.isSuccess)
        assert(result.getOrThrow().ip == "8.8.4.4")
        assert(result.getOrThrow().latency == 11L)
    }

    @Test
    fun `fail to retrive an optimal server when the server list is unknown`() = runBlocking {
        // given
        val cache: ICache = GivenExternal.cache()
        val getOptimalServer: IGetOptimalServer = GivenUsecase.getOptimalServer(
            cache = cache
        )

        // when
        val result = getOptimalServer()

        // then
        assert(result.isFailure)
    }
}
