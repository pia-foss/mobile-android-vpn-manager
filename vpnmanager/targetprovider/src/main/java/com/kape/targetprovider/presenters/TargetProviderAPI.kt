package com.kape.targetprovider.presenters

import com.kape.targetprovider.data.models.TargetProviderServer
import com.kape.targetprovider.data.models.TargetProviderServerList

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

/**
 * Interface defining the API available to the clients.
 */
public interface TargetProviderAPI {

    /**
     * Based on the give servers list. Provides the optimal server to connect to.
     * Callback invoked on main thread.
     *
     * @param serverList `TargetProviderServerList`.
     * @param callback `TargetProviderResultCallback<TargetProviderServer>`.
     */
    fun getOptimalServer(
        serverList: TargetProviderServerList,
        callback: TargetProviderResultCallback<TargetProviderServer>,
    )
}

/**
 * Object containing the details of an API failure.
 *
 * @param code `TargetProviderErrorCode`.
 * @param error `Error`.
 */
public data class TargetProviderError(
    val code: TargetProviderErrorCode,
    val error: Error? = null,
) : Throwable()

/**
 * Enum representing the list of possible API response statuses.
 */
public enum class TargetProviderErrorCode {
    UNKNOWN_SERVER_LIST,
}

/**
 * It defines the callback structure for an API method without a response object.
 */
public typealias TargetProviderCallback = (Result<Unit>) -> Unit

/**
 * It defines the callback structure for an API method requiring an object in its response.
 */
public typealias TargetProviderResultCallback<T> = (Result<T>) -> Unit
