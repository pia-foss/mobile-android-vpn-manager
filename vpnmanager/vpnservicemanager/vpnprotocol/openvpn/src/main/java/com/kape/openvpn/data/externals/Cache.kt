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

import com.kape.openvpn.data.utils.pid
import com.kape.openvpn.domain.usecases.IOpenVpnProcessOutputHandler
import com.kape.openvpn.presenters.OpenVpnError
import com.kape.openvpn.presenters.OpenVpnErrorCode

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

internal class Cache : ICache {

    private var process: Process? = null
    private var openVpnProcessOutputHandler: IOpenVpnProcessOutputHandler? = null

    // region ICache
    override fun clear(): Result<Unit> {
        return clearProcess()
            .mapCatching {
                clearProcessOutputHandler().getOrThrow()
            }
    }

    override fun setProcess(process: Process): Result<Unit> {
        this.process = process
        return Result.success(Unit)
    }

    override fun getProcess(): Result<Process> =
        runCatching {
            process ?: throw OpenVpnError(code = OpenVpnErrorCode.PROCESS_UNKNOWN)
        }

    override fun clearProcess(): Result<Unit> {
        process = null
        return Result.success(Unit)
    }

    override fun getProcessId(): Result<Int> =
        getProcess().mapCatching {
            it.pid().getOrThrow()
        }

    override fun setProcessOutputHandler(openVpnProcessOutputHandler: IOpenVpnProcessOutputHandler): Result<Unit> {
        this.openVpnProcessOutputHandler = openVpnProcessOutputHandler
        return Result.success(Unit)
    }

    override fun getProcessOutputHandler(): Result<IOpenVpnProcessOutputHandler> =
        runCatching {
            openVpnProcessOutputHandler
                ?: throw OpenVpnError(code = OpenVpnErrorCode.PROCESS_OUTPUT_HANDLER_UNKNOWN)
        }

    override fun clearProcessOutputHandler(): Result<Unit> {
        openVpnProcessOutputHandler = null
        return Result.success(Unit)
    }
    // endregion
}
