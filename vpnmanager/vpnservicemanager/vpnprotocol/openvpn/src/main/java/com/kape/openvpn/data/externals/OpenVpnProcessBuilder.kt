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

import com.kape.openvpn.presenters.OpenVpnError
import com.kape.openvpn.presenters.OpenVpnErrorCode
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

internal class OpenVpnProcessBuilder : IOpenVpnProcessBuilder {

    companion object {
        private const val LD_LIBRARY_PATH = "LD_LIBRARY_PATH"
    }

    override fun start(commands: List<String>, ldLibrariesPath: String): Result<Process> {
        val processBuilder = ProcessBuilder(commands)
        val environment: MutableMap<String, String> = processBuilder.environment()
        environment.clear()
        environment[LD_LIBRARY_PATH] = ldLibrariesPath
        processBuilder.redirectErrorStream()

        return try {
            val process: Process = processBuilder.start().apply {
                // We have nothing to input to the process's output. Close it.
                outputStream.close()
            }
            Result.success(process)
        } catch (throwable: Throwable) {
            Result.failure(
                OpenVpnError(
                    code = OpenVpnErrorCode.PROCESS_COULD_NOT_START,
                    error = Error(throwable.message)
                )
            )
        }
    }
    // endregion
}
