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

package com.kape.openvpn.domain.usecases

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kape.openvpn.data.externals.ICache
import com.kape.openvpn.data.externals.IOpenVpnProcess
import com.kape.openvpn.testutils.GivenExternal
import com.kape.openvpn.testutils.GivenUsecase
import com.kape.openvpn.testutils.mocks.OpenVpnProcessMock
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

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

@RunWith(RobolectricTestRunner::class)
internal class StopProcessTest {

    @Test
    fun `fail when trying to stop a process that is not running`() = runBlocking {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val openVpnProcessMock: IOpenVpnProcess = OpenVpnProcessMock(succeed = true)
        val cache: ICache = GivenExternal.cache()
        val stopProcess: IStopProcess = GivenUsecase.stopProcess(
            context = context,
            cache = cache,
            process = openVpnProcessMock
        )

        // when
        val result = stopProcess()

        // then
        assert(result.isFailure)
    }
}
