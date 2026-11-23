package com.phoneguardian.security.data.local

import android.content.Context
import android.content.SharedPreferences

class SecurityPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("phone_guardian_prefs", Context.MODE_PRIVATE)

    var geminiApiKey: String
        get() = prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GEMINI_API_KEY, value.trim()).apply()

    var virusTotalApiKey: String
        get() = prefs.getString(KEY_VIRUSTOTAL_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_VIRUSTOTAL_API_KEY, value.trim()).apply()

    var scanFrequencyHours: Int
        get() = prefs.getInt(KEY_SCAN_FREQUENCY, 1) // default 1 hour
        set(value) = prefs.edit().putInt(KEY_SCAN_FREQUENCY, value).apply()

    var isVpnFirewallEnabled: Boolean
        get() = prefs.getBoolean(KEY_VPN_FIREWALL_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_VPN_FIREWALL_ENABLED, value).apply()

    var isShizukuModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_SHIZUKU_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SHIZUKU_ENABLED, value).apply()

    var isAutonomousRemediationAllowed: Boolean
        get() = prefs.getBoolean(KEY_AUTONOMOUS_REMEDIATION, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTONOMOUS_REMEDIATION, value).apply()

    var blockedIpList: Set<String>
        get() = prefs.getStringSet(KEY_BLOCKED_IPS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_BLOCKED_IPS, value).apply()

    fun addBlockedIp(ip: String) {
        val current = blockedIpList.toMutableSet()
        current.add(ip)
        blockedIpList = current
    }

    companion object {
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_VIRUSTOTAL_API_KEY = "virustotal_api_key"
        private const val KEY_SCAN_FREQUENCY = "scan_frequency_hours"
        private const val KEY_VPN_FIREWALL_ENABLED = "vpn_firewall_enabled"
        private const val KEY_SHIZUKU_ENABLED = "shizuku_enabled"
        private const val KEY_AUTONOMOUS_REMEDIATION = "autonomous_remediation"
        private const val KEY_BLOCKED_IPS = "blocked_ips"
    }
}
