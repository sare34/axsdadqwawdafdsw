package com.phoneguardian.security.domain.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.phoneguardian.security.data.local.SecurityPreferences
import com.phoneguardian.security.data.local.dao.ActionLogDao
import com.phoneguardian.security.data.local.entity.ActionLogEntity
import com.phoneguardian.security.domain.model.SecurityFinding
import com.phoneguardian.security.domain.model.Severity
import com.phoneguardian.security.service.shizuku.ShizukuManager

class RemediationManager(
    private val context: Context,
    private val preferences: SecurityPreferences,
    private val actionLogDao: ActionLogDao,
    private val shizukuManager: ShizukuManager
) {

    /**
     * Attempts autonomous remediation where permitted by the platform.
     * Complies strictly with safety rules: NEVER uninstalls or disables an app automatically.
     */
    suspend fun attemptAutonomousRemediation(finding: SecurityFinding): Boolean {
        if (!preferences.isAutonomousRemediationAllowed) {
            return false
        }

        // 1. Shizuku Advanced Mode (if enabled by user and connected)
        if (preferences.isShizukuModeEnabled && shizukuManager.hasShizukuPermission()) {
            if (finding.id == "system_adb_wifi") {
                val result = shizukuManager.disableUnsafeSetting("global", "adb_wifi_enabled")
                val success = result.isSuccess
                logAction(
                    title = "تعطيل تصحيح الأخطاء اللاسلكي تلقائياً (Shizuku)",
                    details = if (success) "تم إيقاف adb_wifi بنجاح للحماية من الاختراق اللاسلكي." else "فشل تنفيذ الأمر عبر Shizuku.",
                    isAutonomous = true,
                    isSuccess = success,
                    target = "system:adb_wifi"
                )
                return success
            }

            if (finding.packageName != null && finding.id.startsWith("behavior_cam_mic_")) {
                // Revoke camera permission automatically
                val res = shizukuManager.revokePermission(finding.packageName, "android.permission.CAMERA")
                val success = res.isSuccess
                logAction(
                    title = "سحب إذن الكاميرا تلقائياً (Shizuku)",
                    details = "تم سحب إذن الكاميرا من التطبيق ${finding.appName ?: finding.packageName} لمنع التجسس في الخلفية.",
                    isAutonomous = true,
                    isSuccess = success,
                    target = finding.packageName
                )
                return success
            }
        }

        // 2. Local VPN Firewall rules (Autonomous on-device IP blocking)
        if (finding.id.startsWith("threat_c2_") || finding.id.startsWith("network_rogue_dns_")) {
            val maliciousIp = finding.id.substringAfterLast("_")
            preferences.addBlockedIp(maliciousIp)
            logAction(
                title = "حظر اتصال مشبوه تلقائياً عبر VPN المحلي",
                details = "تم حظر العنوان IP $maliciousIp على مستوى الجهاز لمنع الاتصال بخادم التحكم الخبيث.",
                isAutonomous = true,
                isSuccess = true,
                target = maliciousIp
            )
            return true
        }

        return false
    }

    suspend fun logAction(
        title: String,
        details: String,
        isAutonomous: Boolean,
        isSuccess: Boolean,
        target: String?
    ) {
        val entity = ActionLogEntity(
            title = title,
            details = details,
            isAutonomous = isAutonomous,
            isSuccess = isSuccess,
            target = target,
            timestamp = System.currentTimeMillis()
        )
        actionLogDao.insertLog(entity)
    }

    /**
     * Prepares deep link intent for manual one-tap user intervention
     */
    fun createRemediationIntent(finding: SecurityFinding): Intent? {
        val action = finding.deepLinkAction ?: return null
        val intent = Intent(action).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            finding.deepLinkData?.let { dataUri ->
                data = Uri.parse(dataUri)
            }
        }
        return intent
    }
}
