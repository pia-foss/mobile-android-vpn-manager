package com.kape.vpnprotocol.domain.usecases

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kape.vpnprotocol.data.externals.wireguard.IWireguard
import com.kape.vpnprotocol.domain.usecases.wireguard.CreateWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.ICreateWireguardTunnel
import com.kape.vpnprotocol.testutils.GivenExternal
import com.kape.vpnprotocol.testutils.GivenModel
import com.kape.vpnprotocol.testutils.mocks.ServiceConfigurationFileDescriptorProviderMock
import com.kape.vpnprotocol.testutils.mocks.WireguardApiMock
import com.kape.wireguard.presenters.WireguardAPI
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
internal class CreateWireguardTunnelTest {

    @Test
    fun `should access the turn on wireguard module api when creating the tunnel`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val wireguardApiMock: WireguardAPI = WireguardApiMock()
        val generatedSettingsMock = "settings"
        val wireguardAddKeyResponseMock = GivenModel.wireguardAddKeyResponse()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceFileDescriptorProviderMock = ServiceConfigurationFileDescriptorProviderMock()
        val cache = GivenExternal.cache(context = context).apply {
            setAddKeyResponse(wireguardAddKeyResponse = wireguardAddKeyResponseMock)
            setProtocolConfiguration(protocolConfiguration = protocolConfigurationMock)
            setServiceFileDescriptorProvider(serviceFileDescriptorProvider = serviceFileDescriptorProviderMock)
        }
        val wireguard: IWireguard = GivenExternal.wireguard(wireguardApi = wireguardApiMock)
        val createWireguardTunnel: ICreateWireguardTunnel =
            CreateWireguardTunnel(
                cacheProtocol = cache,
                cacheKeys = cache,
                cacheService = cache,
                wireguard = wireguard
            )

        // when
        val result = createWireguardTunnel(generatedSettings = generatedSettingsMock)

        // then
        assert(result.isSuccess)
        assert((wireguardApiMock as WireguardApiMock).invocationsPerformed[WireguardApiMock.MethodSignature.TURN_ON] == 1)
    }
}
