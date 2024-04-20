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

package com.kape.openvpn.data.utils

import com.kape.openvpn.presenters.OpenVpnError
import com.kape.openvpn.presenters.OpenVpnErrorCode
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

internal class OpenVpnUtils {

    companion object {
        internal const val PROCESS_IDENTIFIER = "pid"
    }
}

internal fun Process.pid(): Result<Int> {
    return try {
        val pidField = this.javaClass.getDeclaredField(OpenVpnUtils.PROCESS_IDENTIFIER)
        pidField.isAccessible = true
        val pid = pidField.getInt(this)
        pidField.isAccessible = false
        Result.success(pid)
    } catch (throwable: Throwable) {
        Result.failure(
            OpenVpnError(
                code = OpenVpnErrorCode.INVALID_PID,
                error = Error(throwable.message)
            )
        )
    }
}

internal fun Process.isRunning(): Result<Unit> {
    return this.pid().mapCatching {
        if (File("/proc/$it").exists()) {
            Result.success(Unit)
        } else {
            Result.failure(OpenVpnError(code = OpenVpnErrorCode.PROCESS_NOT_RUNNING))
        }
    }
}
