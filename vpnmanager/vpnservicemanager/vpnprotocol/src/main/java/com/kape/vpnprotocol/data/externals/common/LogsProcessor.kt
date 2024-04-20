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

import com.kape.vpnprotocol.data.models.ProcessStreams
import com.kape.vpnprotocol.presenters.VPNProtocolError
import com.kape.vpnprotocol.presenters.VPNProtocolErrorCode
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception

/*
 *  Copyright (c) 2023 Private Internet Access, Inc.
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

internal class LogsProcessor : ILogsProcessor {

    // region ILogsProcessor
    override fun processLogs(tag: String, streams: ProcessStreams): Result<List<String>> {
        val inputResult = readAndFilterStream(inputStream = streams.inputStream, targetPrefix = tag)
        if (inputResult.isEmpty().not()) {
            return Result.success(inputResult)
        }

        val errorResult = readAndFilterStream(inputStream = streams.errorStream, targetPrefix = tag)
        if (errorResult.isEmpty().not()) {
            return Result.success(errorResult)
        }

        return Result.failure(VPNProtocolError(code = VPNProtocolErrorCode.NO_VPN_LOGS_FOUND))
    }
    // endregion

    // region private
    private fun readAndFilterStream(inputStream: InputStream, targetPrefix: String): List<String> {
        var line: String
        val result = mutableListOf<String>()
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        try {
            while (bufferedReader.readLine().also { line = it } != null) {
                if (line.contains(targetPrefix, ignoreCase = true)) {
                    result.add(line)
                }
            }
        } catch (_: Exception) { }
        bufferedReader.close()
        return result
    }
    // endregion
}
