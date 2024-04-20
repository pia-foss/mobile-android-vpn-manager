package com.kape.vpnprotocol.domain.controllers

import com.kape.vpnprotocol.domain.controllers.wireguard.IStartWireguardConnectionController
import com.kape.vpnprotocol.domain.controllers.wireguard.StartWireguardConnectionController
import com.kape.vpnprotocol.domain.usecases.common.IClearCache
import com.kape.vpnprotocol.domain.usecases.common.IGetServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.common.IIsNetworkAvailable
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.common.ISetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.ISetServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.common.ISetServiceFileDescriptor
import com.kape.vpnprotocol.domain.usecases.common.ISetVpnService
import com.kape.vpnprotocol.domain.usecases.wireguard.ICreateWireguardTunnel
import com.kape.vpnprotocol.domain.usecases.wireguard.IGenerateWireguardKeyPair
import com.kape.vpnprotocol.domain.usecases.wireguard.IGenerateWireguardServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.wireguard.IGenerateWireguardSettings
import com.kape.vpnprotocol.domain.usecases.wireguard.IPerformWireguardAddKeyRequest
import com.kape.vpnprotocol.domain.usecases.wireguard.IProtectWireguardTunnelSocket
import com.kape.vpnprotocol.domain.usecases.wireguard.ISetWireguardAddKeyResponse
import com.kape.vpnprotocol.domain.usecases.wireguard.ISetWireguardKeyPair
import com.kape.vpnprotocol.domain.usecases.wireguard.ISetWireguardTunnelHandle
import com.kape.vpnprotocol.domain.usecases.wireguard.IStartWireguardByteCountJob
import com.kape.vpnprotocol.testutils.GivenModel
import com.kape.vpnprotocol.testutils.mocks.ClearCacheMock
import com.kape.vpnprotocol.testutils.mocks.CreateWireguardTunnelMock
import com.kape.vpnprotocol.testutils.mocks.GenerateWireguardKeyPairMock
import com.kape.vpnprotocol.testutils.mocks.GenerateWireguardServerPeerInformationMock
import com.kape.vpnprotocol.testutils.mocks.GenerateWireguardSettingsMock
import com.kape.vpnprotocol.testutils.mocks.GetServerPeerInformationMock
import com.kape.vpnprotocol.testutils.mocks.IsNetworkAvailableMock
import com.kape.vpnprotocol.testutils.mocks.PerformWireguardAddKeyRequestMock
import com.kape.vpnprotocol.testutils.mocks.ProtectWireguardTunnelSocketMock
import com.kape.vpnprotocol.testutils.mocks.ReportConnectivityStatusMock
import com.kape.vpnprotocol.testutils.mocks.ServiceConfigurationFileDescriptorProviderMock
import com.kape.vpnprotocol.testutils.mocks.SetProtocolConfigurationMock
import com.kape.vpnprotocol.testutils.mocks.SetServerPeerInformationMock
import com.kape.vpnprotocol.testutils.mocks.SetServiceFileDescriptorMock
import com.kape.vpnprotocol.testutils.mocks.SetVpnServiceMock
import com.kape.vpnprotocol.testutils.mocks.SetWireguardAddKeyResponseMock
import com.kape.vpnprotocol.testutils.mocks.SetWireguardKeyPairMock
import com.kape.vpnprotocol.testutils.mocks.SetWireguardTunnelHandleMock
import com.kape.vpnprotocol.testutils.mocks.StartWireguardByteCountJobMock
import com.kape.vpnprotocol.testutils.mocks.VPNProtocolServiceMock
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
internal class StartWireguardConnectionControllerTest {

