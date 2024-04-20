package com.kape.vpnprotocol.domain.controllers

import com.kape.vpnprotocol.domain.controllers.wireguard.IStartWireguardReconnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.StartWireguardReconnectionController
import com.kape.vpnprotocol.domain.usecases.common.IGetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.IIsNetworkAvailable
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.wireguard.ICreateWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.IDestroyWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.IGenerateWireguardSettings
import com.kape.vpnprotocol.domain.usecases.wireguard.IGetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.IProtectWireguardTunnelSocket
import com.kape.vpnprotocol.domain.usecases.wireguard.ISetWireguardTunnelHandle
import com.kape.vpnprotocol.testutils.mocks.CreateWireguardTunnelMock
import com.kape.vpnprotocol.testutils.mocks.DestroyWireguardTunnelMock
import com.kape.vpnprotocol.testutils.mocks.GenerateWireguardSettingsMock
import com.kape.vpnprotocol.testutils.mocks.GetProtocolConfigurationMock
import com.kape.vpnprotocol.testutils.mocks.GetWireguardTunnelHandleMock
import com.kape.vpnprotocol.testutils.mocks.IsNetworkAvailableMock
import com.kape.vpnprotocol.testutils.mocks.ProtectWireguardTunnelSocketMock
import com.kape.vpnprotocol.testutils.mocks.ReportConnectivityStatusMock
import com.kape.vpnprotocol.testutils.mocks.SetWireguardTunnelHandleMock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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

@ExperimentalCoroutinesApi
internal class StartWireguardReconnectionControllerTest {

    @Test
    fun `should succeed if all use cases in the controller flow succeed`() = runTest {
        // given
        val startWireguardReconnectionController: IStartWireguardReconnectionController =
            provideSuccessfulStartWireguardReconnectionController()

        // when
        val result = startWireguardReconnectionController()

        // then
        assert(result.isSuccess)
    }

