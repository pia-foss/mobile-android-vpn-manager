package com.kape.vpnservicemanager.domain.usecases

import com.kape.vpnservicemanager.data.externals.IProtocol
import com.kape.vpnservicemanager.presenters.VPNServiceManagerProtocolTarget

internal class GetVpnProtocolLogs(
    private val protocol: IProtocol,
) : IGetVpnProtocolLogs {

    // region IGetVpnProtocolLogs
    override suspend fun invoke(
        protocolTarget: VPNServiceManagerProtocolTarget,
    ): Result<List<String>> = runCatching {
        return protocol.getVpnProtocolLogs(protocolTarget = protocolTarget)
    }
    // endregion
}
