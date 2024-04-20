package com.kape.vpnservicemanager.testutils.mocks

import com.kape.vpnservicemanager.data.externals.ICache
import com.kape.vpnservicemanager.data.externals.Service
import com.kape.vpnservicemanager.data.models.VPNServiceManagerConfiguration
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation
import com.kape.vpnservicemanager.presenters.VPNServiceManagerError
import com.kape.vpnservicemanager.presenters.VPNServiceManagerErrorCode

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

internal class CacheMock(
    private val mockedResponses: Map<MethodSignature, Any>,
) : ICache {

    internal enum class MethodSignature {
        CLEAR,
        SET_SERVICE,
        GET_SERVICE,
        CLEAR_SERVICE,
        SET_SERVICE_BOUND,
        GET_SERVICE_BOUND,
        CLEAR_SERVICE_BOUND,
        SET_PROTOCOL_CONFIGURATION,
        GET_PROTOCOL_CONFIGURATION,
        CLEAR_PROTOCOL_CONFIGURATION,
        SET_SERVER_PEER_INFORMATION,
        GET_SERVER_PEER_INFORMATION,
        CLEAR_SERVER_PEER_INFORMATION,
    }

    override fun clear(): Result<Unit> =
        mockedResponses[MethodSignature.CLEAR]?.let {
            Result.success(it as Unit)
        } ?: Result.failure(VPNServiceManagerError(VPNServiceManagerErrorCode.SERVICE_NOT_READY))

    override fun setService(service: Service): Result<Unit> =
        mockedResponses[MethodSignature.SET_SERVICE]?.let {
            Result.success(it as Unit)
        } ?: Result.failure(VPNServiceManagerError(VPNServiceManagerErrorCode.KNOWN_SERVICE_PRESENT))

    override fun getService(): Result<Service> =
        mockedResponses[MethodSignature.GET_SERVICE]?.let {
            Result.success(it as Service)
        } ?: Result.failure(VPNServiceManagerError(VPNServiceManagerErrorCode.SERVICE_NOT_READY))

    override fun clearService(): Result<Unit> =
        mockedResponses[MethodSignature.CLEAR_SERVICE]?.let {
            Result.success(it as Unit)
        } ?: Result.failure(VPNServiceManagerError(VPNServiceManagerErrorCode.SERVICE_NOT_READY))

    override fun setServiceBound(): Result<Unit> =
        mockedResponses[MethodSignature.SET_SERVICE_BOUND]?.let {
            Result.success(it as Unit)
        } ?: Result.failure(VPNServiceManagerError(VPNServiceManagerErrorCode.KNOWN_SERVICE_PRESENT))

    override fun isServiceBound(): Boolean =
        mockedResponses[MethodSignature.GET_SERVICE_BOUND]?.let {
            it as Boolean
        } ?: false

    override fun clearServiceBound(): Result<Unit> =
        mockedResponses[MethodSignature.CLEAR_SERVICE_BOUND]?.let {
            Result.success(it as Unit)
        } ?: Result.failure(VPNServiceManagerError(VPNServiceManagerErrorCode.SERVICE_NOT_READY))

    override fun setProtocolConfiguration(
        protocolConfiguration: VPNServiceManagerConfiguration,
    ): Result<Unit> =
        mockedResponses[MethodSignature.SET_PROTOCOL_CONFIGURATION]?.let {
            Result.success(it as Unit)
        } ?: Result.failure(VPNServiceManagerError(VPNServiceManagerErrorCode.PROTOCOL_CONFIGURATION_NOT_READY))

    override fun getProtocolConfiguration(): Result<VPNServiceManagerConfiguration> =
        mockedResponses[MethodSignature.GET_PROTOCOL_CONFIGURATION]?.let {
            Result.success(it as VPNServiceManagerConfiguration)
        } ?: Result.failure(VPNServiceManagerError(VPNServiceManagerErrorCode.PROTOCOL_CONFIGURATION_NOT_READY))

    override fun clearProtocolConfiguration(): Result<Unit> =
        mockedResponses[MethodSignature.CLEAR_PROTOCOL_CONFIGURATION]?.let {
            Result.success(it as Unit)
        } ?: Result.failure(VPNServiceManagerError(VPNServiceManagerErrorCode.PROTOCOL_CONFIGURATION_NOT_READY))

    override fun setServerPeerInformation(serverPeerInformation: VPNServiceServerPeerInformation): Result<Unit> =
        mockedResponses[MethodSignature.SET_SERVER_PEER_INFORMATION]?.let {
            Result.success(it as Unit)
        } ?: Result.failure(VPNServiceManagerError(VPNServiceManagerErrorCode.PROTOCOL_PEER_INFORMATION_NOT_READY))

    override fun getServerPeerInformation(): Result<VPNServiceServerPeerInformation> =
        mockedResponses[MethodSignature.GET_SERVER_PEER_INFORMATION]?.let {
            Result.success(it as VPNServiceServerPeerInformation)
        } ?: Result.failure(VPNServiceManagerError(VPNServiceManagerErrorCode.PROTOCOL_PEER_INFORMATION_NOT_READY))

    override fun clearServerPeerInformation(): Result<Unit> =
        mockedResponses[MethodSignature.CLEAR_SERVER_PEER_INFORMATION]?.let {
            Result.success(it as Unit)
        } ?: Result.failure(VPNServiceManagerError(VPNServiceManagerErrorCode.PROTOCOL_PEER_INFORMATION_NOT_READY))
}
