package com.kape.vpnservicemanager.domain.controllers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kape.vpnservicemanager.data.models.VPNServiceManagerConfiguration
import com.kape.vpnservicemanager.domain.usecases.IClearCache
import com.kape.vpnservicemanager.domain.usecases.IGetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.IIsServiceCleared
import com.kape.vpnservicemanager.domain.usecases.ISetProtocolConfiguration
import com.kape.vpnservicemanager.domain.usecases.ISetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.IStartConnection
import com.kape.vpnservicemanager.domain.usecases.IStartReconnectionHandler
import com.kape.vpnservicemanager.domain.usecases.IStopConnection
import com.kape.vpnservicemanager.testutils.GivenController
import com.kape.vpnservicemanager.testutils.GivenModel
import com.kape.vpnservicemanager.testutils.mocks.ClearCacheMock
import com.kape.vpnservicemanager.testutils.mocks.GetServerPeerInformationMock
import com.kape.vpnservicemanager.testutils.mocks.IsServiceClearedMock
import com.kape.vpnservicemanager.testutils.mocks.SetProtocolConfigurationMock
import com.kape.vpnservicemanager.testutils.mocks.SetServerPeerInformationMock
import com.kape.vpnservicemanager.testutils.mocks.StartConnectionMock
import com.kape.vpnservicemanager.testutils.mocks.StartReconnectionHandlerMock
import com.kape.vpnservicemanager.testutils.mocks.StopConnectionMock
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
internal class StartConnectionControllerTest {

    @Test
    fun `start successfully when there is no service present`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val protocolConfiguration: VPNServiceManagerConfiguration =
            GivenModel.vpnServiceManagerConfiguration(context = context)
        val isServiceClearedMock: IIsServiceCleared = IsServiceClearedMock(shouldSucceed = true)
        val setProtocolConfigurationMock: ISetProtocolConfiguration =
            SetProtocolConfigurationMock(shouldSucceed = true)
        val setServerPeerInformationMock: ISetServerPeerInformation =
            SetServerPeerInformationMock(shouldSucceed = true)
        val startConnectionMock: IStartConnection = StartConnectionMock(shouldSucceed = true)
        val startReconnectionHandlerMock: IStartReconnectionHandler =
            StartReconnectionHandlerMock(shouldSucceed = true)
        val getServerPeerInformationMock: IGetServerPeerInformation =
            GetServerPeerInformationMock(shouldSucceed = true)
        val stopConnectionMock: IStopConnection = StopConnectionMock(shouldSucceed = true)
        val clearCacheMock: IClearCache = ClearCacheMock()
        val startConnectionController: IStartConnectionController =
            GivenController.startConnectionController(
                context = context,
                isServiceCleared = isServiceClearedMock,
                setProtocolConfiguration = setProtocolConfigurationMock,
                setServerPeerInformation = setServerPeerInformationMock,
                startConnection = startConnectionMock,
                startReconnectionHandler = startReconnectionHandlerMock,
                getServerPeerInformation = getServerPeerInformationMock,
                stopConnection = stopConnectionMock,
                clearCache = clearCacheMock
            )

        // when
        val result = startConnectionController(protocolConfiguration = protocolConfiguration)

