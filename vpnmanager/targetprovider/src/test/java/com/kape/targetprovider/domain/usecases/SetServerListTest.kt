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

class SetServerListTest {

    @Test
    fun `successfully set the list of servers in cache`() = runBlocking {
        // given
        val cache: ICache = GivenExternal.cache()
        val setServerList: ISetServerList = SetServerList(cache = cache)
        val serverList: TargetProviderServerList = GivenModel.serverList(
            servers = listOf(
                TargetProviderServer(ip = "8.8.8.8", latency = 12L),
                TargetProviderServer(ip = "8.8.4.4", latency = 11L),
                TargetProviderServer(ip = "1.1.1.1", latency = 13L)
            )
        )

        // when
        val result = setServerList(serverList = serverList)

        // then
        assert(result.isSuccess)
        assert(cache.getServerList().getOrThrow().servers.first().ip == "8.8.8.8")
        assert(cache.getServerList().getOrThrow().servers.first().latency == 12L)
    }
}
