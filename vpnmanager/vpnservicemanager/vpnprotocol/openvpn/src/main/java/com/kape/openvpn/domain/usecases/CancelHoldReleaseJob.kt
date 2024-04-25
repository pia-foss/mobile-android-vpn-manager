package com.kape.openvpn.domain.usecases

import com.kape.vpnmanager.api.data.externals.IJob

internal class CancelHoldReleaseJob(
    private val job: IJob,
) : ICancelHoldReleaseJob {

    // region ICancelHoldReleaseJob
    override suspend fun invoke(): Result<Unit> =
        job.cancel()
    // endregion
}
