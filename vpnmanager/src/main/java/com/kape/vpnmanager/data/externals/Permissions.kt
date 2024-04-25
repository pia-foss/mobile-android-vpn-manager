package com.kape.vpnmanager.data.externals

import android.content.Context
import android.net.VpnService
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import com.kape.vpnmanager.presenters.VPNManagerError
import com.kape.vpnmanager.presenters.VPNManagerErrorCode
import com.kape.vpnmanager.presenters.VPNManagerPermissionsDependency
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

internal class Permissions(
    private val context: Context,
    private val coroutineContext: ICoroutineContext,
    private val permissionsDependency: VPNManagerPermissionsDependency,
) : IPermissions {

    // region IPermissions
    override suspend fun requestVpnPermissions(): Result<Unit> {
        val deferred: CompletableDeferred<Result<Unit>> = CompletableDeferred()
        val clientCoroutineContext = coroutineContext.getClientCoroutineContext().getOrThrow()
        CoroutineScope(clientCoroutineContext).launch {
            permissionsDependency.requestNecessaryPermissions { result ->
                val granted = result.getOrElse {
                    deferred.complete(Result.failure(it))
                    return@requestNecessaryPermissions
                }
                if (granted) {
                    deferred.complete(Result.success(Unit))
                } else {
                    deferred.complete(
                        Result.failure(VPNManagerError(VPNManagerErrorCode.PERMISSIONS_NOT_GRANTED))
                    )
                }
            }
        }
        return deferred.await()
    }

    override fun hasVpnPermissionsGranted(): Result<Unit> {
        // `VpnService.prepare` returning `null` means we have the required permissions.
        // Otherwise, request them. They joys of implicit API behaviour.
        return if (VpnService.prepare(context) == null) {
            Result.success(Unit)
        } else {
            Result.failure(VPNManagerError(VPNManagerErrorCode.PERMISSIONS_NOT_GRANTED))
        }
    }
    // endregion
}
