package com.kape.vpnmanager.data.externals

import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.testutils.GivenExternal
import com.kape.vpnmanager.testutils.GivenModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
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
internal class TargetProviderTest {

    @Before
    fun boostrap() {
        // The TargetProviderApi returns on the main thread. Meaning that we can't use
        // the main dispatcher for unit tests as well as they run on `runBlocking` which would
        // deadlock the execution.
        Dispatchers.setMain(Dispatchers.IO)
    }

    @Test
    fun `given a list of servers with known latencies, it returns the server with the lowest latency`() = runTest {
        // given
        val serverList = ServerList(
            servers = listOf(
                GivenModel.server().copy(ip = "1.1.1.1", latency = 11L),
                GivenModel.server().copy(ip = "8.8.4.4", latency = 10L),
                GivenModel.server().copy(ip = "8.8.8.8", latency = 12L)
            )
        )
        val targetProvider: ITargetProvider = GivenExternal.targetProvider()

        // when
        val result = targetProvider.getOptimalServer(serverList = serverList)

        // then
        assert(result.isSuccess)
        assert(result.getOrThrow().ip == "8.8.4.4")
    }

    @Test
    fun `given a list of servers with known and unknown latencies, it returns the server with the lowest latency`() = runTest {
        // given
        val serverList = ServerList(
            servers = listOf(
                GivenModel.server().copy(ip = "1.1.1.1", latency = null),
                GivenModel.server().copy(ip = "4.4.4.4", latency = 10L),
                GivenModel.server().copy(ip = "8.8.4.4", latency = null),
                GivenModel.server().copy(ip = "8.8.8.8", latency = 11L)
            )
        )
        val targetProvider: ITargetProvider = GivenExternal.targetProvider()

        // when
        val result = targetProvider.getOptimalServer(serverList = serverList)

        // then
        assert(result.isSuccess)
        assert(result.getOrThrow().ip == "4.4.4.4")
    }

    @Test
    fun `given a list of servers with unknown latencies, it returns a random server with the lowest latency`() = runTest {
        // given
        val serverList = ServerList(
            servers = listOf(
                GivenModel.server().copy(ip = "1.1.1.1", latency = null),
                GivenModel.server().copy(ip = "4.4.4.4", latency = null),
                GivenModel.server().copy(ip = "8.8.4.4", latency = null),
                GivenModel.server().copy(ip = "8.8.8.8", latency = null)
            )
        )
        val targetProvider: ITargetProvider = GivenExternal.targetProvider()

        // when
        val result = targetProvider.getOptimalServer(serverList = serverList)

        // then
        assert(result.isSuccess)
    }
}
