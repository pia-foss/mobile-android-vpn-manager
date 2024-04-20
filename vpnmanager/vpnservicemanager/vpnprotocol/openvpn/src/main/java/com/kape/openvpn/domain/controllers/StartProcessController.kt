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

package com.kape.openvpn.domain.controllers

import com.kape.openvpn.data.utils.getOrFail
import com.kape.openvpn.domain.usecases.IClearCache
import com.kape.openvpn.domain.usecases.IIsProcessStopped
import com.kape.openvpn.domain.usecases.IStartOpenVpnOutputHandler
import com.kape.openvpn.domain.usecases.IStartProcess
import com.kape.openvpn.domain.usecases.IStartProcessOutputReader
import com.kape.openvpn.presenters.OpenVpnProcessEventHandler

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

internal class StartProcessController(
    private val isProcessStopped: IIsProcessStopped,
    private val startOpenVpnOutputHandler: IStartOpenVpnOutputHandler,
    private val startProcess: IStartProcess,
    private val startProcessOutputReader: IStartProcessOutputReader,
    private val clearCache: IClearCache,
) : com.kape.openvpn.domain.controllers.IStartProcessController {

    // region IStartProcessController
    override suspend fun invoke(
        commandLineParams: List<String>,
        openVpnProcessEventHandler: OpenVpnProcessEventHandler,
    ): Result<Unit> {
        val onFailure: suspend (throwable: Throwable) -> Unit = {
            clearCache()
            throw it
        }

        return isProcessStopped()
            .mapCatching {
                startOpenVpnOutputHandler(
                    openVpnProcessEventHandler = openVpnProcessEventHandler
                ).getOrFail(onFailure = onFailure)
            }
            .mapCatching {
                startProcess(commandLineParams = commandLineParams).getOrFail(onFailure = onFailure)
            }
            .mapCatching {
                startProcessOutputReader().getOrFail(onFailure = onFailure)
            }
    }
    // endregion
}
