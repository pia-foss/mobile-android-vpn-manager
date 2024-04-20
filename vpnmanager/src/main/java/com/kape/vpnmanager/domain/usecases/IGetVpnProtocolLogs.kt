package com.kape.vpnmanager.usecases

import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget

internal interface IGetVpnProtocolLogs {
    suspend operator fun invoke(protocolTarget: VPNManagerProtocolTarget): Result<List<String>>
}
