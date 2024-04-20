package com.kape.openvpn.domain.usecases

internal interface IHandleOpenVpnMtuTestResultOutput {

    operator fun invoke(line: String): Result<Unit>
}
