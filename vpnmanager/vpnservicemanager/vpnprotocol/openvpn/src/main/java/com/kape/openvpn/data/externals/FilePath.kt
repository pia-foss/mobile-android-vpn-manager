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

package com.kape.openvpn.data.externals

import android.content.Context
import com.kape.openvpn.presenters.OpenVpnError
import com.kape.openvpn.presenters.OpenVpnErrorCode
import java.io.File
import java.lang.Exception

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

internal class FilePath(
    private val context: Context,
) : IFilePath {

    companion object {
        private const val EXECUTABLE_LIBRARY_NAME = "libovpnexec.so"
    }

    // region IFilePath
    override fun getExecutablePath(): Result<String> {
        return try {
            val executablePath: String = File(
                context.applicationInfo.nativeLibraryDir,
                EXECUTABLE_LIBRARY_NAME
            ).canonicalFile.absolutePath
            Result.success(executablePath)
        } catch (exception: Exception) {
            Result.failure(OpenVpnError(code = OpenVpnErrorCode.UNKNOWN_LIBRARY_PATH))
        }
    }

    override fun getLibrariesPath(): Result<String> {
        return try {
            val librariesPath: String = File(
                context.applicationInfo.nativeLibraryDir
            ).canonicalFile.absolutePath
            Result.success(librariesPath)
        } catch (exception: Exception) {
            Result.failure(OpenVpnError(code = OpenVpnErrorCode.UNKNOWN_LIBRARY_PATH))
        }
    }
    // endregion
}