        // then
        assert(result.isSuccess)
    }

    @Test
    fun `start successfully should not clear cache`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val protocolConfiguration: VPNServiceManagerConfiguration =
            GivenModel.vpnServiceManagerConfiguration(context = context)
        val isServiceClearedMock: IIsServiceCleared = IsServiceClearedMock(shouldSucceed = true)
        val setProtocolConfigurationMock: ISetProtocolConfiguration =
            SetProtocolConfigurationMock(shouldSucceed = true)
        val setServerPeerInformationMock: ISetServerPeerInformation =
            SetServerPeerInformationMock(shouldSucceed = true)
        val startConnectionMock: IStartConnection = StartConnectionMock(shouldSucceed = true)
        val startReconnectionHandlerMock: IStartReconnectionHandler =
            StartReconnectionHandlerMock(shouldSucceed = true)
        val getServerPeerInformationMock: IGetServerPeerInformation =
            GetServerPeerInformationMock(shouldSucceed = true)
        val stopConnectionMock: IStopConnection = StopConnectionMock(shouldSucceed = true)
        val clearCacheMock: IClearCache = ClearCacheMock()
        val startConnectionController: IStartConnectionController =
            GivenController.startConnectionController(
                context = context,
                isServiceCleared = isServiceClearedMock,
                setProtocolConfiguration = setProtocolConfigurationMock,
                setServerPeerInformation = setServerPeerInformationMock,
                startConnection = startConnectionMock,
                startReconnectionHandler = startReconnectionHandlerMock,
                getServerPeerInformation = getServerPeerInformationMock,
                stopConnection = stopConnectionMock,
                clearCache = clearCacheMock
            )

        // when
        val result = startConnectionController(protocolConfiguration = protocolConfiguration)

        // then
        assert(result.isSuccess)
        assert((clearCacheMock as ClearCacheMock).invocationsCounter == 0)
    }

    @Test
    fun `fail to start when there is a service present`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val protocolConfiguration: VPNServiceManagerConfiguration =
            GivenModel.vpnServiceManagerConfiguration(context = context)
        val isServiceClearedMock: IIsServiceCleared = IsServiceClearedMock(shouldSucceed = false)
        val setProtocolConfigurationMock: ISetProtocolConfiguration =
            SetProtocolConfigurationMock(shouldSucceed = true)
        val setServerPeerInformationMock: ISetServerPeerInformation =
            SetServerPeerInformationMock(shouldSucceed = true)
        val startConnectionMock: IStartConnection = StartConnectionMock(shouldSucceed = true)
        val startReconnectionHandlerMock: IStartReconnectionHandler =
            StartReconnectionHandlerMock(shouldSucceed = true)
        val getServerPeerInformationMock: IGetServerPeerInformation =
            GetServerPeerInformationMock(shouldSucceed = true)
        val stopConnectionMock: IStopConnection = StopConnectionMock(shouldSucceed = true)
        val clearCacheMock: IClearCache = ClearCacheMock()
        val startConnectionController: IStartConnectionController =
            GivenController.startConnectionController(
                context = context,
                isServiceCleared = isServiceClearedMock,
                setProtocolConfiguration = setProtocolConfigurationMock,
                setServerPeerInformation = setServerPeerInformationMock,
                startConnection = startConnectionMock,
                startReconnectionHandler = startReconnectionHandlerMock,
                getServerPeerInformation = getServerPeerInformationMock,
                stopConnection = stopConnectionMock,
                clearCache = clearCacheMock
            )

        // when
        val result = startConnectionController(protocolConfiguration = protocolConfiguration)

        // then
        assert(result.isFailure)
    }

    @Test
    fun `fail to start when there is an issue setting the configuration`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val protocolConfiguration: VPNServiceManagerConfiguration =
            GivenModel.vpnServiceManagerConfiguration(context = context)
        val isServiceClearedMock: IIsServiceCleared = IsServiceClearedMock(shouldSucceed = true)
        val setProtocolConfigurationMock: ISetProtocolConfiguration =
            SetProtocolConfigurationMock(shouldSucceed = false)
        val setServerPeerInformationMock: ISetServerPeerInformation =
            SetServerPeerInformationMock(shouldSucceed = true)
        val startConnectionMock: IStartConnection = StartConnectionMock(shouldSucceed = true)
        val startReconnectionHandlerMock: IStartReconnectionHandler =
            StartReconnectionHandlerMock(shouldSucceed = true)
        val getServerPeerInformationMock: IGetServerPeerInformation =
            GetServerPeerInformationMock(shouldSucceed = true)
        val stopConnectionMock: IStopConnection = StopConnectionMock(shouldSucceed = true)
        val clearCacheMock: IClearCache = ClearCacheMock()
        val startConnectionController: IStartConnectionController =
            GivenController.startConnectionController(
                context = context,
                isServiceCleared = isServiceClearedMock,
                setProtocolConfiguration = setProtocolConfigurationMock,
                setServerPeerInformation = setServerPeerInformationMock,
                startConnection = startConnectionMock,
                startReconnectionHandler = startReconnectionHandlerMock,
                getServerPeerInformation = getServerPeerInformationMock,
                stopConnection = stopConnectionMock,
                clearCache = clearCacheMock
            )

        // when
        val result = startConnectionController(protocolConfiguration = protocolConfiguration)

        // then
        assert(result.isFailure)
    }

    @Test
    fun `fail to start due to an issue setting the configuration triggers a clear cache`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val protocolConfiguration: VPNServiceManagerConfiguration =
            GivenModel.vpnServiceManagerConfiguration(context = context)
        val isServiceClearedMock: IIsServiceCleared = IsServiceClearedMock(shouldSucceed = true)
        val setProtocolConfigurationMock: ISetProtocolConfiguration =
            SetProtocolConfigurationMock(shouldSucceed = false)
        val setServerPeerInformationMock: ISetServerPeerInformation =
            SetServerPeerInformationMock(shouldSucceed = true)
        val startConnectionMock: IStartConnection = StartConnectionMock(shouldSucceed = true)
        val startReconnectionHandlerMock: IStartReconnectionHandler =
            StartReconnectionHandlerMock(shouldSucceed = true)
        val getServerPeerInformationMock: IGetServerPeerInformation =
            GetServerPeerInformationMock(shouldSucceed = true)
        val stopConnectionMock: IStopConnection = StopConnectionMock(shouldSucceed = true)
        val clearCacheMock: IClearCache = ClearCacheMock()
        val startConnectionController: IStartConnectionController =
            GivenController.startConnectionController(
                context = context,
                isServiceCleared = isServiceClearedMock,
                setProtocolConfiguration = setProtocolConfigurationMock,
                setServerPeerInformation = setServerPeerInformationMock,
                startConnection = startConnectionMock,
                startReconnectionHandler = startReconnectionHandlerMock,
                getServerPeerInformation = getServerPeerInformationMock,
                stopConnection = stopConnectionMock,
                clearCache = clearCacheMock
            )

        // when
        val result = startConnectionController(protocolConfiguration = protocolConfiguration)

        // then
        assert(result.isFailure)
        assert((clearCacheMock as ClearCacheMock).invocationsCounter == 1)
        assert((stopConnectionMock as StopConnectionMock).invocationsCounter == 0)
    }

    @Test
    fun `fail to start due to an issue setting the server information triggers a clear cache and stop service`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val protocolConfiguration: VPNServiceManagerConfiguration =
            GivenModel.vpnServiceManagerConfiguration(context = context)
        val isServiceClearedMock: IIsServiceCleared = IsServiceClearedMock(shouldSucceed = true)
        val setProtocolConfigurationMock: ISetProtocolConfiguration =
            SetProtocolConfigurationMock(shouldSucceed = true)
        val setServerPeerInformationMock: ISetServerPeerInformation =
            SetServerPeerInformationMock(shouldSucceed = false)
        val startConnectionMock: IStartConnection = StartConnectionMock(shouldSucceed = true)
        val startReconnectionHandlerMock: IStartReconnectionHandler =
            StartReconnectionHandlerMock(shouldSucceed = true)
        val getServerPeerInformationMock: IGetServerPeerInformation =
            GetServerPeerInformationMock(shouldSucceed = true)
        val stopConnectionMock: IStopConnection = StopConnectionMock(shouldSucceed = true)
        val clearCacheMock: IClearCache = ClearCacheMock()
        val startConnectionController: IStartConnectionController =
            GivenController.startConnectionController(
                context = context,
                isServiceCleared = isServiceClearedMock,
                setProtocolConfiguration = setProtocolConfigurationMock,
                setServerPeerInformation = setServerPeerInformationMock,
                startConnection = startConnectionMock,
                startReconnectionHandler = startReconnectionHandlerMock,
                getServerPeerInformation = getServerPeerInformationMock,
                stopConnection = stopConnectionMock,
                clearCache = clearCacheMock
            )

        // when
        val result = startConnectionController(protocolConfiguration = protocolConfiguration)

        // then
        assert(result.isFailure)
        assert((clearCacheMock as ClearCacheMock).invocationsCounter == 1)
        assert((stopConnectionMock as StopConnectionMock).invocationsCounter == 1)
    }

    @Test
    fun `fail to start due to a service issue triggers clear cache and stop service`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val protocolConfiguration: VPNServiceManagerConfiguration =
            GivenModel.vpnServiceManagerConfiguration(context = context)
        val isServiceClearedMock: IIsServiceCleared = IsServiceClearedMock(shouldSucceed = true)
        val setProtocolConfigurationMock: ISetProtocolConfiguration =
            SetProtocolConfigurationMock(shouldSucceed = true)
        val setServerPeerInformationMock: ISetServerPeerInformation =
            SetServerPeerInformationMock(shouldSucceed = true)
        val startConnectionMock: IStartConnection = StartConnectionMock(shouldSucceed = false)
        val startReconnectionHandlerMock: IStartReconnectionHandler =
            StartReconnectionHandlerMock(shouldSucceed = true)
        val getServerPeerInformationMock: IGetServerPeerInformation =
            GetServerPeerInformationMock(shouldSucceed = true)
        val stopConnectionMock: IStopConnection = StopConnectionMock(shouldSucceed = true)
        val clearCacheMock: IClearCache = ClearCacheMock()
        val startConnectionController: IStartConnectionController =
            GivenController.startConnectionController(
                context = context,
                isServiceCleared = isServiceClearedMock,
                setProtocolConfiguration = setProtocolConfigurationMock,
                setServerPeerInformation = setServerPeerInformationMock,
                startConnection = startConnectionMock,
                startReconnectionHandler = startReconnectionHandlerMock,
                getServerPeerInformation = getServerPeerInformationMock,
                stopConnection = stopConnectionMock,
                clearCache = clearCacheMock
            )

        // when
        val result = startConnectionController(protocolConfiguration = protocolConfiguration)

        // then
        assert(result.isFailure)
        assert((clearCacheMock as ClearCacheMock).invocationsCounter == 1)
        assert((stopConnectionMock as StopConnectionMock).invocationsCounter == 1)
    }

    @Test
    fun `fail to start due to an issue on the reconnection handler clear cache and stop service`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val protocolConfiguration: VPNServiceManagerConfiguration =
            GivenModel.vpnServiceManagerConfiguration(context = context)
        val isServiceClearedMock: IIsServiceCleared = IsServiceClearedMock(shouldSucceed = true)
        val setProtocolConfigurationMock: ISetProtocolConfiguration =
            SetProtocolConfigurationMock(shouldSucceed = true)
        val setServerPeerInformationMock: ISetServerPeerInformation =
            SetServerPeerInformationMock(shouldSucceed = true)
        val startConnectionMock: IStartConnection = StartConnectionMock(shouldSucceed = true)
        val startReconnectionHandlerMock: IStartReconnectionHandler =
            StartReconnectionHandlerMock(shouldSucceed = false)
        val getServerPeerInformationMock: IGetServerPeerInformation =
            GetServerPeerInformationMock(shouldSucceed = true)
        val stopConnectionMock: IStopConnection = StopConnectionMock(shouldSucceed = true)
        val clearCacheMock: IClearCache = ClearCacheMock()
        val startConnectionController: IStartConnectionController =
            GivenController.startConnectionController(
                context = context,
                isServiceCleared = isServiceClearedMock,
                setProtocolConfiguration = setProtocolConfigurationMock,
                setServerPeerInformation = setServerPeerInformationMock,
                startConnection = startConnectionMock,
                startReconnectionHandler = startReconnectionHandlerMock,
                getServerPeerInformation = getServerPeerInformationMock,
                stopConnection = stopConnectionMock,
                clearCache = clearCacheMock
            )

        // when
        val result = startConnectionController(protocolConfiguration = protocolConfiguration)

        // then
        assert(result.isFailure)
        assert((clearCacheMock as ClearCacheMock).invocationsCounter == 1)
        assert((stopConnectionMock as StopConnectionMock).invocationsCounter == 1)
    }

    @Test
    fun `fail to start due to an issue on getting the server information clear cache and stop service`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val protocolConfiguration: VPNServiceManagerConfiguration =
            GivenModel.vpnServiceManagerConfiguration(context = context)
        val isServiceClearedMock: IIsServiceCleared = IsServiceClearedMock(shouldSucceed = true)
        val setProtocolConfigurationMock: ISetProtocolConfiguration =
            SetProtocolConfigurationMock(shouldSucceed = true)
        val setServerPeerInformationMock: ISetServerPeerInformation =
            SetServerPeerInformationMock(shouldSucceed = true)
        val startConnectionMock: IStartConnection = StartConnectionMock(shouldSucceed = true)
        val startReconnectionHandlerMock: IStartReconnectionHandler =
            StartReconnectionHandlerMock(shouldSucceed = true)
        val getServerPeerInformationMock: IGetServerPeerInformation =
            GetServerPeerInformationMock(shouldSucceed = false)
        val stopConnectionMock: IStopConnection = StopConnectionMock(shouldSucceed = true)
        val clearCacheMock: IClearCache = ClearCacheMock()
        val startConnectionController: IStartConnectionController =
            GivenController.startConnectionController(
                context = context,
                isServiceCleared = isServiceClearedMock,
                setProtocolConfiguration = setProtocolConfigurationMock,
                setServerPeerInformation = setServerPeerInformationMock,
                startConnection = startConnectionMock,
                startReconnectionHandler = startReconnectionHandlerMock,
                getServerPeerInformation = getServerPeerInformationMock,
                stopConnection = stopConnectionMock,
                clearCache = clearCacheMock
            )

        // when
        val result = startConnectionController(protocolConfiguration = protocolConfiguration)

        // then
        assert(result.isFailure)
        assert((clearCacheMock as ClearCacheMock).invocationsCounter == 1)
        assert((stopConnectionMock as StopConnectionMock).invocationsCounter == 1)
    }
}
