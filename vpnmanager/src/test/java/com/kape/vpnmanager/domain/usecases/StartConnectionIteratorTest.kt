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

import com.kape.vpnmanager.data.externals.IConnectionEventAnnouncer
import com.kape.vpnmanager.data.externals.IServiceManager
import com.kape.vpnmanager.data.externals.ITargetProvider
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.data.models.TransportProtocol
import com.kape.vpnmanager.presenters.VPNManagerError
import com.kape.vpnmanager.presenters.VPNManagerErrorCode
import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget
import com.kape.vpnmanager.testutils.GivenExternal
import com.kape.vpnmanager.testutils.GivenModel
import com.kape.vpnmanager.testutils.GivenUsecase
import com.kape.vpnmanager.testutils.mocks.ServiceManagerMock
import com.kape.vpnmanager.usecases.ISetServer
import com.kape.vpnmanager.usecases.IStartConnection
import com.kape.vpnmanager.usecases.IStartIteratingConnection
import com.kape.vpnmanager.usecases.StartConnection
import com.kape.vpnmanager.usecases.StartIteratingConnection
import io.mockk.mockk
import io.mockk.verifySequence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
internal class StartConnectionIteratorTest {

    @Before
    fun boostrap() {
        // The TargetProviderApi returns on the main thread. Meaning that we can't use
        // the main dispatcher for unit tests as well as they run on `runBlocking` which would
        // deadlock the execution.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `should succeed if the service is started successfully`() = runTest {
        // given
        val serverList = ServerList(
            servers = listOf(
                GivenModel.server().copy(ip = "1.1.1.1", latency = 11L),
                GivenModel.server().copy(ip = "8.8.4.4", latency = 10L),
                GivenModel.server().copy(ip = "8.8.8.8", latency = 12L)
            )
        )
        val targetProvider: ITargetProvider = GivenExternal.targetProvider()
        val serviceManagerMock: IServiceManager =
            ServiceManagerMock(shouldSucceedOnStartAttemptNumber = 0)
        val startConnection: IStartConnection = StartConnection(
            serviceManager = serviceManagerMock
        )
        val setServer: ISetServer = GivenUsecase.setServer()
        val connectionEventAnnouncer = GivenExternal.connectionEventAnnouncer()
        val coroutineContext = GivenExternal.coroutineContext()
        val startIteratingConnection: IStartIteratingConnection = StartIteratingConnection(
            targetProvider = targetProvider,
            startConnection = startConnection,
            setServer = setServer,
            connectionEventAnnouncer = connectionEventAnnouncer,
            coroutineContext = coroutineContext
        )

        // when
        val result = startIteratingConnection(
            serverList = serverList,
            vpnProtocol = VPNManagerProtocolTarget.OPENVPN
        )

        // then
        assert(result.isSuccess)
    }

    @Test
    fun `should succeed on a second server if the first one failed`() = runTest {
        // given
        val serverList = ServerList(
            servers = listOf(
                GivenModel.server().copy(ip = "1.1.1.1", latency = 11L),
                GivenModel.server().copy(ip = "8.8.4.4", latency = 10L),
                GivenModel.server().copy(ip = "8.8.8.8", latency = 12L)
            )
        )
        val targetProvider: ITargetProvider = GivenExternal.targetProvider()
        val serviceManagerMock: IServiceManager =
            ServiceManagerMock(shouldSucceedOnStartAttemptNumber = 1)
        val startConnection: IStartConnection = StartConnection(
            serviceManager = serviceManagerMock
        )
        val setServer: ISetServer = GivenUsecase.setServer()
        val connectionEventAnnouncer = GivenExternal.connectionEventAnnouncer()
        val coroutineContext = GivenExternal.coroutineContext()
        val startIteratingConnection: IStartIteratingConnection = StartIteratingConnection(
            targetProvider = targetProvider,
            startConnection = startConnection,
            setServer = setServer,
            connectionEventAnnouncer = connectionEventAnnouncer,
            coroutineContext = coroutineContext
        )

        // when
        val result = startIteratingConnection(
            serverList = serverList,
            vpnProtocol = VPNManagerProtocolTarget.OPENVPN
        )

        // then
        assert(result.isSuccess)
        assert((serviceManagerMock as ServiceManagerMock).startInvocationsCounter == 1)
    }

