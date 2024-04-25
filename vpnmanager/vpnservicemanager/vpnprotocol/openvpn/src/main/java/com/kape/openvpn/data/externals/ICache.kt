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

import com.kape.openvpn.domain.usecases.IOpenVpnProcessOutputHandler
import com.kape.vpnmanager.api.data.externals.IJob

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

internal interface ICache {

    /**
     * @return `Result<Unit>`.
     */
    fun clear(): Result<Unit>

    /**
     * @param process `Process`.
     *
     * @return `Result<Unit>`.
     */
    fun setProcess(process: Process): Result<Unit>

    /**
     * @return `Result<Process>`.
     */
    fun getProcess(): Result<Process>

    /**
     * @return `Result<Unit>`.
     */
    fun clearProcess(): Result<Unit>

    /**
     * @return `Result<Int>`.
     */
    fun getProcessId(): Result<Int>

    /**
     * @param openVpnProcessOutputHandler `IOpenVpnProcessOutputHandler`.
     *
     * @return `Result<Unit>`.
     */
    fun setProcessOutputHandler(
        openVpnProcessOutputHandler: IOpenVpnProcessOutputHandler,
    ): Result<Unit>

    /**
     * @return `Result<IOpenVpnProcessOutputHandler>`.
     */
    fun getProcessOutputHandler(): Result<IOpenVpnProcessOutputHandler>

    /**
     * @return `Result<Unit>`.
     */
    fun clearProcessOutputHandler(): Result<Unit>

    /**
     * @param job `IJob`.
     *
     * @return `Result<Unit>`.
     */
    fun setHoldReleaseJob(job: IJob): Result<Unit>

    /**
     * @return `Result<IJob>`.
     */
    fun getHoldReleaseJob(): Result<IJob>

    /**
     * @return `Result<Unit>`.
     */
    fun clearHoldReleaseJob(): Result<Unit>
}