    @Test
    fun `should fail if reporting the connectivity status failed`() = runTest {
        // given
        val reportConnectivityStatusMock: IReportConnectivityStatus =
            ReportConnectivityStatusMock(shouldSucceed = false)
        val startWireguardReconnectionController: IStartWireguardReconnectionController =
            provideSuccessfulStartWireguardReconnectionController(
                reportConnectivityStatusMock = reportConnectivityStatusMock
            )

        // when
        val result = startWireguardReconnectionController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if getting the protocol configuration failed`() = runTest {
        // given
        val getProtocolConfigurationMock: IGetProtocolConfiguration =
            GetProtocolConfigurationMock(shouldSucceed = false)
        val startWireguardReconnectionController: IStartWireguardReconnectionController =
            provideSuccessfulStartWireguardReconnectionController(
                getProtocolConfigurationMock = getProtocolConfigurationMock
            )

        // when
        val result = startWireguardReconnectionController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if there is no network connectivity`() = runTest {
        // given
        val isNetworkAvailableMock: IIsNetworkAvailable =
            IsNetworkAvailableMock(shouldSucceed = false)
        val startWireguardReconnectionController: IStartWireguardReconnectionController =
            provideSuccessfulStartWireguardReconnectionController(
                isNetworkAvailableMock = isNetworkAvailableMock
            )

        // when
        val result = startWireguardReconnectionController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if getting the wireguard tunnel handle failed`() = runTest {
        // given
        val getWireguardTunnelHandleMock: IGetWireguardTunnelHandle =
            GetWireguardTunnelHandleMock(shouldSucceed = false)
        val startWireguardReconnectionController: IStartWireguardReconnectionController =
            provideSuccessfulStartWireguardReconnectionController(
                getWireguardTunnelHandleMock = getWireguardTunnelHandleMock
            )

        // when
        val result = startWireguardReconnectionController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if destroying the wireguard tunnel failed`() = runTest {
        // given
        val destroyWireguardTunnelMock: IDestroyWireguardTunnel =
            DestroyWireguardTunnelMock(shouldSucceed = false)
        val startWireguardReconnectionController: IStartWireguardReconnectionController =
            provideSuccessfulStartWireguardReconnectionController(
                destroyWireguardTunnelMock = destroyWireguardTunnelMock
            )

        // when
        val result = startWireguardReconnectionController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if generating the wireguard settings failed`() = runTest {
        // given
        val generateWireguardSettingsMock: IGenerateWireguardSettings =
            GenerateWireguardSettingsMock(shouldSucceed = false)
        val startWireguardReconnectionController: IStartWireguardReconnectionController =
            provideSuccessfulStartWireguardReconnectionController(
                generateWireguardSettingsMock = generateWireguardSettingsMock
            )

        // when
        val result = startWireguardReconnectionController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if creating the wireguard tunnel failed`() = runTest {
        // given
        val createWireguardTunnelMock: ICreateWireguardTunnel =
            CreateWireguardTunnelMock(shouldSucceed = false)
        val startWireguardReconnectionController: IStartWireguardReconnectionController =
            provideSuccessfulStartWireguardReconnectionController(
                createWireguardTunnelMock = createWireguardTunnelMock
            )

        // when
        val result = startWireguardReconnectionController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if setting the wireguard tunnel handle failed`() = runTest {
        // given
        val setWireguardTunnelHandleMock: ISetWireguardTunnelHandle =
            SetWireguardTunnelHandleMock(shouldSucceed = false)
        val startWireguardReconnectionController: IStartWireguardReconnectionController =
            provideSuccessfulStartWireguardReconnectionController(
                setWireguardTunnelHandleMock = setWireguardTunnelHandleMock
            )

        // when
        val result = startWireguardReconnectionController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if protecting the wireguard tunnel failed`() = runTest {
        // given
        val protectWireguardTunnelSocketMock: IProtectWireguardTunnelSocket =
            ProtectWireguardTunnelSocketMock(shouldSucceed = false)
        val startWireguardReconnectionController: IStartWireguardReconnectionController =
            provideSuccessfulStartWireguardReconnectionController(
                protectWireguardTunnelSocketMock = protectWireguardTunnelSocketMock
            )

        // when
        val result = startWireguardReconnectionController()

        // then
        assert(result.isFailure)
    }

    // region private
    private fun provideSuccessfulStartWireguardReconnectionController(
        reportConnectivityStatusMock: IReportConnectivityStatus =
            ReportConnectivityStatusMock(shouldSucceed = true),
        getProtocolConfigurationMock: IGetProtocolConfiguration =
            GetProtocolConfigurationMock(shouldSucceed = true),
        isNetworkAvailableMock: IIsNetworkAvailable =
            IsNetworkAvailableMock(shouldSucceed = true),
        getWireguardTunnelHandleMock: IGetWireguardTunnelHandle =
            GetWireguardTunnelHandleMock(shouldSucceed = true),
        destroyWireguardTunnelMock: IDestroyWireguardTunnel =
            DestroyWireguardTunnelMock(shouldSucceed = true),
        generateWireguardSettingsMock: IGenerateWireguardSettings =
            GenerateWireguardSettingsMock(shouldSucceed = true),
        createWireguardTunnelMock: ICreateWireguardTunnel =
            CreateWireguardTunnelMock(shouldSucceed = true),
        setWireguardTunnelHandleMock: ISetWireguardTunnelHandle =
            SetWireguardTunnelHandleMock(shouldSucceed = true),
        protectWireguardTunnelSocketMock: IProtectWireguardTunnelSocket =
            ProtectWireguardTunnelSocketMock(shouldSucceed = true),
    ): IStartWireguardReconnectionController =
        StartWireguardReconnectionController(
            reportConnectivityStatus = reportConnectivityStatusMock,
            getProtocolConfiguration = getProtocolConfigurationMock,
            isNetworkAvailable = isNetworkAvailableMock,
            getWireguardTunnelHandle = getWireguardTunnelHandleMock,
            destroyWireguardTunnel = destroyWireguardTunnelMock,
            generateWireguardSettings = generateWireguardSettingsMock,
            createWireguardTunnel = createWireguardTunnelMock,
            setWireguardTunnelHandle = setWireguardTunnelHandleMock,
            protectWireguardTunnelSocket = protectWireguardTunnelSocketMock
        )
    // endregion
}
