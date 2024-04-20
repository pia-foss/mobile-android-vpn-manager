package com.kape.vpnprotocol.data.externals.wireguard

import com.kape.vpnprotocol.presenters.VPNProtocolError
import com.kape.vpnprotocol.presenters.VPNProtocolErrorCode
import com.wireguard.crypto.KeyPair

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

internal class WireguardKeyPair : IWireguardKeyPair {

    private var keyPair: KeyPair? = null

    // region IWireguardKeyPair
    override fun generateKeyPair(): Result<IWireguardKeyPair> {
        this.keyPair = KeyPair()
        return Result.success(this)
    }

    override fun getPublicKeyBase64(): Result<String> =
        keyPair?.let {
            Result.success(it.publicKey.toBase64())
        } ?: Result.failure(VPNProtocolError(code = VPNProtocolErrorCode.KEY_PAIR_NOT_READY))

    override fun getPublicKeyHex(): Result<String> =
        keyPair?.let {
            Result.success(it.publicKey.toHex())
        } ?: Result.failure(VPNProtocolError(code = VPNProtocolErrorCode.KEY_PAIR_NOT_READY))

    override fun getPrivateKeyHex(): Result<String> =
        keyPair?.let {
            Result.success(it.privateKey.toHex())
        } ?: Result.failure(VPNProtocolError(code = VPNProtocolErrorCode.KEY_PAIR_NOT_READY))
    // endregion
}
