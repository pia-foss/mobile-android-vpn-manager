package com.kape.vpnmanager.api.data.model

/**
 * Object containing the details of an API failure.
 *
 * @param code `JobErrorCode`.
 * @param error `Error`.
 */
data class JobError(
    val code: JobErrorCode,
    val error: Error? = null,
) : Throwable() {

    /**
     * Enum representing the list of possible API response statuses.
     */
    public enum class JobErrorCode {
        JOB_NOT_FOUND,
    }
}
