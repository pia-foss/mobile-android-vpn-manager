package com.kape.openvpn.domain.usecases

import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HandleOpenVpnMtuTestResultOutputTest {

    private lateinit var openVpnMtuTestResultAnnouncer: IOpenVpnMtuTestResultAnnouncer
    private lateinit var handleOpenVpnMtuTestResultOutput: HandleOpenVpnMtuTestResultOutput

    @Before
    fun setup() {
        openVpnMtuTestResultAnnouncer = mockk(relaxed = true)
        handleOpenVpnMtuTestResultOutput =
            HandleOpenVpnMtuTestResultOutput(openVpnMtuTestResultAnnouncer)
    }

    @Test
    fun `test result announcer is invoked & result is successful for proper mtu test result`() {
        // Given
        val outputLine =
            "empirical mtu test completed [tried,actual] local->remote=[1573,1573] remote->local=[1469,1469]"

        // When
        val result = handleOpenVpnMtuTestResultOutput(outputLine)

        // Then
        verify {
            openVpnMtuTestResultAnnouncer.onMtuTestResult(
                localToRemote = 1573,
                remoteToLocal = 1469
            )
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `result is a failure with IllegalArgumentException if mtu test result line is wrong`() {
        // Given
        val outputLine =
            "empirical mtu test not completed [tried,actual] local->remote=[1573,1573] remote->local=[1469,1469]"

        // When
        val result = handleOpenVpnMtuTestResultOutput(outputLine)

        // Then
        verify { openVpnMtuTestResultAnnouncer wasNot Called }
        assertTrue(result.isFailure)
        assertEquals(IllegalArgumentException::class, result.exceptionOrNull()!!::class)
    }
}
