/*
 *
 *  *  Copyright (c) "2023" Private Internet Access, Inc.
 *  *
 *  *  This file is part of the Private Internet Access Android Client.
 *  *
 *  *  The Private Internet Access Android Client is free software: you can redistribute it and/or
 *  *  modify it under the terms of the GNU General Public License as published by the Free
 *  *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  *  The Private Internet Access Android Client is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  *  details.
 *  *
 *  *  You should have received a copy of the GNU General Public License along with the Private
 *  *  Internet Access Android Client.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.kape.openvpn.data.externals

import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.os.ParcelFileDescriptor
import com.kape.openvpn.domain.usecases.IOpenVpnProcessOutputHandler
import com.kape.openvpn.presenters.OpenVpnError
import com.kape.openvpn.presenters.OpenVpnErrorCode
import com.kape.vpnmanager.api.data.externals.ICoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/*
 *  Copyright (c) 2022 Private Internet Access, Inc.
 *
 *  This file is part of the Private Internet Access Android Client.
 *
 *  The Private Internet Access Android Client is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  The Private Internet Access Android Client is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with the Private
 *  Internet Access Android Client.  If not, see <https://www.gnu.org/licenses/>.
 */

internal class OpenVpnProcessSocket(
    private val coroutineContext: ICoroutineContext,
) : IOpenVpnProcessSocket {

    companion object {
        private const val PROCESS_SIGINT_CMD = "signal SIGINT\n"
    }

    private lateinit var socket: LocalSocket
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private val moduleCoroutineContext: CoroutineContext =
        coroutineContext.getModuleCoroutineContext().getOrThrow()

    // region IOpenVpnProcessSocket
    override fun createSocket(namespace: String): Result<Unit> {
        try {
            socket = LocalSocket()
            socket.connect(LocalSocketAddress(namespace, LocalSocketAddress.Namespace.FILESYSTEM))
            return Result.success(Unit)
        } catch (throwable: Throwable) {
            return Result.failure(
                OpenVpnError(
                    code = OpenVpnErrorCode.SOCKET_CONNECTION_ERROR,
                    error = Error(throwable.message)
                )
            )
        }
    }

    override fun connectSocketInputBuffer(outputHandler: IOpenVpnProcessOutputHandler): Result<Unit> {
        // We are single-threaded. However, in order to handle the stream we need to step away
        // from it, to avoid blocking execution. Let's jump back to the known context
        // when reporting the output via `outputHandler.output`.
        var isConnected = socket.isConnected
        var inputStream = socket.inputStream
        val scope = CoroutineScope(Dispatchers.IO)
        scope.async {
            try {
                while (isConnected) {
                    val buffer = ByteArray(2048)
                    val bytesRead = inputStream.read(buffer)
                    val line = String(buffer, 0, bytesRead, Charsets.UTF_8)
                    withContext(moduleCoroutineContext) {
                        outputHandler.output(line = line)
                    }

                    // Check whether we should continue reading by retrieving the socket
                    // on the dedicated thread.
                    val socket = withContext(moduleCoroutineContext) {
                        socket
                    }
                    isConnected = socket.isConnected
                    inputStream = socket.inputStream
                }
            } catch (throwable: Throwable) {
                // TODO: Announce error via listener/announcer
            }
        }
        return Result.success(Unit)
    }

    override fun write(message: String): Result<Unit> {
        try {
            socket.outputStream.write(message.toByteArray(Charsets.UTF_8))
            socket.outputStream.flush()
            return Result.success(Unit)
        } catch (throwable: Throwable) {
            return Result.failure(
                OpenVpnError(
                    code = OpenVpnErrorCode.SOCKET_CONNECTION_ERROR,
                    error = Error(throwable.message)
                )
            )
        }
    }

    override fun setFileDescriptorsForSend(fd: Int): Result<Unit> {
        try {
            parcelFileDescriptor = ParcelFileDescriptor.adoptFd(fd)
            socket.setFileDescriptorsForSend(
                arrayOf(parcelFileDescriptor?.fileDescriptor)
            )
            return Result.success(Unit)
        } catch (throwable: Throwable) {
            return Result.failure(
                OpenVpnError(
                    code = OpenVpnErrorCode.SOCKET_CONNECTION_ERROR,
                    error = Error(throwable.message)
                )
            )
        }
    }

    override fun getSocketFd(): Result<Int> {
        try {
            val fileDescriptor = socket.ancillaryFileDescriptors.firstOrNull()
                ?: return Result.failure(
                    OpenVpnError(
                        code = OpenVpnErrorCode.SOCKET_CONNECTION_ERROR,
                        error = Error("Failure to get socket fd. Invalid fd return.")
                    )
                )

            val parcelFileDescriptor = ParcelFileDescriptor.dup(fileDescriptor)
            return Result.success(parcelFileDescriptor.fd)
        } catch (throwable: Throwable) {
            return Result.failure(
                OpenVpnError(
                    code = OpenVpnErrorCode.SOCKET_CONNECTION_ERROR,
                    error = Error(throwable.message)
                )
            )
        }
    }

    override fun close(): Result<Unit> {
        try {
            return write(PROCESS_SIGINT_CMD).mapCatching {
                parcelFileDescriptor?.close()
                socket.close()
            }
        } catch (throwable: Throwable) {
            return Result.failure(
                OpenVpnError(
                    code = OpenVpnErrorCode.SOCKET_CONNECTION_ERROR,
                    error = Error(throwable.message)
                )
            )
        }
    }
    // endregion
}
