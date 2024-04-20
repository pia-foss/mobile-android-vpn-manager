package com.kape.vpnprotocol.domain.usecases.openvpn

import com.kape.vpnprotocol.data.externals.common.ICacheProtocol
import com.kape.vpnprotocol.data.externals.common.IFile
import com.kape.vpnprotocol.presenters.VPNProtocolError
import com.kape.vpnprotocol.presenters.VPNProtocolErrorCode
import java.lang.Error

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

internal class CreateOpenVpnCertificateFile(
    private val cacheProtocol: ICacheProtocol,
    private val file: IFile,
) : ICreateOpenVpnCertificateFile {

    // region ICreateOpenVpnCertificateFile
    override suspend fun invoke(): Result<String> {
        val protocolConfiguration = cacheProtocol.getProtocolConfiguration().getOrElse {
            return Result.failure(
                VPNProtocolError(
                    code = VPNProtocolErrorCode.PROTOCOL_CONFIGURATION_NOT_READY,
                    error = Error("Certificate file creation failed. Object not ready.")
                )
            )
        }

        return file.createCertificateFile(
            certificate = protocolConfiguration.openVpnClientConfiguration.caCertificate ?: ""
        )
    }
    // endregion
}
