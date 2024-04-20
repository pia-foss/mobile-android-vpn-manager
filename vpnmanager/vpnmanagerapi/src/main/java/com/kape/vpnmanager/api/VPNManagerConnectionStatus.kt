package com.kape.vpnmanager.api

/**
 * The list of possible vpn connection states and their associated properties if any.
 */
sealed interface VPNManagerConnectionStatus {
    data class Disconnected(val disconnectReason: DisconnectReason) : VPNManagerConnectionStatus
    data object Connecting : VPNManagerConnectionStatus
    data object Disconnecting : VPNManagerConnectionStatus
    data object Reconnecting : VPNManagerConnectionStatus
    data object Authenticating : VPNManagerConnectionStatus
    data object LinkUp : VPNManagerConnectionStatus
    data object Configuring : VPNManagerConnectionStatus
    data class Connected(val placeholder: String? = null) : VPNManagerConnectionStatus
}
