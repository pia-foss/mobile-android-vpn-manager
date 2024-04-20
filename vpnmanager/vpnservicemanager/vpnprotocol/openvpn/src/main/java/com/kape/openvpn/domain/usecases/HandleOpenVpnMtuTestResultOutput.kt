package com.kape.openvpn.domain.usecases

private val REGEX_MTU_TEST_COMPLETE = Regex("empirical mtu test completed \\[tried,actual] local->remote=\\[[0-9]+,([0-9]+)] remote->local=\\[[0-9]+,([0-9]+)]")

internal class HandleOpenVpnMtuTestResultOutput(
    private val openVpnMtuTestResultAnnouncer: IOpenVpnMtuTestResultAnnouncer,
) : IHandleOpenVpnMtuTestResultOutput {

    override fun invoke(line: String): Result<Unit> {
        return runCatching {
            val match = REGEX_MTU_TEST_COMPLETE.find(line, startIndex = 0) ?: throw IllegalArgumentException("Mtu Regex: No match found")
            val localToRemote = match.groupValues[1].toInt()
            val remoteToLocal = match.groupValues[2].toInt()

            openVpnMtuTestResultAnnouncer.onMtuTestResult(localToRemote = localToRemote, remoteToLocal = remoteToLocal)
        }
    }
}
