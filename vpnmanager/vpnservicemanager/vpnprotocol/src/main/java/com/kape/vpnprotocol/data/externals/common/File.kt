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

package com.kape.vpnprotocol.data.externals.common

import android.content.Context
import com.kape.vpnprotocol.presenters.VPNProtocolError
import com.kape.vpnprotocol.presenters.VPNProtocolErrorCode
import java.io.File
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

internal class File(
    private val context: Context,
) : IFile {

    companion object {
        const val CERTIFICATE_FILENAME = "ca.crt"
    }

    // region IFile
    override fun createCertificateFile(certificate: String): Result<String> {
        val appDataDirectory = File(context.applicationInfo.dataDir)
        val caCertificateFile = File(appDataDirectory, CERTIFICATE_FILENAME)
        caCertificateFile.writeText(certificate)
        return Result.success(caCertificateFile.absolutePath)
    }

    override fun createTemporaryDirectory(): Result<String> {
        try {
            val tmpFolder = File(context.applicationInfo.dataDir, "tmp")
            tmpFolder.deleteRecursively()
            tmpFolder.mkdir()
            return Result.success(tmpFolder.canonicalPath)
        } catch (throwable: Throwable) {
            return Result.failure(
                VPNProtocolError(
                    code = VPNProtocolErrorCode.PROTOCOL_CONFIGURATION_NOT_READY,
                    error = Error("Generation of openvpn configuration failed. Tmp creation failed.")
                )
            )
        }
    }
    // endregion
}