    @Test
    fun `should succeed if all use cases in the controller flow succeed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController()

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isSuccess)
    }

    @Test
    fun `should fail if reporting the connectivity status failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val reportConnectivityStatusMock: IReportConnectivityStatus =
            ReportConnectivityStatusMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                reportConnectivityStatusMock = reportConnectivityStatusMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if there is no network connectivity`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val isNetworkAvailableMock: IIsNetworkAvailable =
            IsNetworkAvailableMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                isNetworkAvailableMock = isNetworkAvailableMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if setting the vpn service failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val setVpnServiceMock: ISetVpnService =
            SetVpnServiceMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                setVpnServiceMock = setVpnServiceMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if setting the protocol configuration failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val setProtocolConfigurationMock: ISetProtocolConfiguration =
            SetProtocolConfigurationMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                setProtocolConfigurationMock = setProtocolConfigurationMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if setting the service file descriptor failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val setServiceFileDescriptorMock: ISetServiceFileDescriptor =
            SetServiceFileDescriptorMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                setServiceFileDescriptorMock = setServiceFileDescriptorMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if generating the wireguard key pair failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val generateWireguardKeyPairMock: IGenerateWireguardKeyPair =
            GenerateWireguardKeyPairMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                generateWireguardKeyPairMock = generateWireguardKeyPairMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if setting the wireguard key pair failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val setWireguardKeyPairMock: ISetWireguardKeyPair =
            SetWireguardKeyPairMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                setWireguardKeyPairMock = setWireguardKeyPairMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if performing the add key request failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val performWireguardAddKeyRequestMock: IPerformWireguardAddKeyRequest =
            PerformWireguardAddKeyRequestMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                performWireguardAddKeyRequestMock = performWireguardAddKeyRequestMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if setting the wireguard add key response failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val setWireguardAddKeyResponseMock: ISetWireguardAddKeyResponse =
            SetWireguardAddKeyResponseMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                setWireguardAddKeyResponseMock = setWireguardAddKeyResponseMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if generating the wireguard settings failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val generateWireguardSettingsMock: IGenerateWireguardSettings =
            GenerateWireguardSettingsMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                generateWireguardSettingsMock = generateWireguardSettingsMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if creating the wireguard tunnel failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val createWireguardTunnelMock: ICreateWireguardTunnel =
            CreateWireguardTunnelMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                createWireguardTunnelMock = createWireguardTunnelMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if setting the wireguard tunnel failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val setWireguardTunnelHandleMock: ISetWireguardTunnelHandle =
            SetWireguardTunnelHandleMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                setWireguardTunnelHandleMock = setWireguardTunnelHandleMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if protecting the wireguard tunnel failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val protectWireguardTunnelSocketMock: IProtectWireguardTunnelSocket =
            ProtectWireguardTunnelSocketMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                protectWireguardTunnelSocketMock = protectWireguardTunnelSocketMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if generating the wireguard server peer information failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val generateWireguardServerPeerInformationMock: IGenerateWireguardServerPeerInformation =
            GenerateWireguardServerPeerInformationMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                generateWireguardServerPeerInformationMock = generateWireguardServerPeerInformationMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if setting the server peer information failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val setServerPeerInformationMock: ISetServerPeerInformation =
            SetServerPeerInformationMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                setServerPeerInformationMock = setServerPeerInformationMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if getting the server peer information failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val getServerPeerInformationMock: IGetServerPeerInformation =
            GetServerPeerInformationMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                getServerPeerInformationMock = getServerPeerInformationMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if starting byte count job failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val startWireguardByteCountJobMock: IStartWireguardByteCountJob =
            StartWireguardByteCountJobMock(shouldSucceed = false)
        val startWireguardConnectionController: IStartWireguardConnectionController =
            provideSuccessfulStartWireguardConnectionController(
                startWireguardByteCountJobMock = startWireguardByteCountJobMock
            )

        // when
        val result = startWireguardConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    // region private
    private fun provideSuccessfulStartWireguardConnectionController(
        reportConnectivityStatusMock: IReportConnectivityStatus =
            ReportConnectivityStatusMock(shouldSucceed = true),
        isNetworkAvailableMock: IIsNetworkAvailable =
            IsNetworkAvailableMock(shouldSucceed = true),
        setVpnServiceMock: ISetVpnService =
            SetVpnServiceMock(shouldSucceed = true),
        setProtocolConfigurationMock: ISetProtocolConfiguration =
            SetProtocolConfigurationMock(shouldSucceed = true),
        setServiceFileDescriptorMock: ISetServiceFileDescriptor =
            SetServiceFileDescriptorMock(shouldSucceed = true),
        generateWireguardKeyPairMock: IGenerateWireguardKeyPair =
            GenerateWireguardKeyPairMock(shouldSucceed = true),
        setWireguardKeyPairMock: ISetWireguardKeyPair =
            SetWireguardKeyPairMock(shouldSucceed = true),
        performWireguardAddKeyRequestMock: IPerformWireguardAddKeyRequest =
            PerformWireguardAddKeyRequestMock(shouldSucceed = true),
        setWireguardAddKeyResponseMock: ISetWireguardAddKeyResponse =
            SetWireguardAddKeyResponseMock(shouldSucceed = true),
        generateWireguardSettingsMock: IGenerateWireguardSettings =
            GenerateWireguardSettingsMock(shouldSucceed = true),
        createWireguardTunnelMock: ICreateWireguardTunnel =
            CreateWireguardTunnelMock(shouldSucceed = true),
        setWireguardTunnelHandleMock: ISetWireguardTunnelHandle =
            SetWireguardTunnelHandleMock(shouldSucceed = true),
        protectWireguardTunnelSocketMock: IProtectWireguardTunnelSocket =
            ProtectWireguardTunnelSocketMock(shouldSucceed = true),
        generateWireguardServerPeerInformationMock: IGenerateWireguardServerPeerInformation =
            GenerateWireguardServerPeerInformationMock(shouldSucceed = true),
        startWireguardByteCountJobMock: IStartWireguardByteCountJob =
            StartWireguardByteCountJobMock(shouldSucceed = true),
        setServerPeerInformationMock: ISetServerPeerInformation =
            SetServerPeerInformationMock(shouldSucceed = true),
        getServerPeerInformationMock: IGetServerPeerInformation =
            GetServerPeerInformationMock(shouldSucceed = true),
        clearCacheMock: IClearCache =
            ClearCacheMock(shouldSucceed = true),
    ): IStartWireguardConnectionController =
        StartWireguardConnectionController(
            reportConnectivityStatus = reportConnectivityStatusMock,
            isNetworkAvailable = isNetworkAvailableMock,
            setVpnService = setVpnServiceMock,
            setProtocolConfiguration = setProtocolConfigurationMock,
            setServiceFileDescriptor = setServiceFileDescriptorMock,
            generateWireguardKeyPair = generateWireguardKeyPairMock,
            setWireguardKeyPair = setWireguardKeyPairMock,
            performWireguardAddKeyRequest = performWireguardAddKeyRequestMock,
            setWireguardAddKeyResponse = setWireguardAddKeyResponseMock,
            generateWireguardSettings = generateWireguardSettingsMock,
            createWireguardTunnel = createWireguardTunnelMock,
            setWireguardTunnelHandle = setWireguardTunnelHandleMock,
            protectWireguardTunnelSocket = protectWireguardTunnelSocketMock,
            generateWireguardServerPeerInformation = generateWireguardServerPeerInformationMock,
            startWireguardByteCountJob = startWireguardByteCountJobMock,
            setServerPeerInformation = setServerPeerInformationMock,
            getServerPeerInformation = getServerPeerInformationMock,
            clearCache = clearCacheMock
        )
    // endregion
}