    @Test
    fun `should succeed on a third server if the first two failed`() = runTest {
        // given
        val serverList = ServerList(
            servers = listOf(
                GivenModel.server().copy(ip = "1.1.1.1", latency = 11L),
                GivenModel.server().copy(ip = "8.8.4.4", latency = 10L),
                GivenModel.server().copy(ip = "8.8.8.8", latency = 12L)
            )
        )
        val targetProvider: ITargetProvider = GivenExternal.targetProvider()
        val serviceManagerMock: IServiceManager =
            ServiceManagerMock(shouldSucceedOnStartAttemptNumber = 2)
        val startConnection: IStartConnection = StartConnection(
            serviceManager = serviceManagerMock
        )
        val setServer: ISetServer = GivenUsecase.setServer()
        val connectionEventAnnouncer = GivenExternal.connectionEventAnnouncer()
        val coroutineContext = GivenExternal.coroutineContext()
        val startIteratingConnection: IStartIteratingConnection = StartIteratingConnection(
            targetProvider = targetProvider,
            startConnection = startConnection,
            setServer = setServer,
            connectionEventAnnouncer = connectionEventAnnouncer,
            coroutineContext = coroutineContext
        )

        // when
        val result = startIteratingConnection(
            serverList = serverList,
            vpnProtocol = VPNManagerProtocolTarget.OPENVPN
        )

        // then
        assert(result.isSuccess)
        assert((serviceManagerMock as ServiceManagerMock).startInvocationsCounter == 2)
    }

    @Test
    fun `should go through the full list of regions when the start of the service fails`() = runTest {
        // given
        val serverList = ServerList(
            servers = listOf(
                GivenModel.server().copy(ip = "1.1.1.1", latency = 11L),
                GivenModel.server().copy(ip = "8.8.4.4", latency = 10L),
                GivenModel.server().copy(ip = "8.8.8.8", latency = 12L),
                GivenModel.server().copy(ip = "4.4.4.4", latency = 13L)
            )
        )
        val targetProvider: ITargetProvider = GivenExternal.targetProvider()
        val serviceManagerMock: IServiceManager =
            ServiceManagerMock(shouldSucceedOnStartAttemptNumber = 9999)
        val startConnection: IStartConnection = StartConnection(
            serviceManager = serviceManagerMock
        )
        val setServer: ISetServer = GivenUsecase.setServer()
        val connectionEventAnnouncer = GivenExternal.connectionEventAnnouncer()
        val coroutineContext = GivenExternal.coroutineContext()
        val startIteratingConnection: IStartIteratingConnection = StartIteratingConnection(
            targetProvider = targetProvider,
            startConnection = startConnection,
            setServer = setServer,
            connectionEventAnnouncer = connectionEventAnnouncer,
            coroutineContext = coroutineContext
        )

        // when
        val result = startIteratingConnection(
            serverList = serverList,
            vpnProtocol = VPNManagerProtocolTarget.OPENVPN
        )

        // then
        assert(result.isFailure)
        assert((serviceManagerMock as ServiceManagerMock).startInvocationsCounter == 4)
    }

    @Test
    fun `handleServerConnectFailed is called for each server if all connection attempts fail`() = runTest {
        // given
        val serverList = ServerList(
            servers = listOf(
                GivenModel.server().copy(ip = "1.1.1.1", latency = 11L),
                GivenModel.server().copy(ip = "8.8.4.4", latency = 10L),
                GivenModel.server().copy(ip = "8.8.8.8", latency = 12L),
                GivenModel.server().copy(ip = "4.4.4.4", latency = 13L)
            )
        )
        val targetProvider: ITargetProvider = GivenExternal.targetProvider()
        val serviceManagerMock: IServiceManager =
            ServiceManagerMock(shouldSucceedOnStartAttemptNumber = 9999)
        val startConnection: IStartConnection = StartConnection(
            serviceManager = serviceManagerMock
        )
        val setServer: ISetServer = GivenUsecase.setServer()
        val connectionEventAnnouncer: IConnectionEventAnnouncer = mockk(relaxed = true)
        val coroutineContext = GivenExternal.coroutineContext()
        val startIteratingConnection: IStartIteratingConnection = StartIteratingConnection(
            targetProvider = targetProvider,
            startConnection = startConnection,
            setServer = setServer,
            connectionEventAnnouncer = connectionEventAnnouncer,
            coroutineContext = coroutineContext
        )

        // when
        startIteratingConnection(
            serverList = serverList,
            vpnProtocol = VPNManagerProtocolTarget.OPENVPN
        )

        // then
        verifySequence {
            connectionEventAnnouncer.handleServerConnectAttemptFailed(
                serverIp = "8.8.4.4",
                transportMode = TransportProtocol.UDP,
                vpnProtocol = VPNManagerProtocolTarget.OPENVPN,
                throwable = VPNManagerError(VPNManagerErrorCode.FAILED)
            )
            connectionEventAnnouncer.handleServerConnectAttemptFailed(
                serverIp = "1.1.1.1",
                transportMode = TransportProtocol.UDP,
                vpnProtocol = VPNManagerProtocolTarget.OPENVPN,
                throwable = VPNManagerError(VPNManagerErrorCode.FAILED)
            )
            connectionEventAnnouncer.handleServerConnectAttemptFailed(
                serverIp = "8.8.8.8",
                transportMode = TransportProtocol.UDP,
                vpnProtocol = VPNManagerProtocolTarget.OPENVPN,
                throwable = VPNManagerError(VPNManagerErrorCode.FAILED)
            )
            connectionEventAnnouncer.handleServerConnectAttemptFailed(
                serverIp = "4.4.4.4",
                transportMode = TransportProtocol.UDP,
                vpnProtocol = VPNManagerProtocolTarget.OPENVPN,
                throwable = VPNManagerError(VPNManagerErrorCode.FAILED)
            )
        }
    }

