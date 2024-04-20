package com.kape.vpnservicemanager.domain.usecases

import com.kape.vpnservicemanager.presenters.VPNServiceManagerProtocolTarget

internal interface IGetVpnProtocolLogs {
    suspend operator fun invoke(protocolTarget: VPNServiceManagerProtocolTarget): Result<List<String>>
}
