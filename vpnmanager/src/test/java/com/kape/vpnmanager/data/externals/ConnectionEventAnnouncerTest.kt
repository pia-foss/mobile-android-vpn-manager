package com.kape.vpnmanager.data.externals

import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnmanager.data.models.TransportProtocol
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget
import com.kape.vpnmanager.testutils.GivenExternal
import com.kape.vpnmanager.testutils.mocks.VPNManagerConnectionListenerMock
import io.mockk.mockk
import io.mockk.verifySequence
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
class ConnectionEventAnnouncerTest {

    @Test
    fun `announcing a connection status change must be received by its listeners`() = runTest {
        // given
        val firstConnectionListenerMock: VPNManagerConnectionListener = VPNManagerConnectionListenerMock()
        val lastConnectionListenerMock: VPNManagerConnectionListener = VPNManagerConnectionListenerMock()
        val connectionListeners: List<VPNManagerConnectionListener> = listOf(
            firstConnectionListenerMock,
            lastConnectionListenerMock
        )
        val cache: ICache = GivenExternal.cache()
        val connectionEventAnnouncer: IConnectionEventAnnouncer =
            GivenExternal.connectionEventAnnouncer(cache = cache)

        // when
        cache.setConnectionListeners(connectionListeners)
        connectionEventAnnouncer.handleConnectivityStatusChange(VPNManagerConnectionStatus.Connected())

        // then
        assert((firstConnectionListenerMock as VPNManagerConnectionListenerMock).invocationsCounter == 1)
        assert((lastConnectionListenerMock as VPNManagerConnectionListenerMock).invocationsCounter == 1)
    }

    @Test
    fun `announcing a server connect success must be received by its listeners`() = runTest {
        // given
        val firstConnectionListenerMock: VPNManagerConnectionListener = mockk(relaxed = true)
        val lastConnectionListenerMock: VPNManagerConnectionListener = mockk(relaxed = true)
        val connectionListeners: List<VPNManagerConnectionListener> = listOf(
            firstConnectionListenerMock,
            lastConnectionListenerMock
        )
        val cache: ICache = GivenExternal.cache()
        val connectionEventAnnouncer: IConnectionEventAnnouncer =
            GivenExternal.connectionEventAnnouncer(cache = cache)

        // when
        cache.setConnectionListeners(connectionListeners)
        val serverIp = "1.2.3.4"
        val transportMode = TransportProtocol.TCP
        val vpnProtocol = VPNManagerProtocolTarget.WIREGUARD
        connectionEventAnnouncer.handleServerConnectAttemptSucceeded(serverIp, transportMode, vpnProtocol)

        // then
        verifySequence {
            firstConnectionListenerMock.handleServerConnectAttemptSucceeded(serverIp, transportMode, vpnProtocol)
            lastConnectionListenerMock.handleServerConnectAttemptSucceeded(serverIp, transportMode, vpnProtocol)
        }
    }

    @Test
    fun `announcing a server connect failure must be received by its listeners`() = runTest {
        // given
        val firstConnectionListenerMock: VPNManagerConnectionListener = mockk(relaxed = true)
        val lastConnectionListenerMock: VPNManagerConnectionListener = mockk(relaxed = true)
        val connectionListeners: List<VPNManagerConnectionListener> = listOf(
            firstConnectionListenerMock,
            lastConnectionListenerMock
        )
        val cache: ICache = GivenExternal.cache()
        val connectionEventAnnouncer: IConnectionEventAnnouncer =
            GivenExternal.connectionEventAnnouncer(cache = cache)

        // when
        cache.setConnectionListeners(connectionListeners)
        val serverIp = "1.2.3.4"
        val transportMode = TransportProtocol.UDP
        val vpnProtocol = VPNManagerProtocolTarget.OPENVPN
        val throwable = Throwable()
        connectionEventAnnouncer.handleServerConnectAttemptFailed(serverIp, transportMode, vpnProtocol, throwable)

        // then
        verifySequence {
            firstConnectionListenerMock.handleServerConnectAttemptFailed(serverIp, transportMode, vpnProtocol, throwable)
            lastConnectionListenerMock.handleServerConnectAttemptFailed(serverIp, transportMode, vpnProtocol, throwable)
        }
    }
}