    @Test
    fun `handleServerConnectSuccess is called for the first server alone when it succeeds`() = runTest {
        // given
        val serverList = ServerList(
            servers = listOf(
                GivenModel.server().copy(ip = "1.1.1.1", latency = 11L),
                GivenModel.server().copy(ip = "8.8.4.4", latency = 10L),
                GivenModel.server().copy(ip = "8.8.8.8", latency = 12L),
                GivenModel.server().copy(ip = "4.4.4.4", latency = 13L)
            )
        )
        val targetProvider: ITargetProvider = GivenExternal.targetProvider()
        val serviceManagerMock: IServiceManager =
            ServiceManagerMock(shouldSucceedOnStartAttemptNumber = 0)
        val startConnection: IStartConnection = StartConnection(
            serviceManager = serviceManagerMock
        )
        val setServer: ISetServer = GivenUsecase.setServer()
        val connectionEventAnnouncer: IConnectionEventAnnouncer = mockk(relaxed = true)
        val coroutineContext = GivenExternal.coroutineContext()
        val startIteratingConnection: IStartIteratingConnection = StartIteratingConnection(
            targetProvider = targetProvider,
            startConnection = startConnection,
            setServer = setServer,
            connectionEventAnnouncer = connectionEventAnnouncer,
            coroutineContext = coroutineContext
        )

        // when
        startIteratingConnection(
            serverList = serverList,
            vpnProtocol = VPNManagerProtocolTarget.OPENVPN
        )

        // then
        verifySequence {
            connectionEventAnnouncer.handleServerConnectAttemptSucceeded(
                serverIp = "8.8.4.4",
                transportMode = TransportProtocol.UDP,
                vpnProtocol = VPNManagerProtocolTarget.OPENVPN
            )
        }
    }

    @Test
    fun `handleServerConnectFailed is called for each server before a connection attempt succeeds`() = runTest {
        // given
        val serverList = ServerList(
            servers = listOf(
                GivenModel.server().copy(ip = "1.1.1.1", latency = 11L),
                GivenModel.server().copy(ip = "8.8.4.4", latency = 10L),
                GivenModel.server().copy(ip = "8.8.8.8", latency = 12L),
                GivenModel.server().copy(ip = "4.4.4.4", latency = 13L)
            )
        )
        val targetProvider: ITargetProvider = GivenExternal.targetProvider()
        val serviceManagerMock: IServiceManager =
            ServiceManagerMock(shouldSucceedOnStartAttemptNumber = 2)
        val startConnection: IStartConnection = StartConnection(
            serviceManager = serviceManagerMock
        )
        val setServer: ISetServer = GivenUsecase.setServer()
        val connectionEventAnnouncer: IConnectionEventAnnouncer = mockk(relaxed = true)
        val coroutineContext = GivenExternal.coroutineContext()
        val startIteratingConnection: IStartIteratingConnection = StartIteratingConnection(
            targetProvider = targetProvider,
            startConnection = startConnection,
            setServer = setServer,
            connectionEventAnnouncer = connectionEventAnnouncer,
            coroutineContext = coroutineContext
        )

        // when
        startIteratingConnection(
            serverList = serverList,
            vpnProtocol = VPNManagerProtocolTarget.OPENVPN
        )

        // then
        verifySequence {
            connectionEventAnnouncer.handleServerConnectAttemptFailed(
                serverIp = "8.8.4.4",
                transportMode = TransportProtocol.UDP,
                vpnProtocol = VPNManagerProtocolTarget.OPENVPN,
                throwable = VPNManagerError(VPNManagerErrorCode.FAILED)
            )
            connectionEventAnnouncer.handleServerConnectAttemptFailed(
                serverIp = "1.1.1.1",
                transportMode = TransportProtocol.UDP,
                vpnProtocol = VPNManagerProtocolTarget.OPENVPN,
                throwable = VPNManagerError(VPNManagerErrorCode.FAILED)
            )
            connectionEventAnnouncer.handleServerConnectAttemptSucceeded(
                serverIp = "8.8.8.8",
                transportMode = TransportProtocol.UDP,
                vpnProtocol = VPNManagerProtocolTarget.OPENVPN
            )
        }
    }
}
