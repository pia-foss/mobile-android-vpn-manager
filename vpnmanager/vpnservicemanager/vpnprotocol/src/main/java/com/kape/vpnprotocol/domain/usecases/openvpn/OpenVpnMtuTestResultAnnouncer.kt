package com.kape.vpnprotocol.domain.usecases.openvpn

import com.kape.openvpn.domain.usecases.IOpenVpnMtuTestResultAnnouncer
import com.kape.vpnprotocol.presenters.VPNProtocolConnectivityStatusChangeCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class OpenVpnMtuTestResultAnnouncer(
    private val connectivityStatusChangeCallback: VPNProtocolConnectivityStatusChangeCallback,
    private val clientCoroutineContext: CoroutineContext,
) : IOpenVpnMtuTestResultAnnouncer {

    override fun onMtuTestResult(localToRemote: Int, remoteToLocal: Int) {
        CoroutineScope(clientCoroutineContext).launch {
            connectivityStatusChangeCallback.handleMtuTestResult(
                localToRemote = localToRemote,
                remoteToLocal = remoteToLocal
            )
        }
    }
}
