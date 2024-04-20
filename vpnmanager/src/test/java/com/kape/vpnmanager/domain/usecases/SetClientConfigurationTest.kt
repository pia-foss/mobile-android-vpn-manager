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

package com.kape.vpnmanager.domain.usecases

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kape.vpnmanager.data.externals.ICache
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.domain.datasources.ICacheDatasource
import com.kape.vpnmanager.testutils.GivenExternal
import com.kape.vpnmanager.testutils.GivenGateway
import com.kape.vpnmanager.testutils.GivenModel
import com.kape.vpnmanager.testutils.GivenUsecase
import com.kape.vpnmanager.usecases.ISetClientConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class SetClientConfigurationTest {

    @Test
    fun `successfully set the client configuration on the external cache layer`() = runTest {
        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val clientConfiguration: ClientConfiguration = GivenModel.clientConfiguration(
            context = context,
            mtu = 1000,
            allowedApplicationPackages = listOf("com.kape.android")
        )
        val cache: ICache = GivenExternal.cache()
        val cacheDatasource: ICacheDatasource = GivenGateway.cacheDatasource(cache = cache)
        val setClientConfiguration: ISetClientConfiguration = GivenUsecase.setClientConfiguration(
            cacheDatasource = cacheDatasource
        )

        // when
        val result = setClientConfiguration(clientConfiguration = clientConfiguration)

        // then
        assert(result.isSuccess)
        assert(cacheDatasource.getState().getOrThrow().configuration.clientConfiguration == clientConfiguration)
    }
}
