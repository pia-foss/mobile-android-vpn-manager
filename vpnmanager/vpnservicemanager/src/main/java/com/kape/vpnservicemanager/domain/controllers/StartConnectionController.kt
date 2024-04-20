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

package com.kape.vpnservicemanager.domain.controllers

import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnservicemanager.data.models.VPNServiceManagerConfiguration
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation
import com.kape.vpnservicemanager.data.utils.getOrFail
import com.kape.vpnservicemanager.domain.usecases.IClearCache
import com.kape.vpnservicemanager.domain.usecases.IGetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.IIsServiceCleared
import com.kape.vpnservicemanager.domain.usecases.ISetProtocolConfiguration
import com.kape.vpnservicemanager.domain.usecases.ISetServerPeerInformation
import com.kape.vpnservicemanager.domain.usecases.IStartConnection
import com.kape.vpnservicemanager.domain.usecases.IStartReconnectionHandler
import com.kape.vpnservicemanager.domain.usecases.IStopConnection

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

internal class StartConnectionController(
    private val isServiceCleared: IIsServiceCleared,
    private val setProtocolConfiguration: ISetProtocolConfiguration,
    private val setServerPeerInformation: ISetServerPeerInformation,
    private val startConnection: IStartConnection,
    private val startReconnectionHandler: IStartReconnectionHandler,
    private val getServerPeerInformation: IGetServerPeerInformation,
    private val stopConnection: IStopConnection,
    private val clearCache: IClearCache,
) : com.kape.vpnservicemanager.domain.controllers.IStartConnectionController {

    // region IStartConnectionController
    override suspend fun invoke(
        protocolConfiguration: VPNServiceManagerConfiguration,
    ): Result<VPNServiceServerPeerInformation> {
        return isServiceCleared()
            .mapCatching {
                setProtocolConfiguration(
                    protocolConfiguration = protocolConfiguration
                ).getOrFail {
                    handleFailure(it)
                }
            }
            .mapCatching {
                startConnection().getOrFail {
                    handleFailureStoppingConnection(it, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching { serverPeerInformation ->
                setServerPeerInformation(
                    serverPeerInformation = serverPeerInformation
                ).getOrFail {
                    handleFailureStoppingConnection(it, DisconnectReason.SERVER_ERROR)
                }
            }
            .mapCatching {
                startReconnectionHandler().getOrFail {
                    handleFailureStoppingConnection(it, DisconnectReason.CONFIGURATION_ERROR)
                }
            }
            .mapCatching {
                getServerPeerInformation().getOrFail {
                    handleFailureStoppingConnection(it, DisconnectReason.SERVER_ERROR)
                }
            }
    }
    // endregion

    private suspend fun handleFailureStoppingConnection(throwable: Throwable, disconnectReason: DisconnectReason) {
        stopConnection(disconnectReason)
        handleFailure(throwable)
    }

    private suspend fun handleFailure(throwable: Throwable) {
        clearCache()
        throw throwable
    }
}
