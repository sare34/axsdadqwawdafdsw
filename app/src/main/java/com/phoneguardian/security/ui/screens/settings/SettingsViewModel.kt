package com.phoneguardian.security.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.phoneguardian.security.PhoneGuardianApp
import com.phoneguardian.security.service.LocalVpnFirewallService
import com.phoneguardian.security.service.SecurityScanWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PhoneGuardianApp
    private val prefs = app.preferences

    val geminiApiKey = MutableStateFlow(prefs.geminiApiKey)
    val virusTotalApiKey = MutableStateFlow(prefs.virusTotalApiKey)
    val scanFrequencyHours = MutableStateFlow(prefs.scanFrequencyHours)
    val isVpnEnabled = MutableStateFlow(prefs.isVpnFirewallEnabled)
    val isShizukuEnabled = MutableStateFlow(prefs.isShizukuModeEnabled)
    val isAutonomousFixEnabled = MutableStateFlow(prefs.isAutonomousRemediationAllowed)

    private val _shizukuStatusText = MutableStateFlow("")
    val shizukuStatusText: StateFlow<String> = _shizukuStatusText.asStateFlow()

    init {
        checkShizukuStatus()
    }

    fun checkShizukuStatus() {
        val available = app.shizukuManager.isShizukuAvailable()
        val granted = app.shizukuManager.hasShizukuPermission()
        _shizukuStatusText.value = when {
            !available -> "خدمة Shizuku غير مثبتة أو غير نشطة"
            !granted -> "خدمة Shizuku نشطة لكن الصلاحية لم تُمنح"
            else -> "خدمة Shizuku نشطة وتعمل بصلاحيات مرتفعة"
        }
    }

    fun saveGeminiApiKey(key: String) {
        prefs.geminiApiKey = key
        geminiApiKey.value = key
    }

    fun saveVirusTotalApiKey(key: String) {
        prefs.virusTotalApiKey = key
        virusTotalApiKey.value = key
    }

    fun setScanFrequency(hours: Int) {
        prefs.scanFrequencyHours = hours
        scanFrequencyHours.value = hours
        SecurityScanWorker.schedule(getApplication(), hours.toLong())
    }

    fun toggleVpn(enable: Boolean) {
        prefs.isVpnFirewallEnabled = enable
        isVpnEnabled.value = enable
        if (enable) {
            LocalVpnFirewallService.start(getApplication())
        } else {
            LocalVpnFirewallService.stop(getApplication())
        }
    }

    fun toggleShizuku(enable: Boolean) {
        prefs.isShizukuModeEnabled = enable
        isShizukuEnabled.value = enable
        if (enable) {
            app.shizukuManager.requestShizukuPermission(1001)
            checkShizukuStatus()
        }
    }

    fun toggleAutonomousFix(enable: Boolean) {
        prefs.isAutonomousRemediationAllowed = enable
        isAutonomousFixEnabled.value = enable
    }
}
