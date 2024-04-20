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

package com.kape.vpnprotocol.domain.controllers

import com.kape.vpnprotocol.domain.controllers.openvpn.IStartOpenVpnConnectionController
import com.kape.vpnprotocol.domain.controllers.openvpn.StartOpenVpnConnectionController
import com.kape.vpnprotocol.domain.usecases.common.IClearCache
import com.kape.vpnprotocol.domain.usecases.common.IGetServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.common.IIsNetworkAvailable
import com.kape.vpnprotocol.domain.usecases.common.IReportConnectivityStatus
import com.kape.vpnprotocol.domain.usecases.common.ISetProtocolConfiguration
import com.kape.vpnprotocol.domain.usecases.common.ISetServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.common.ISetServiceFileDescriptor
import com.kape.vpnprotocol.domain.usecases.common.ISetVpnService
import com.kape.vpnprotocol.domain.usecases.openvpn.ICreateOpenVpnCertificateFile
import com.kape.vpnprotocol.domain.usecases.openvpn.ICreateOpenVpnProcessConnectedDeferrable
import com.kape.vpnprotocol.domain.usecases.openvpn.IFilterAdditionalOpenVpnParams
import com.kape.vpnprotocol.domain.usecases.openvpn.IGenerateOpenVpnServerPeerInformation
import com.kape.vpnprotocol.domain.usecases.openvpn.IGenerateOpenVpnSettings
import com.kape.vpnprotocol.domain.usecases.openvpn.ISetGeneratedOpenVpnSettings
import com.kape.vpnprotocol.domain.usecases.openvpn.IStartOpenVpnEventHandler
import com.kape.vpnprotocol.domain.usecases.openvpn.IStartOpenVpnProcess
import com.kape.vpnprotocol.domain.usecases.openvpn.IWaitForOpenVpnProcessConnectedDeferrable
import com.kape.vpnprotocol.testutils.GivenModel
import com.kape.vpnprotocol.testutils.mocks.ClearCacheMock
import com.kape.vpnprotocol.testutils.mocks.CreateOpenVpnCertificateFileMock
import com.kape.vpnprotocol.testutils.mocks.CreateOpenVpnProcessConnectedDeferrableMock
import com.kape.vpnprotocol.testutils.mocks.FilterAdditionalOpenVpnParamsMock
import com.kape.vpnprotocol.testutils.mocks.GenerateOpenVpnServerPeerInformationMock
import com.kape.vpnprotocol.testutils.mocks.GenerateOpenVpnSettingsMock
import com.kape.vpnprotocol.testutils.mocks.GetServerPeerInformationMock
import com.kape.vpnprotocol.testutils.mocks.IsNetworkAvailableMock
import com.kape.vpnprotocol.testutils.mocks.ReportConnectivityStatusMock
import com.kape.vpnprotocol.testutils.mocks.ServiceConfigurationFileDescriptorProviderMock
import com.kape.vpnprotocol.testutils.mocks.SetGeneratedOpenVpnSettingsMock
import com.kape.vpnprotocol.testutils.mocks.SetProtocolConfigurationMock
import com.kape.vpnprotocol.testutils.mocks.SetServerPeerInformationMock
import com.kape.vpnprotocol.testutils.mocks.SetServiceFileDescriptorMock
import com.kape.vpnprotocol.testutils.mocks.SetVpnServiceMock
import com.kape.vpnprotocol.testutils.mocks.StartOpenVpnEventHandlerMock
import com.kape.vpnprotocol.testutils.mocks.StartOpenVpnProcessMock
import com.kape.vpnprotocol.testutils.mocks.VPNProtocolServiceMock
import com.kape.vpnprotocol.testutils.mocks.WaitForOpenVpnProcessConnectedDeferrableMock
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
internal class StartOpenVpnConnectionControllerTest {

