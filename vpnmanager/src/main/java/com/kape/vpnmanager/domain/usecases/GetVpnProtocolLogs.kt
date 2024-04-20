package com.kape.vpnmanager.usecases

import com.kape.vpnmanager.data.externals.IServiceManager
import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget

internal class GetVpnProtocolLogs(
    private val serviceManager: IServiceManager,
) : IGetVpnProtocolLogs {

    // region IGetVpnProtocolLogs
    override suspend fun invoke(protocolTarget: VPNManagerProtocolTarget): Result<List<String>> =
        serviceManager.getVpnProtocolLogs(protocolTarget = protocolTarget)
    // endregion
}
