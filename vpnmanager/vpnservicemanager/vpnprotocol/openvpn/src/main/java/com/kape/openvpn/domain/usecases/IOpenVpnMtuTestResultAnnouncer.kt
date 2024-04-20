package com.kape.openvpn.domain.usecases

interface IOpenVpnMtuTestResultAnnouncer {
    fun onMtuTestResult(localToRemote: Int, remoteToLocal: Int)
}
