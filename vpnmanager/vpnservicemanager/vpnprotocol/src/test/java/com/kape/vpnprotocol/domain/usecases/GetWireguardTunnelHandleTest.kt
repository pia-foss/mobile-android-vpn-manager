package com.kape.vpnprotocol.domain.usecases

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kape.vpnprotocol.data.externals.wireguard.ICacheWireguard
import com.kape.vpnprotocol.domain.usecases.wireguard.GetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.IGetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.ISetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.SetWireguardTunnelHandle
import com.kape.vpnprotocol.testutils.GivenExternal
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
internal class GetWireguardTunnelHandleTest {

    @Test
    fun `should succeed retrieving from cache the set wireguard tunnel handle information`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val wireguardTunnelHandleMock = 1234
        val cacheWireguard: ICacheWireguard = GivenExternal.cache(context = context)
        val setWireguardTunnelHandle: ISetWireguardTunnelHandle =
            SetWireguardTunnelHandle(cacheWireguard = cacheWireguard)
        val getWireguardTunnelHandle: IGetWireguardTunnelHandle =
            GetWireguardTunnelHandle(cacheWireguard = cacheWireguard)

        // when
        val setResult = setWireguardTunnelHandle(tunnelHandle = wireguardTunnelHandleMock)
        val getResult = getWireguardTunnelHandle()

        // then
        assert(setResult.isSuccess)
        assert(getResult.isSuccess)
        assert(getResult.getOrThrow() == wireguardTunnelHandleMock)
    }

    @Test
    fun `should fail retrieving from cache when there is no wireguard tunnel handle information`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val cacheWireguard: ICacheWireguard = GivenExternal.cache(context = context)
        val getWireguardTunnelHandle: IGetWireguardTunnelHandle =
            GetWireguardTunnelHandle(cacheWireguard = cacheWireguard)

        // when
        val result = getWireguardTunnelHandle()

        // then
        assert(result.isFailure)
    }
}
