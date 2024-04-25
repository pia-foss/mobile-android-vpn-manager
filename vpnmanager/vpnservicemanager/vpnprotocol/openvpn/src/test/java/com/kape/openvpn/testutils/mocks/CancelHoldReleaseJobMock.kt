package com.kape.openvpn.testutils.mocks

import com.kape.openvpn.domain.usecases.ICancelHoldReleaseJob
import com.kape.openvpn.presenters.OpenVpnError
import com.kape.openvpn.presenters.OpenVpnErrorCode

internal class CancelHoldReleaseJobMock(
    private val succeed: Boolean,
) : ICancelHoldReleaseJob {

    // region ICancelHoldReleaseJob
    override suspend fun invoke(): Result<Unit> {
        return if (succeed) {
            Result.success(Unit)
        } else {
            Result.failure(OpenVpnError(code = OpenVpnErrorCode.HOLD_RELEASE_JOB_UNKNOWN))
        }
    }
    // endregion
}