    @Test
    fun `should succeed if all use cases in the controller flow succeed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController()

        // when
        val result = startOpenVpnConnectionController(
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
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                reportConnectivityStatusMock = reportConnectivityStatusMock
            )

        // when
        val result = startOpenVpnConnectionController(
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
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                isNetworkAvailableMock = isNetworkAvailableMock
            )

        // when
        val result = startOpenVpnConnectionController(
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
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                setVpnServiceMock = setVpnServiceMock
            )

        // when
        val result = startOpenVpnConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if filtering the OpenVpn parameters failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val filterAdditionalOpenVpnParams: IFilterAdditionalOpenVpnParams =
            FilterAdditionalOpenVpnParamsMock(shouldSucceed = false)
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                filterAdditionalOpenVpnParams = filterAdditionalOpenVpnParams
            )

        // when
        val result = startOpenVpnConnectionController(
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
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                setProtocolConfigurationMock = setProtocolConfigurationMock
            )

        // when
        val result = startOpenVpnConnectionController(
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
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                setServiceFileDescriptorMock = setServiceFileDescriptorMock
            )

        // when
        val result = startOpenVpnConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if creating the certificate file failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val createOpenVpnCertificateFileMock: ICreateOpenVpnCertificateFile =
            CreateOpenVpnCertificateFileMock(shouldSucceed = false)
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                createOpenVpnCertificateFileMock = createOpenVpnCertificateFileMock
            )

        // when
        val result = startOpenVpnConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if generating the setting failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val generateOpenVpnSettingsMock: IGenerateOpenVpnSettings =
            GenerateOpenVpnSettingsMock(shouldSucceed = false)
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                generateOpenVpnSettingsMock = generateOpenVpnSettingsMock
            )

        // when
        val result = startOpenVpnConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if setting the generated settings failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val setGeneratedOpenVpnSettingsMock: ISetGeneratedOpenVpnSettings =
            SetGeneratedOpenVpnSettingsMock(shouldSucceed = false)
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                setGeneratedOpenVpnSettingsMock = setGeneratedOpenVpnSettingsMock
            )

        // when
        val result = startOpenVpnConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if creating the open vpn connection deferrable failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val createOpenVpnProcessConnectedDeferrableMock: ICreateOpenVpnProcessConnectedDeferrable =
            CreateOpenVpnProcessConnectedDeferrableMock(shouldSucceed = false)
        ClearCacheMock(shouldSucceed = true)
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                createOpenVpnProcessConnectedDeferrableMock = createOpenVpnProcessConnectedDeferrableMock
            )

        // when
        val result = startOpenVpnConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if starting the open vpn event handler failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val startOpenVpnEventHandlerMock: IStartOpenVpnEventHandler =
            StartOpenVpnEventHandlerMock(shouldSucceed = false)
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                startOpenVpnEventHandlerMock = startOpenVpnEventHandlerMock
            )

        // when
        val result = startOpenVpnConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if starting the open vpn process failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val startOpenVpnProcessMock: IStartOpenVpnProcess =
            StartOpenVpnProcessMock(shouldSucceed = false)
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                startOpenVpnProcessMock = startOpenVpnProcessMock
            )

        // when
        val result = startOpenVpnConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if waiting for the open vpn deferrable failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val waitForOpenVpnProcessConnectedDeferrableMock: IWaitForOpenVpnProcessConnectedDeferrable =
            WaitForOpenVpnProcessConnectedDeferrableMock(shouldSucceed = false)
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                waitForOpenVpnProcessConnectedDeferrableMock = waitForOpenVpnProcessConnectedDeferrableMock
            )

        // when
        val result = startOpenVpnConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if generating the server peer information failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val generateOpenVpnServerPeerInformationMock: IGenerateOpenVpnServerPeerInformation =
            GenerateOpenVpnServerPeerInformationMock(shouldSucceed = false)
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                generateOpenVpnServerPeerInformationMock = generateOpenVpnServerPeerInformationMock
            )

        // when
        val result = startOpenVpnConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    @Test
    fun `should fail if setting the generated server peer information failed`() = runTest {
        // given
        val vpnProtocolServiceMock = VPNProtocolServiceMock()
        val protocolConfigurationMock = GivenModel.vpnProtocolConfiguration()
        val serviceConfigurationFileDescriptorProviderMock =
            ServiceConfigurationFileDescriptorProviderMock()
        val setServerPeerInformationMock: ISetServerPeerInformation =
            SetServerPeerInformationMock(shouldSucceed = false)
        ClearCacheMock(shouldSucceed = true)
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                setServerPeerInformationMock = setServerPeerInformationMock
            )

        // when
        val result = startOpenVpnConnectionController(
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
        val startOpenVpnConnectionController: IStartOpenVpnConnectionController =
            provideSuccessfulStartOpenVpnConnectionController(
                getServerPeerInformationMock = getServerPeerInformationMock
            )

        // when
        val result = startOpenVpnConnectionController(
            vpnService = vpnProtocolServiceMock,
            protocolConfiguration = protocolConfigurationMock,
            serviceConfigurationFileDescriptorProvider = serviceConfigurationFileDescriptorProviderMock
        )

        // then
        assert(result.isFailure)
    }

    // region private
    private fun provideSuccessfulStartOpenVpnConnectionController(
        reportConnectivityStatusMock: IReportConnectivityStatus =
            ReportConnectivityStatusMock(shouldSucceed = true),
        isNetworkAvailableMock: IIsNetworkAvailable =
            IsNetworkAvailableMock(shouldSucceed = true),
        setVpnServiceMock: ISetVpnService =
            SetVpnServiceMock(shouldSucceed = true),
        filterAdditionalOpenVpnParams: IFilterAdditionalOpenVpnParams =
            FilterAdditionalOpenVpnParamsMock(shouldSucceed = true),
        setProtocolConfigurationMock: ISetProtocolConfiguration =
            SetProtocolConfigurationMock(shouldSucceed = true),
        setServiceFileDescriptorMock: ISetServiceFileDescriptor =
            SetServiceFileDescriptorMock(shouldSucceed = true),
        createOpenVpnCertificateFileMock: ICreateOpenVpnCertificateFile =
            CreateOpenVpnCertificateFileMock(shouldSucceed = true),
        generateOpenVpnSettingsMock: IGenerateOpenVpnSettings =
            GenerateOpenVpnSettingsMock(shouldSucceed = true),
        setGeneratedOpenVpnSettingsMock: ISetGeneratedOpenVpnSettings =
            SetGeneratedOpenVpnSettingsMock(shouldSucceed = true),
        createOpenVpnProcessConnectedDeferrableMock: ICreateOpenVpnProcessConnectedDeferrable =
            CreateOpenVpnProcessConnectedDeferrableMock(shouldSucceed = true),
        startOpenVpnEventHandlerMock: IStartOpenVpnEventHandler =
            StartOpenVpnEventHandlerMock(shouldSucceed = true),
        startOpenVpnProcessMock: IStartOpenVpnProcess =
            StartOpenVpnProcessMock(shouldSucceed = true),
        waitForOpenVpnProcessConnectedDeferrableMock: IWaitForOpenVpnProcessConnectedDeferrable =
            WaitForOpenVpnProcessConnectedDeferrableMock(shouldSucceed = true),
        generateOpenVpnServerPeerInformationMock: IGenerateOpenVpnServerPeerInformation =
            GenerateOpenVpnServerPeerInformationMock(shouldSucceed = true),
        setServerPeerInformationMock: ISetServerPeerInformation =
            SetServerPeerInformationMock(shouldSucceed = true),
        getServerPeerInformationMock: IGetServerPeerInformation =
            GetServerPeerInformationMock(shouldSucceed = true),
        clearCacheMock: IClearCache =
            ClearCacheMock(shouldSucceed = true),
    ): IStartOpenVpnConnectionController = StartOpenVpnConnectionController(
        reportConnectivityStatus = reportConnectivityStatusMock,
        isNetworkAvailable = isNetworkAvailableMock,
        setVpnService = setVpnServiceMock,
        filterAdditionalOpenVpnParams = filterAdditionalOpenVpnParams,
        setProtocolConfiguration = setProtocolConfigurationMock,
        setServiceFileDescriptor = setServiceFileDescriptorMock,
        createOpenVpnCertificateFile = createOpenVpnCertificateFileMock,
        generateOpenVpnSettings = generateOpenVpnSettingsMock,
        setGeneratedOpenVpnSettings = setGeneratedOpenVpnSettingsMock,
        createOpenVpnProcessConnectedDeferrable = createOpenVpnProcessConnectedDeferrableMock,
        startOpenVpnEventHandler = startOpenVpnEventHandlerMock,
        startOpenVpnProcess = startOpenVpnProcessMock,
        waitForOpenVpnProcessConnectedDeferrable = waitForOpenVpnProcessConnectedDeferrableMock,
        generateOpenVpnServerPeerInformation = generateOpenVpnServerPeerInformationMock,
        setServerPeerInformation = setServerPeerInformationMock,
        getServerPeerInformation = getServerPeerInformationMock,
        clearCache = clearCacheMock
    )
    // endregion
}
