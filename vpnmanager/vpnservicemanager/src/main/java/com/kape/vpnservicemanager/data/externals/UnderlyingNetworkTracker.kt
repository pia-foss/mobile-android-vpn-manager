package com.kape.vpnservicemanager.data.externals

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.os.Build
import android.util.Log

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

/**
 * Declares the VPN's underlying network on Android 7.0–9 (API 24–28).
 *
 * On Android 10+ a VPN that passes `null` as underlying networks follows the system default
 * network, and the OS copies that network's transports and metered state onto the VPN.
 * Android 9 and lower don't: the VPN is exposed as `TRANSPORT_VPN` only and metered. Apps that
 * check `hasTransport(WIFI | CELLULAR | ETHERNET)` then think the device is offline
 * (e.g. Fire TV on Fire OS 7, see KM-19469).
 *
 * Two Android 9 details shape this class:
 * - Networks passed to [VpnService.Builder.setUnderlyingNetworks] are not reflected in the VPN's
 *   capabilities. Only [VpnService.setUnderlyingNetworks] on the established VPN updates them,
 *   so [onEstablished] must be called after `establish()` succeeds.
 * - The default network callback follows the app's own default network, which is our VPN once
 *   it is up. So instead this tracks all non-VPN internet networks and picks the best one.
 */
internal class UnderlyingNetworkTracker(
    private val vpnService: VpnService,
) {

    /** Non-VPN internet networks currently available, with their latest capabilities. */
    private val networks = LinkedHashMap<Network, NetworkCapabilities>()

    @Volatile
    private var selectedNetwork: Network? = null
    private var callback: ConnectivityManager.NetworkCallback? = null

    private val connectivityManager: ConnectivityManager
        get() = vpnService.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Starts tracking. Call before the tunnel is established so a network is usually known by
     * the time [underlyingNetworks] is read. Safe to call repeatedly.
     */
    @Synchronized
    fun start() {
        Log.d(TAG, "start: sdk=${Build.VERSION.SDK_INT} needed=${isNeeded()} alreadyRegistered=${callback != null}")
        if (!isNeeded() || callback != null) return

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                // Called right after onAvailable as well, so this alone keeps the map current.
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return
                synchronized(this@UnderlyingNetworkTracker) { networks[network] = capabilities }
                reselect("onCapabilitiesChanged $network")
            }

            override fun onLost(network: Network) {
                synchronized(this@UnderlyingNetworkTracker) { networks.remove(network) }
                reselect("onLost $network")
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            .build()

        runCatching { connectivityManager.registerNetworkCallback(request, networkCallback) }
            .onSuccess { callback = networkCallback }
            .onFailure { Log.e(TAG, "registerNetworkCallback failed", it) }
    }

    /** Stops tracking. Call when the connection stops, the service is revoked or destroyed. */
    @Synchronized
    fun stop() {
        Log.d(TAG, "stop: registered=${callback != null}")
        callback?.let { runCatching { connectivityManager.unregisterNetworkCallback(it) } }
        callback = null
        networks.clear()
        selectedNetwork = null
    }

    /**
     * Value for [VpnService.Builder.setUnderlyingNetworks]. Always `null` on Android 10+
     * (follow the default network, the OS fills in its capabilities).
     */
    fun underlyingNetworks(): Array<Network>? {
        if (!isNeeded()) return null
        return selectedNetwork?.let { arrayOf(it) }
            .also { Log.d(TAG, "underlyingNetworks: ${it?.joinToString()}") }
    }

    /**
     * Call after `establish()` succeeded. On Android 9 the buiglder value is not applied to the
     * VPN's capabilities, so it has to be declared again on the established VPN.
     */
    fun onEstablished() {
        applyToService("onEstablished")
    }

    private fun reselect(reason: String) {
        val best = synchronized(this) { networks.entries.maxByOrNull { (_, caps) -> score(caps) }?.key }
        if (best == selectedNetwork) return
        selectedNetwork = best
        applyToService(reason)
    }

    /**
     * Mirrors the platform's default network preference closely enough for our purpose:
     * validated first, then Ethernet over Wi-Fi over cellular.
     */
    private fun score(caps: NetworkCapabilities): Int {
        var score = 0
        if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) score += 100
        score += when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> 30
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> 20
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> 10
            else -> 0
        }
        return score
    }

    private fun applyToService(reason: String) {
        if (!isNeeded()) return
        val networks = underlyingNetworks()
        runCatching { vpnService.setUnderlyingNetworks(networks) }
            .onSuccess { Log.d(TAG, "$reason: setUnderlyingNetworks(${networks?.joinToString()}) -> $it") }
            .onFailure { Log.e(TAG, "$reason: setUnderlyingNetworks failed", it) }
    }

    private fun isNeeded(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    private companion object {
        const val TAG = "UnderlyingNetwork"
    }
}
