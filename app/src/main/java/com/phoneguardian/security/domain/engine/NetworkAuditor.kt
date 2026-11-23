package com.phoneguardian.security.domain.engine

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import com.phoneguardian.security.domain.model.FindingCategory
import com.phoneguardian.security.domain.model.SecurityFinding
import com.phoneguardian.security.domain.model.Severity
import java.net.InetAddress
import java.net.NetworkInterface

class NetworkAuditor(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    fun auditNetwork(): List<SecurityFinding> {
        val findings = mutableListOf<SecurityFinding>()

        val activeNetwork = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)

        if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            // Check Wi-Fi Security
            @Suppress("DEPRECATION")
            val connectionInfo: WifiInfo? = wifiManager?.connectionInfo
            val ssid = connectionInfo?.ssid?.replace("\"", "") ?: "الشبكة الحالية"

            // Check if Wi-Fi network has no encryption (Captive or Open)
            // On modern Android, open networks often lack WPA/WPA2/WPA3 capabilities
            if (connectionInfo != null && (connectionInfo.networkId == -1 || ssid == "<unknown ssid>")) {
                // Info restricted or disconnected
            }

            // Check DNS servers for anomalies
            val linkProps = connectivityManager?.getLinkProperties(activeNetwork)
            val dnsServers = linkProps?.dnsServers ?: emptyList()
            for (dns in dnsServers) {
                val hostAddress = dns.hostAddress ?: ""
                // Suspicious non-routable or loopback DNS tampering
                if (hostAddress.startsWith("127.0.0.") && hostAddress != "127.0.0.1") {
                    findings.add(
                        SecurityFinding(
                            id = "network_rogue_dns_$hostAddress",
                            title = "خادم DNS مشبوه أو محول محلياً ($hostAddress)",
                            description = "خادم النطاقات يشير إلى عنوان محلي غير قياسي، مما يشير إلى احتمالية اعتراض أو تزوير خوادم النطاق (DNS Spoofing).",
                            category = FindingCategory.NETWORK,
                            severity = Severity.HIGH,
                            deepLinkAction = Settings.ACTION_WIFI_SETTINGS
                        )
                    )
                }
            }
        }

        // Check if device is on VPN
        val isVpnActive = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false
        if (!isVpnActive) {
            findings.add(
                SecurityFinding(
                    id = "network_no_vpn_shield",
                    title = "حركة المرور المباشرة غير مفلترة بحاجز حماية محلي",
                    description = "جهازك يرسل البيانات دون مراقبة محلية للفحص ومنع الاتصالات الخبيثة الصادرة.",
                    category = FindingCategory.NETWORK,
                    severity = Severity.INFO,
                    canAutoFix = true
                )
            )
        }

        return findings
    }
}
