package com.kape.openvpn.domain.usecases

import com.kape.openvpn.data.externals.ICache
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.coroutines.CoroutineContext

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

internal class StartProcessOutputReader(
    private val cache: ICache,
    private val coroutineContext: ICoroutineContext,
) : IStartProcessOutputReader {

    private val moduleCoroutineContext: CoroutineContext =
        coroutineContext.getModuleCoroutineContext().getOrThrow()

    // region IStartProcessOutputReader
    override suspend fun invoke(): Result<Unit> {
        val process = cache.getProcess().getOrElse {
            return Result.failure(it)
        }
        val processOutputHandler = cache.getProcessOutputHandler().getOrElse {
            return Result.failure(it)
        }

        readLines(
            bufferedReader = BufferedReader(InputStreamReader(process.inputStream)),
            processOutputHandler = processOutputHandler
        )
        return Result.success(Unit)
    }
    // endregion

    // region private
    private fun readLines(
        bufferedReader: BufferedReader,
        processOutputHandler: IOpenVpnProcessOutputHandler,
    ) {
        // We are single-threaded. However, in order to handle the stream we need to step away
        // from it, to avoid blocking execution. Let's jump back to the known context
        // when reporting the output via `outputHandler.output`.
        var active = cache.getProcess().isSuccess
        val scope = CoroutineScope(Dispatchers.IO)
        scope.async {
            try {
                var lastKnownLine: String? = null
                while (active) {
                    val line = bufferedReader.readLine()

                    // Avoid over-reporting
                    if (line != lastKnownLine) {
                        withContext(moduleCoroutineContext) {
                            processOutputHandler.output(line)
                        }
                    }

                    lastKnownLine = line

                    // Check whether we should continue reading by retrieving the process status
                    // on the dedicated thread.
                    active = withContext(moduleCoroutineContext) {
                        cache.getProcess().isSuccess
                    }
                }
            } catch (throwable: Throwable) {
                // TODO: Announce error via listener/announcer
            }
        }
    }
    // endregion
}
