package com.kape.myapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kape.myapplication.databinding.ActivityMainBinding
import com.kape.vpnmanager.api.OpenVpnSocksProxyDetails
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.DnsInformation
import com.kape.vpnmanager.data.models.OpenVpnClientConfiguration
import com.kape.vpnmanager.data.models.ProtocolCipher
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.data.models.TransportProtocol
import com.kape.vpnmanager.data.models.WireguardClientConfiguration
import com.kape.vpnmanager.presenters.VPNManagerAPI
import com.kape.vpnmanager.presenters.VPNManagerBuilder
import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget
import kotlinx.coroutines.Dispatchers
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val PIA_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n" +
            "MIIHqzCCBZOgAwIBAgIJAJ0u+vODZJntMA0GCSqGSIb3DQEBDQUAMIHoMQswCQYD\n" +
            "VQQGEwJVUzELMAkGA1UECBMCQ0ExEzARBgNVBAcTCkxvc0FuZ2VsZXMxIDAeBgNV\n" +
            "BAoTF1ByaXZhdGUgSW50ZXJuZXQgQWNjZXNzMSAwHgYDVQQLExdQcml2YXRlIElu\n" +
            "dGVybmV0IEFjY2VzczEgMB4GA1UEAxMXUHJpdmF0ZSBJbnRlcm5ldCBBY2Nlc3Mx\n" +
            "IDAeBgNVBCkTF1ByaXZhdGUgSW50ZXJuZXQgQWNjZXNzMS8wLQYJKoZIhvcNAQkB\n" +
            "FiBzZWN1cmVAcHJpdmF0ZWludGVybmV0YWNjZXNzLmNvbTAeFw0xNDA0MTcxNzQw\n" +
            "MzNaFw0zNDA0MTIxNzQwMzNaMIHoMQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0Ex\n" +
            "EzARBgNVBAcTCkxvc0FuZ2VsZXMxIDAeBgNVBAoTF1ByaXZhdGUgSW50ZXJuZXQg\n" +
            "QWNjZXNzMSAwHgYDVQQLExdQcml2YXRlIEludGVybmV0IEFjY2VzczEgMB4GA1UE\n" +
            "AxMXUHJpdmF0ZSBJbnRlcm5ldCBBY2Nlc3MxIDAeBgNVBCkTF1ByaXZhdGUgSW50\n" +
            "ZXJuZXQgQWNjZXNzMS8wLQYJKoZIhvcNAQkBFiBzZWN1cmVAcHJpdmF0ZWludGVy\n" +
            "bmV0YWNjZXNzLmNvbTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBALVk\n" +
            "hjumaqBbL8aSgj6xbX1QPTfTd1qHsAZd2B97m8Vw31c/2yQgZNf5qZY0+jOIHULN\n" +
            "De4R9TIvyBEbvnAg/OkPw8n/+ScgYOeH876VUXzjLDBnDb8DLr/+w9oVsuDeFJ9K\n" +
            "V2UFM1OYX0SnkHnrYAN2QLF98ESK4NCSU01h5zkcgmQ+qKSfA9Ny0/UpsKPBFqsQ\n" +
            "25NvjDWFhCpeqCHKUJ4Be27CDbSl7lAkBuHMPHJs8f8xPgAbHRXZOxVCpayZ2SND\n" +
            "fCwsnGWpWFoMGvdMbygngCn6jA/W1VSFOlRlfLuuGe7QFfDwA0jaLCxuWt/BgZyl\n" +
            "p7tAzYKR8lnWmtUCPm4+BtjyVDYtDCiGBD9Z4P13RFWvJHw5aapx/5W/CuvVyI7p\n" +
            "Kwvc2IT+KPxCUhH1XI8ca5RN3C9NoPJJf6qpg4g0rJH3aaWkoMRrYvQ+5PXXYUzj\n" +
            "tRHImghRGd/ydERYoAZXuGSbPkm9Y/p2X8unLcW+F0xpJD98+ZI+tzSsI99Zs5wi\n" +
            "jSUGYr9/j18KHFTMQ8n+1jauc5bCCegN27dPeKXNSZ5riXFL2XX6BkY68y58UaNz\n" +
            "meGMiUL9BOV1iV+PMb7B7PYs7oFLjAhh0EdyvfHkrh/ZV9BEhtFa7yXp8XR0J6vz\n" +
            "1YV9R6DYJmLjOEbhU8N0gc3tZm4Qz39lIIG6w3FDAgMBAAGjggFUMIIBUDAdBgNV\n" +
            "HQ4EFgQUrsRtyWJftjpdRM0+925Y6Cl08SUwggEfBgNVHSMEggEWMIIBEoAUrsRt\n" +
            "yWJftjpdRM0+925Y6Cl08SWhge6kgeswgegxCzAJBgNVBAYTAlVTMQswCQYDVQQI\n" +
            "EwJDQTETMBEGA1UEBxMKTG9zQW5nZWxlczEgMB4GA1UEChMXUHJpdmF0ZSBJbnRl\n" +
            "cm5ldCBBY2Nlc3MxIDAeBgNVBAsTF1ByaXZhdGUgSW50ZXJuZXQgQWNjZXNzMSAw\n" +
            "HgYDVQQDExdQcml2YXRlIEludGVybmV0IEFjY2VzczEgMB4GA1UEKRMXUHJpdmF0\n" +
            "ZSBJbnRlcm5ldCBBY2Nlc3MxLzAtBgkqhkiG9w0BCQEWIHNlY3VyZUBwcml2YXRl\n" +
            "aW50ZXJuZXRhY2Nlc3MuY29tggkAnS7684Nkme0wDAYDVR0TBAUwAwEB/zANBgkq\n" +
            "hkiG9w0BAQ0FAAOCAgEAJsfhsPk3r8kLXLxY+v+vHzbr4ufNtqnL9/1Uuf8NrsCt\n" +
            "pXAoyZ0YqfbkWx3NHTZ7OE9ZRhdMP/RqHQE1p4N4Sa1nZKhTKasV6KhHDqSCt/dv\n" +
            "Em89xWm2MVA7nyzQxVlHa9AkcBaemcXEiyT19XdpiXOP4Vhs+J1R5m8zQOxZlV1G\n" +
            "tF9vsXmJqWZpOVPmZ8f35BCsYPvv4yMewnrtAC8PFEK/bOPeYcKN50bol22QYaZu\n" +
            "LfpkHfNiFTnfMh8sl/ablPyNY7DUNiP5DRcMdIwmfGQxR5WEQoHL3yPJ42LkB5zs\n" +
            "6jIm26DGNXfwura/mi105+ENH1CaROtRYwkiHb08U6qLXXJz80mWJkT90nr8Asj3\n" +
            "5xN2cUppg74nG3YVav/38P48T56hG1NHbYF5uOCske19F6wi9maUoto/3vEr0rnX\n" +
            "JUp2KODmKdvBI7co245lHBABWikk8VfejQSlCtDBXn644ZMtAdoxKNfR2WTFVEwJ\n" +
            "iyd1Fzx0yujuiXDROLhISLQDRjVVAvawrAtLZWYK31bY7KlezPlQnl/D9Asxe85l\n" +
            "8jO5+0LdJ6VyOs/Hd4w52alDW/MFySDZSfQHMTIc30hLBJ8OnCEIvluVQQ2UQvoW\n" +
            "+no177N9L2Y+M9TcTA62ZyMXShHQGeh20rb4kK8f+iFX8NxtdHVSkxMEFSfDDyQ=\n" +
            "-----END CERTIFICATE-----\n"

        private const val OPENVPN_DROPDOWN_VALUE = "OpenVPN"
        private const val WIREGUARD_DROPDOWN_VALUE = "Wireguard"
    }

    private object DefaultValues {
        const val sessionName: String = "sessionName"
        val protocol: VPNManagerProtocolTarget = VPNManagerProtocolTarget.OPENVPN
        const val certificate: String = PIA_CERTIFICATE
        const val username: String = ""
        const val password: String = ""
        const val allowLocalNetworkAccess: Boolean = false
        const val mtu: Int = 1280
        val dnsInformation: DnsInformation = DnsInformation(
            dnsList = mutableListOf(),
            // if `systemDnsResolverEnabled` is `true`. The above dns list will be replaced by
            // the ones provided via the active interface on runtime.
            // @see `connectivityManager.activeNetwork` below.
            systemDnsResolverEnabled = false
        )

        /**
         * The list of servers is obtained from https://serverlist.piaservers.net/vpninfo/servers/v6
         *
         * If a connection fails or if you need to test other protocols/transport modes, you can
         * pick a relevant server from the URL above.
         */
        val servers: List<ServerList.Server> = listOf(
            ServerList.Server(
                ip = "46.246.3.238",
                port = 80,
                commonOrDistinguishedName = "stockholm406",
                transport = TransportProtocol.TCP,
                ciphers = listOf(ProtocolCipher.AES_128_GCM),
                latency = 10,
                dnsInformation = dnsInformation
            )
        )
        val openVpnSocksProxy: OpenVpnSocksProxyDetails? = null
    }

    private lateinit var clientConfiguration: ClientConfiguration

    private val protocolItems = arrayOf(
        OPENVPN_DROPDOWN_VALUE,
        WIREGUARD_DROPDOWN_VALUE
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val settingsDialog =
            BottomSheetDialog(this@MainActivity).apply { setContentView(R.layout.settings) }
        val uiLogger = UILogger(textViewOutput = binding.textviewOutput)

        val vpnManagerDependencies = VPNManagerDependencies(
            activity = this,
            uiLogger = uiLogger
        )
        val vpnManagerAPI: VPNManagerAPI = VPNManagerBuilder()
            .setContext(this.applicationContext)
            .setClientCoroutineContext(Dispatchers.Main)
            .setDebugLoggingDependency(vpnManagerDependencies)
            .setPermissionsDependency(vpnManagerDependencies)
            .setProtocolByteCountDependency(vpnManagerDependencies)
            .build()

        vpnManagerAPI.addConnectionListener(vpnManagerDependencies) {
            uiLogger.log("AddConnectionListener result: $it")
        }

        binding.connectButton.setOnClickListener {
            prepareClientConfiguration(settingsDialog = settingsDialog)
            vpnManagerAPI.startConnection(clientConfiguration = clientConfiguration) {
                uiLogger.log("StartConnection result: $it")
            }
        }

        binding.disconnectButton.setOnClickListener {
            vpnManagerAPI.stopConnection {
                uiLogger.log("StopConnection result: $it")
            }
        }

        binding.settingsButton.setOnClickListener {
            settingsDialog.show()
        }

        binding.getVpnLogsButton.setOnClickListener {
            vpnManagerAPI.getVpnProtocolLogs(protocolTarget = clientConfiguration.protocolTarget) { logs ->
                val unwrappedLogs = logs.getOrElse {
                    uiLogger.log("Failure retrieving logs: $it")
                    return@getVpnProtocolLogs
                }
                unwrappedLogs.forEach { logLine ->
                    uiLogger.log(logLine)
                }
            }
        }

        binding.clearLogsTextView.setOnClickListener {
            binding.textviewOutput.text = ""
        }

        prepareInitialClientConfiguration()
        prepareProtocolDropdown(settingsDialog = settingsDialog, uiLogger = uiLogger)
        updateSettingsUiWithKnownConfiguration(settingsDialog = settingsDialog)
    }

    // region private
    @RequiresApi(Build.VERSION_CODES.O)
    private fun prepareInitialClientConfiguration() {
        val notificationChannel =
            NotificationChannel("channelId", "channelName", NotificationManager.IMPORTANCE_DEFAULT)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationChannel.importance = NotificationManager.IMPORTANCE_MIN
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(notificationChannel)

        val notificationBuilder = Notification.Builder(this, "channelId")
        val notification = notificationBuilder.setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        val configureIntent = Intent(this, MainActivity::class.java)
        configureIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (DefaultValues.dnsInformation.systemDnsResolverEnabled) {
            val connectivityManager: ConnectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            connectivityManager.getLinkProperties(network)?.dnsServers?.let { dnsServers ->
                dnsServers.firstOrNull()?.hostAddress?.let {
                    (DefaultValues.dnsInformation.dnsList as MutableList).add(it)
                }
            }
        }

        clientConfiguration = ClientConfiguration(
            sessionName = DefaultValues.sessionName,
            configureIntent = PendingIntent.getActivity(
                this,
                0,
                configureIntent,
                PendingIntent.FLAG_IMMUTABLE
            ),
            protocolTarget = DefaultValues.protocol,
            mtu = DefaultValues.mtu,
            notificationId = 123,
            notification = notification,
            allowedApplicationPackages = emptyList(),
            disallowedApplicationPackages = emptyList(),
            allowLocalNetworkAccess = DefaultValues.allowLocalNetworkAccess,
            serverList = ServerList(servers = DefaultValues.servers),
            openVpnClientConfiguration = OpenVpnClientConfiguration(
                caCertificate = DefaultValues.certificate,
                username = DefaultValues.username,
                password = DefaultValues.password,
                socksProxy = DefaultValues.openVpnSocksProxy,
                additionalParameters = ""
            ),
            wireguardClientConfiguration = WireguardClientConfiguration(
                token = "${DefaultValues.username}:${DefaultValues.password}",
                pinningCertificate = DefaultValues.certificate
            )
        )
    }

    private fun prepareClientConfiguration(settingsDialog: BottomSheetDialog) {
        val protocolSpinner = settingsDialog.findViewById<Spinner>(R.id.protocolSpinner)
        val mtuEditText = settingsDialog.findViewById<EditText>(R.id.mtuEditText)
        val dnsEditText = settingsDialog.findViewById<EditText>(R.id.dnsEditText)
        val openVpnUsernameEditText = settingsDialog.findViewById<EditText>(R.id.openVpnUsernameEditText)
        val openVpnPasswordEditText = settingsDialog.findViewById<EditText>(R.id.openVpnPasswordEditText)
        val wireguardTokenEditText = settingsDialog.findViewById<EditText>(R.id.wireguardTokenEditText)

        val protocolTarget = when (protocolSpinner?.selectedItemPosition) {
            0 -> VPNManagerProtocolTarget.OPENVPN
            1 -> VPNManagerProtocolTarget.WIREGUARD
            else -> throw IllegalStateException("Unsupported Operation")
        }

        val dnsList = if (dnsEditText?.text.toString().isEmpty()) {
            DefaultValues.dnsInformation.dnsList
        } else {
            listOf(dnsEditText?.text.toString())
        }
        (DefaultValues.dnsInformation.dnsList as MutableList).clear()
        (DefaultValues.dnsInformation.dnsList as MutableList).addAll(dnsList)

        clientConfiguration = clientConfiguration.copy(
            protocolTarget = protocolTarget,
            mtu = mtuEditText?.text.toString().toInt(),
            openVpnClientConfiguration = clientConfiguration.openVpnClientConfiguration.copy(
                caCertificate = PIA_CERTIFICATE,
                username = openVpnUsernameEditText?.text.toString(),
                password = openVpnPasswordEditText?.text.toString(),
                socksProxy = clientConfiguration.openVpnClientConfiguration.socksProxy
            ),
            wireguardClientConfiguration = clientConfiguration.wireguardClientConfiguration.copy(
                token = wireguardTokenEditText?.text.toString(),
                pinningCertificate = PIA_CERTIFICATE
            )
        )
    }

    private fun prepareProtocolDropdown(
        settingsDialog: BottomSheetDialog,
        uiLogger: UILogger,
    ) {
        val protocolSpinner = settingsDialog.findViewById<Spinner>(R.id.protocolSpinner)
        val protocolAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, protocolItems)
        protocolSpinner?.adapter = protocolAdapter
        protocolSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long,
            ) {
                uiLogger.log("Protocol selected: ${protocolItems[position]}")
                val protocolTarget = when (position) {
                    0 -> VPNManagerProtocolTarget.OPENVPN
                    1 -> VPNManagerProtocolTarget.WIREGUARD
                    else -> throw IllegalStateException("Unsupported Operation")
                }
                clientConfiguration = clientConfiguration.copy(protocolTarget = protocolTarget)
                updateSettingsUiWithKnownConfiguration(settingsDialog = settingsDialog)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do nothing.
            }
        }
    }

    private fun updateSettingsUiWithKnownConfiguration(settingsDialog: BottomSheetDialog) {
        val protocolSpinner = settingsDialog.findViewById<Spinner>(R.id.protocolSpinner)
        val mtuEditText = settingsDialog.findViewById<EditText>(R.id.mtuEditText)
        val dnsEditText = settingsDialog.findViewById<EditText>(R.id.dnsEditText)

        mtuEditText?.setText(clientConfiguration.mtu.toString())
        DefaultValues.dnsInformation.dnsList.firstOrNull()?.let {
            dnsEditText?.setText(it)
        }
        when (clientConfiguration.protocolTarget) {
            VPNManagerProtocolTarget.OPENVPN -> protocolSpinner?.setSelection(0)
            VPNManagerProtocolTarget.WIREGUARD -> protocolSpinner?.setSelection(1)
        }

        val openVpnUsernameEditText = settingsDialog.findViewById<EditText>(R.id.openVpnUsernameEditText)
        val openVpnPasswordEditText = settingsDialog.findViewById<EditText>(R.id.openVpnPasswordEditText)
        openVpnUsernameEditText?.setText(clientConfiguration.openVpnClientConfiguration.username)
        openVpnPasswordEditText?.setText(clientConfiguration.openVpnClientConfiguration.password)

        val wireguardTokenEditText = settingsDialog.findViewById<EditText>(R.id.wireguardTokenEditText)
        wireguardTokenEditText?.setText(clientConfiguration.wireguardClientConfiguration.token)
    }
    // endregion
}
