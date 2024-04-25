package com.kape.openvpn.domain.usecases

interface ICancelHoldReleaseJob {
    suspend operator fun invoke(): Result<Unit>
}
