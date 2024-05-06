package com.kape.vpnservicemanager.data.externals

import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.kape.vpnmanager.api.DisconnectReason
import com.kape.vpnprotocol.presenters.ServiceConfigurationFileDescriptorProvider
import com.kape.vpnprotocol.presenters.VPNProtocolService
import com.kape.vpnservicemanager.data.models.NetworkDetails
import com.kape.vpnservicemanager.data.models.VPNServiceServerPeerInformation
import com.kape.vpnservicemanager.data.utils.parseIpWithSubnetMask
import com.kape.vpnservicemanager.presenters.VPNServiceManagerError
import com.kape.vpnservicemanager.presenters.VPNServiceManagerErrorCode

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

internal class Service :
    android.net.VpnService(),
    IService,
    VPNProtocolService,
    ServiceConfigurationFileDescriptorProvider {

    private val binder = ServiceBinder()
    private lateinit var protocol: IProtocol
    private lateinit var subnet: ISubnet
    private lateinit var cache: ICache
    private lateinit var onServiceRevoked: () -> Unit

    // region VpnService
    override fun onBind(intent: Intent?): IBinder? {
        return if (intent?.action == SERVICE_INTERFACE) {
            // Passing in a custom binder for SERVICE_INTERFACE action would prevent the #onRevoke() from being called.
            super.onBind(intent)
        } else {
            binder
        }
    }

    override fun onRevoke() {
        onServiceRevoked()
    }
    // endregion

    // region IService
    override fun bootstrap(
        protocol: IProtocol,
        subnet: ISubnet,
        cache: ICache,
        onServiceRevoked: () -> Unit,
    ): Result<Unit> {
        this.protocol = protocol
        this.subnet = subnet
        this.cache = cache
        this.onServiceRevoked = onServiceRevoked
        return Result.success(Unit)
    }

    override suspend fun startConnection(): Result<VPNServiceServerPeerInformation> {
        val allowedIps = getRoutes().getOrElse {
            return Result.failure(it)
        }
        val protocolConfiguration = cache.getProtocolConfiguration().getOrElse {
            return Result.failure(it)
        }

        startForeground(
            protocolConfiguration.notificationId,
            protocolConfiguration.notification
        )
        return protocol.startConnection(
            vpnService = this,
            allowedIps = allowedIps,
            serviceConfigurationFileDescriptorProvider = this
        )
    }

    override suspend fun startReconnection(): Result<Unit> =
        protocol.startReconnection()

    override suspend fun stopConnection(disconnectReason: DisconnectReason): Result<Unit> {
        protocol.stopConnection(disconnectReason).getOrElse {
            return Result.failure(it)
        }
        stopForeground(true)
        stopSelf()
        return Result.success(Unit)
    }
    // endregion

    // region VPNProtocolService
    override fun serviceProtect(socket: Int): Result<Boolean> =
        try {
            Result.success(protect(socket))
        } catch (throwable: Throwable) {
            Result.failure(
                VPNServiceManagerError(
                    code = VPNServiceManagerErrorCode.SERVICE_CONFIGURATION_ERROR,
                    error = Error("Failed to protect socket")
                )
            )
        }
    // endregion

    // region ServiceConfigurationFileDescriptorProvider
    override fun establish(peerIp: String, dnsIp: String?, mtu: Int?, gateway: String): Result<Int> {
        cache.setGateway(gateway)

        val allowedIps = getRoutes().getOrElse {
            return Result.failure(it)
        }

        val protocolConfiguration = cache.getProtocolConfiguration().getOrElse {
            return Result.failure(it)
        }

        val parsedAddress = peerIp.parseIpWithSubnetMask().getOrElse {
            return Result.failure(it)
        }

        val builder = Builder()
        builder.setBlocking(true)
        builder.setSession(protocolConfiguration.sessionName)
        builder.addAddress(parsedAddress.address, parsedAddress.prefix)
        protocolConfiguration.configureIntent?.let { builder.setConfigureIntent(it) }
        builder.setMtu(mtu ?: protocolConfiguration.mtu)

        if (protocolConfiguration.dnsInformation.dnsList.isNotEmpty()) {
            protocolConfiguration.dnsInformation.dnsList.forEach {
                builder.addDnsServer(it)
            }
        } else {
            dnsIp?.let {
                builder.addDnsServer(it)
            }
        }

        allowedIps.forEach {
            builder.addRoute(it.address, it.prefix)
        }

        protocolConfiguration.allowedApplicationPackages.forEach {
            builder.addAllowedApplication(it)
        }

        protocolConfiguration.disallowedApplicationPackages.forEach {
            builder.addDisallowedApplication(it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setUnderlyingNetworks(null)
        }

        return try {
            builder.establish()?.let {
                Result.success(it.detachFd())
            } ?: Result.failure(
                VPNServiceManagerError(
                    code = VPNServiceManagerErrorCode.SERVICE_CONFIGURATION_ERROR,
                    error = Error("Application is not prepared. null `establish`")
                )
            )
        } catch (exception: Exception) {
            Result.failure(
                VPNServiceManagerError(
                    code = VPNServiceManagerErrorCode.SERVICE_CONFIGURATION_ERROR,
                    error = Error(exception.message)
                )
            )
        }
    }
    // endregion

    // region private
    private fun getRoutes(): Result<List<NetworkDetails>> {
        val protocolConfiguration = cache.getProtocolConfiguration().getOrThrow()
        val gateway = cache.getGateway().getOrNull()
        return if (protocolConfiguration.allowLocalNetworkAccess) {
            // By default allow all public IPs through the tunnel.
            var subnets = IPV4_PUBLIC_NETWORKS.toMutableList().apply {
                protocolConfiguration.dnsInformation.dnsList.forEach {
                    add(it.parseIpWithSubnetMask().getOrThrow())
                }
                gateway?.let {
                    add(it.parseIpWithSubnetMask().getOrThrow())
                }
            }.toList()

            // If systemDnsResolverEnabled is enabled. Exclude it from the tunnel.
            if (protocolConfiguration.dnsInformation.systemDnsResolverEnabled) {
                subnets = subnet.excludeIpFromSubnets(
                    subnets,
                    protocolConfiguration.dnsInformation.dnsList.first()
                )
            }

            // If an OpenVPN socks-proxy is defined. Exclude it from the tunnel.
            if (protocolConfiguration.openVpnClientConfiguration.socksProxy != null) {
                subnets = subnet.excludeIpFromSubnets(
                    subnets,
                    protocolConfiguration.openVpnClientConfiguration.socksProxy.serverProxyAddress
                )
            }
            Result.success(subnets)
        } else {
            val result = "0.0.0.0/0".parseIpWithSubnetMask().getOrThrow()
            Result.success(listOf(result))
        }
    }
    // endregion

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class ServiceBinder : Binder() {
        fun getService(): Service = this@Service
    }

    companion object {
        // Intentionally split into smaller subnets to reduce the load on the logic recalculating
        // subnets to exclude the system DNS resolver.
        private val IPV4_PUBLIC_NETWORKS = listOf(
            NetworkDetails("0.0.0.0", 8), NetworkDetails("1.0.0.0", 8),
            NetworkDetails("2.0.0.0", 8), NetworkDetails("3.0.0.0", 8),
            NetworkDetails("4.0.0.0", 8), NetworkDetails("5.0.0.0", 8),
            NetworkDetails("6.0.0.0", 8), NetworkDetails("7.0.0.0", 8),
            NetworkDetails("8.0.0.0", 8), NetworkDetails("9.0.0.0", 8),
            NetworkDetails("11.0.0.0", 8), NetworkDetails("12.0.0.0", 8),
            NetworkDetails("13.0.0.0", 8), NetworkDetails("14.0.0.0", 8),
            NetworkDetails("15.0.0.0", 8), NetworkDetails("16.0.0.0", 8),
            NetworkDetails("17.0.0.0", 8), NetworkDetails("18.0.0.0", 8),
            NetworkDetails("19.0.0.0", 8), NetworkDetails("20.0.0.0", 8),
            NetworkDetails("21.0.0.0", 8), NetworkDetails("22.0.0.0", 8),
            NetworkDetails("23.0.0.0", 8), NetworkDetails("24.0.0.0", 8),
            NetworkDetails("25.0.0.0", 8), NetworkDetails("26.0.0.0", 8),
            NetworkDetails("27.0.0.0", 8), NetworkDetails("28.0.0.0", 8),
            NetworkDetails("29.0.0.0", 8), NetworkDetails("30.0.0.0", 8),
            NetworkDetails("31.0.0.0", 8), NetworkDetails("32.0.0.0", 8),
            NetworkDetails("33.0.0.0", 8), NetworkDetails("34.0.0.0", 8),
            NetworkDetails("35.0.0.0", 8), NetworkDetails("36.0.0.0", 8),
            NetworkDetails("37.0.0.0", 8), NetworkDetails("38.0.0.0", 8),
            NetworkDetails("39.0.0.0", 8), NetworkDetails("40.0.0.0", 8),
            NetworkDetails("41.0.0.0", 8), NetworkDetails("42.0.0.0", 8),
            NetworkDetails("43.0.0.0", 8), NetworkDetails("44.0.0.0", 8),
            NetworkDetails("45.0.0.0", 8), NetworkDetails("46.0.0.0", 8),
            NetworkDetails("47.0.0.0", 8), NetworkDetails("48.0.0.0", 8),
            NetworkDetails("49.0.0.0", 8), NetworkDetails("50.0.0.0", 8),
            NetworkDetails("51.0.0.0", 8), NetworkDetails("52.0.0.0", 8),
            NetworkDetails("53.0.0.0", 8), NetworkDetails("54.0.0.0", 8),
            NetworkDetails("55.0.0.0", 8), NetworkDetails("56.0.0.0", 8),
            NetworkDetails("57.0.0.0", 8), NetworkDetails("58.0.0.0", 8),
            NetworkDetails("59.0.0.0", 8), NetworkDetails("60.0.0.0", 8),
            NetworkDetails("61.0.0.0", 8), NetworkDetails("62.0.0.0", 8),
            NetworkDetails("63.0.0.0", 8), NetworkDetails("64.0.0.0", 8),
            NetworkDetails("65.0.0.0", 8), NetworkDetails("66.0.0.0", 8),
            NetworkDetails("67.0.0.0", 8), NetworkDetails("68.0.0.0", 8),
            NetworkDetails("69.0.0.0", 8), NetworkDetails("70.0.0.0", 8),
            NetworkDetails("71.0.0.0", 8), NetworkDetails("72.0.0.0", 8),
            NetworkDetails("73.0.0.0", 8), NetworkDetails("74.0.0.0", 8),
            NetworkDetails("75.0.0.0", 8), NetworkDetails("76.0.0.0", 8),
            NetworkDetails("77.0.0.0", 8), NetworkDetails("78.0.0.0", 8),
            NetworkDetails("79.0.0.0", 8), NetworkDetails("80.0.0.0", 8),
            NetworkDetails("81.0.0.0", 8), NetworkDetails("82.0.0.0", 8),
            NetworkDetails("83.0.0.0", 8), NetworkDetails("84.0.0.0", 8),
            NetworkDetails("85.0.0.0", 8), NetworkDetails("86.0.0.0", 8),
            NetworkDetails("87.0.0.0", 8), NetworkDetails("88.0.0.0", 8),
            NetworkDetails("89.0.0.0", 8), NetworkDetails("90.0.0.0", 8),
            NetworkDetails("91.0.0.0", 8), NetworkDetails("92.0.0.0", 8),
            NetworkDetails("93.0.0.0", 8), NetworkDetails("94.0.0.0", 8),
            NetworkDetails("95.0.0.0", 8), NetworkDetails("96.0.0.0", 8),
            NetworkDetails("97.0.0.0", 8), NetworkDetails("98.0.0.0", 8),
            NetworkDetails("99.0.0.0", 8), NetworkDetails("100.0.0.0", 8),
            NetworkDetails("101.0.0.0", 8), NetworkDetails("102.0.0.0", 8),
            NetworkDetails("103.0.0.0", 8), NetworkDetails("104.0.0.0", 8),
            NetworkDetails("105.0.0.0", 8), NetworkDetails("106.0.0.0", 8),
            NetworkDetails("107.0.0.0", 8), NetworkDetails("108.0.0.0", 8),
            NetworkDetails("109.0.0.0", 8), NetworkDetails("110.0.0.0", 8),
            NetworkDetails("111.0.0.0", 8), NetworkDetails("112.0.0.0", 8),
            NetworkDetails("113.0.0.0", 8), NetworkDetails("114.0.0.0", 8),
            NetworkDetails("115.0.0.0", 8), NetworkDetails("116.0.0.0", 8),
            NetworkDetails("117.0.0.0", 8), NetworkDetails("118.0.0.0", 8),
            NetworkDetails("119.0.0.0", 8), NetworkDetails("120.0.0.0", 8),
            NetworkDetails("121.0.0.0", 8), NetworkDetails("122.0.0.0", 8),
            NetworkDetails("123.0.0.0", 8), NetworkDetails("124.0.0.0", 8),
            NetworkDetails("125.0.0.0", 8), NetworkDetails("126.0.0.0", 8),
            NetworkDetails("128.0.0.0", 8), NetworkDetails("129.0.0.0", 8),
            NetworkDetails("130.0.0.0", 8), NetworkDetails("131.0.0.0", 8),
            NetworkDetails("132.0.0.0", 8), NetworkDetails("133.0.0.0", 8),
            NetworkDetails("134.0.0.0", 8), NetworkDetails("135.0.0.0", 8),
            NetworkDetails("136.0.0.0", 8), NetworkDetails("137.0.0.0", 8),
            NetworkDetails("138.0.0.0", 8), NetworkDetails("139.0.0.0", 8),
            NetworkDetails("140.0.0.0", 8), NetworkDetails("141.0.0.0", 8),
            NetworkDetails("142.0.0.0", 8), NetworkDetails("143.0.0.0", 8),
            NetworkDetails("144.0.0.0", 8), NetworkDetails("145.0.0.0", 8),
            NetworkDetails("146.0.0.0", 8), NetworkDetails("147.0.0.0", 8),
            NetworkDetails("148.0.0.0", 8), NetworkDetails("149.0.0.0", 8),
            NetworkDetails("150.0.0.0", 8), NetworkDetails("151.0.0.0", 8),
            NetworkDetails("152.0.0.0", 8), NetworkDetails("153.0.0.0", 8),
            NetworkDetails("154.0.0.0", 8), NetworkDetails("155.0.0.0", 8),
            NetworkDetails("156.0.0.0", 8), NetworkDetails("157.0.0.0", 8),
            NetworkDetails("158.0.0.0", 8), NetworkDetails("159.0.0.0", 8),
            NetworkDetails("160.0.0.0", 8), NetworkDetails("161.0.0.0", 8),
            NetworkDetails("162.0.0.0", 8), NetworkDetails("163.0.0.0", 8),
            NetworkDetails("164.0.0.0", 8), NetworkDetails("165.0.0.0", 8),
            NetworkDetails("166.0.0.0", 8), NetworkDetails("167.0.0.0", 8),
            NetworkDetails("168.0.0.0", 8), NetworkDetails("169.0.0.0", 8),
            NetworkDetails("170.0.0.0", 8), NetworkDetails("171.0.0.0", 8),
            NetworkDetails("172.0.0.0", 12), NetworkDetails("172.32.0.0", 11),
            NetworkDetails("172.64.0.0", 10), NetworkDetails("172.128.0.0", 9),
            NetworkDetails("173.0.0.0", 8), NetworkDetails("174.0.0.0", 8),
            NetworkDetails("175.0.0.0", 8), NetworkDetails("176.0.0.0", 8),
            NetworkDetails("177.0.0.0", 8), NetworkDetails("178.0.0.0", 8),
            NetworkDetails("179.0.0.0", 8), NetworkDetails("180.0.0.0", 8),
            NetworkDetails("181.0.0.0", 8), NetworkDetails("182.0.0.0", 8),
            NetworkDetails("183.0.0.0", 8), NetworkDetails("184.0.0.0", 8),
            NetworkDetails("185.0.0.0", 8), NetworkDetails("186.0.0.0", 8),
            NetworkDetails("187.0.0.0", 8), NetworkDetails("188.0.0.0", 8),
            NetworkDetails("189.0.0.0", 8), NetworkDetails("190.0.0.0", 8),
            NetworkDetails("191.0.0.0", 8), NetworkDetails("192.0.0.0", 9),
            NetworkDetails("192.128.0.0", 11), NetworkDetails("192.160.0.0", 13),
            NetworkDetails("192.169.0.0", 16), NetworkDetails("192.170.0.0", 15),
            NetworkDetails("192.172.0.0", 14), NetworkDetails("192.176.0.0", 12),
            NetworkDetails("192.192.0.0", 10), NetworkDetails("193.0.0.0", 8),
            NetworkDetails("194.0.0.0", 8), NetworkDetails("195.0.0.0", 8),
            NetworkDetails("196.0.0.0", 8), NetworkDetails("197.0.0.0", 8),
            NetworkDetails("198.0.0.0", 8), NetworkDetails("199.0.0.0", 8),
            NetworkDetails("200.0.0.0", 8), NetworkDetails("201.0.0.0", 8),
            NetworkDetails("202.0.0.0", 8), NetworkDetails("203.0.0.0", 8),
            NetworkDetails("204.0.0.0", 8), NetworkDetails("205.0.0.0", 8),
            NetworkDetails("206.0.0.0", 8), NetworkDetails("207.0.0.0", 8),
            NetworkDetails("208.0.0.0", 8), NetworkDetails("209.0.0.0", 8),
            NetworkDetails("210.0.0.0", 8), NetworkDetails("211.0.0.0", 8),
            NetworkDetails("212.0.0.0", 8), NetworkDetails("213.0.0.0", 8),
            NetworkDetails("214.0.0.0", 8), NetworkDetails("215.0.0.0", 8),
            NetworkDetails("216.0.0.0", 8), NetworkDetails("217.0.0.0", 8),
            NetworkDetails("218.0.0.0", 8), NetworkDetails("219.0.0.0", 8),
            NetworkDetails("220.0.0.0", 8), NetworkDetails("221.0.0.0", 8),
            NetworkDetails("222.0.0.0", 8), NetworkDetails("223.0.0.0", 8),
            NetworkDetails("224.0.0.0", 8), NetworkDetails("225.0.0.0", 8),
            NetworkDetails("226.0.0.0", 8), NetworkDetails("227.0.0.0", 8),
            NetworkDetails("228.0.0.0", 8), NetworkDetails("229.0.0.0", 8),
            NetworkDetails("230.0.0.0", 8), NetworkDetails("231.0.0.0", 8),
            NetworkDetails("232.0.0.0", 8), NetworkDetails("233.0.0.0", 8),
            NetworkDetails("234.0.0.0", 8), NetworkDetails("235.0.0.0", 8),
            NetworkDetails("236.0.0.0", 8), NetworkDetails("237.0.0.0", 8),
            NetworkDetails("238.0.0.0", 8), NetworkDetails("239.0.0.0", 8),
            NetworkDetails("240.0.0.0", 8), NetworkDetails("241.0.0.0", 8),
            NetworkDetails("242.0.0.0", 8), NetworkDetails("243.0.0.0", 8),
            NetworkDetails("244.0.0.0", 8), NetworkDetails("245.0.0.0", 8),
            NetworkDetails("246.0.0.0", 8), NetworkDetails("247.0.0.0", 8),
            NetworkDetails("248.0.0.0", 8), NetworkDetails("249.0.0.0", 8),
            NetworkDetails("250.0.0.0", 8), NetworkDetails("251.0.0.0", 8),
            NetworkDetails("252.0.0.0", 8), NetworkDetails("253.0.0.0", 8),
            NetworkDetails("254.0.0.0", 8), NetworkDetails("255.0.0.0", 8)
        )
    }
}
