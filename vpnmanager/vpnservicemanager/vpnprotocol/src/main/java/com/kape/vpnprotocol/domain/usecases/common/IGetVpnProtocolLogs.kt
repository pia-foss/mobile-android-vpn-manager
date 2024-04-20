package com.kape.vpnprotocol.domain.usecases.common

import com.kape.vpnprotocol.presenters.VPNProtocolTarget

internal interface IGetVpnProtocolLogs {
    suspend operator fun invoke(protocolTarget: VPNProtocolTarget): Result<List<String>>
}
