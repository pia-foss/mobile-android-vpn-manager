package com.kape.vpnprotocol.domain.usecases.openvpn

import com.kape.vpnprotocol.data.externals.common.ICacheProtocol
import com.kape.vpnprotocol.data.externals.common.IFile
import com.kape.vpnprotocol.data.models.VPNProtocolCipher
import com.kape.vpnprotocol.data.models.VPNProtocolConfiguration
import com.kape.vpnprotocol.data.models.VPNProtocolOpenVpnConfiguration
import com.kape.vpnprotocol.data.models.VPNProtocolServer
import com.kape.vpnprotocol.data.models.VPNTransportProtocol
import com.kape.vpnprotocol.presenters.VPNProtocolTarget
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
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

class GenerateOpenVpnSettingsTest {

    // mocked dependencies of the usecase
    private val cacheProtocol: ICacheProtocol = mockk(relaxed = true)
    private val file: IFile = mockk(relaxed = true)

    // SUT
    private val usecase = GenerateOpenVpnSettings(cacheProtocol, file)

    @Before
    fun setUp() {
        every { cacheProtocol.getManagementPath() } returns Result.success("path")
        every { file.createTemporaryDirectory() } returns Result.success("file")
    }

    @Test
    fun `The additional parameters are added to the OpenVPN command`() = runTest {
        // given
        val mtuTest = "--mtu-test"
        every { cacheProtocol.getProtocolConfiguration() } returns Result.success(
            createOpenVpnConfigWithAdditionalParams(mtuTest)
        )
        val certificateFilePath = "ca.crt"

        // when
        val result = usecase(certificateFilePath).getOrThrow()

        // then
        assertTrue(result.contains(mtuTest))
    }

    @Test
    fun `Empty additional parameters are not added to the OpenVpn command`() = runTest {
        // given
        every { cacheProtocol.getProtocolConfiguration() } returns Result.success(
            createOpenVpnConfigWithAdditionalParams("")
        )
        val certificateFilePath = "ca.crt"

        // when
        val result = usecase(certificateFilePath).getOrThrow()

        // then
        assertFalse(result.contains(""))
    }

    private fun createOpenVpnConfigWithAdditionalParams(params: String): VPNProtocolConfiguration {
        val openVpnConfig = VPNProtocolOpenVpnConfiguration(
            server = VPNProtocolServer(
                ip = "1.2.3.4",
                port = 8080,
                commonOrDistinguishedName = "serverName",
                transport = VPNTransportProtocol.UDP,
                ciphers = listOf(VPNProtocolCipher.AES_256_GCM)
            ),
            caCertificate = "ca.crt",
            username = "username",
            password = "password",
            socksProxy = null,
            additionalParameters = params
        )
        return VPNProtocolConfiguration(
            sessionName = "session",
            protocolTarget = VPNProtocolTarget.OPENVPN,
            mtu = 1280,
            allowedIps = emptyList(),
            openVpnClientConfiguration = openVpnConfig,
            wireguardClientConfiguration = mockk(relaxed = true)
        )
    }
}
