package com.kape.vpnprotocol.domain.usecases

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kape.vpnprotocol.domain.usecases.common.ClearCache
import com.kape.vpnprotocol.domain.usecases.common.IClearCache
import com.kape.vpnprotocol.testutils.GivenExternal
import com.kape.vpnprotocol.testutils.GivenModel
import com.kape.vpnprotocol.testutils.mocks.ServiceConfigurationFileDescriptorProviderMock
import com.kape.vpnprotocol.testutils.mocks.VPNProtocolServiceMock
import com.kape.vpnprotocol.testutils.mocks.WireguardKeyPairMock
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
internal class ClearCacheTest {

    @Test
    fun `clear all should clear all wireguard related cache`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val wireguardTunnelHandleMock = 1234
        val wireguardKeyPairMock = WireguardKeyPairMock()
        val wireguardAddKeyResponseMock = GivenModel.wireguardAddKeyResponse()
        val cache = GivenExternal.cache(context = context).apply {
            setWireguardTunnelHandle(tunnelHandle = wireguardTunnelHandleMock)
            setKeyPair(wireguardKeyPair = wireguardKeyPairMock)
            setAddKeyResponse(wireguardAddKeyResponse = wireguardAddKeyResponseMock)
        }
        val clearCache: IClearCache =
            ClearCache(cache = cache)

        // when
        val result = clearCache()

        // then
        assert(result.isSuccess)
        assert(cache.getWireguardTunnelHandle().isFailure)
        assert(cache.getKeyPair().isFailure)
        assert(cache.getAddKeyResponse().isFailure)
    }

    @Test
    fun `clear all should clear all openvpn related cache`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val openVpnGeneratedSettingsMock = listOf("setting1", "setting2")
        val openVpnServerPeerInformationMock = GivenModel.openVpnServerPeerInformation()
        val cache = GivenExternal.cache(context = context).apply {
            setOpenVpnGeneratedSettings(generatedSettings = openVpnGeneratedSettingsMock)
            setOpenVpnServerPeerInformation(openVpnServerPeerInformation = openVpnServerPeerInformationMock)
            createOpenVpnProcessConnectedDeferrable()
        }
        val clearCache: IClearCache =
            ClearCache(cache = cache)

        // when
        val result = clearCache()

        // then
        assert(result.isSuccess)
        assert(cache.getOpenVpnGeneratedSettings().isFailure)
        assert(cache.getOpenVpnServerPeerInformation().isFailure)
        assert(cache.getOpenVpnProcessConnectedDeferrable().isFailure)
    }

    @Test
    fun `clear all should clear all common related cache`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serverPeerInformationMock = GivenModel.vpnProtocolServerPeerInformation()
        val serviceFileDescriptorProviderMock = ServiceConfigurationFileDescriptorProviderMock()
        val cache = GivenExternal.cache(context = context).apply {
            setVpnProtocolService(vpnProtocolService = vpnProtocolServiceMock)
            setProtocolConfiguration(protocolConfiguration = protocolConfigurationMock)
            setProtocolServerPeerInformation(serverPeerInformation = serverPeerInformationMock)
            setServiceFileDescriptorProvider(serviceFileDescriptorProvider = serviceFileDescriptorProviderMock)
        }
        val clearCache: IClearCache =
            ClearCache(cache = cache)

        // when
        val result = clearCache()

        // then
        assert(result.isSuccess)
        assert(cache.getVpnProtocolService().isFailure)
        assert(cache.getProtocolConfiguration().isFailure)
        assert(cache.getProtocolServerPeerInformation().isFailure)
        assert(cache.getServiceFileDescriptorProvider().isFailure)
    }
}
