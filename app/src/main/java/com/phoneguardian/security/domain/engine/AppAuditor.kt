package com.phoneguardian.security.domain.engine

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.phoneguardian.security.domain.model.FindingCategory
import com.phoneguardian.security.domain.model.SecurityFinding
import com.phoneguardian.security.domain.model.Severity
import java.io.File
import java.security.MessageDigest

class AppAuditor(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    fun auditInstalledApps(): List<SecurityFinding> {
        val findings = mutableListOf<SecurityFinding>()

        val flags = PackageManager.GET_PERMISSIONS or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    PackageManager.GET_SIGNING_CERTIFICATES
                } else {
                    @Suppress("DEPRECATION")
                    PackageManager.GET_SIGNATURES
                }

        val installedPackages: List<PackageInfo> = try {
            packageManager.getInstalledPackages(flags)
        } catch (e: Exception) {
            emptyList()
        }

        // Active device admins
        val activeAdmins: List<ComponentName> = try {
            devicePolicyManager?.activeAdmins ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        val adminPackages = activeAdmins.map { it.packageName }.toSet()

        // Notification listener setting
        val enabledNotificationListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: ""

        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val launcherPackages = packageManager.queryIntentActivities(launcherIntent, 0)
            .map { it.activityInfo.packageName }
            .toSet()

        val now = System.currentTimeMillis()
        val twoDaysAgo = now - (48 * 60 * 60 * 1000L)

        for (pkg in installedPackages) {
            val packageName = pkg.packageName
            val appInfo = pkg.applicationInfo ?: continue

            // Exclude Phone Guardian itself from threat flagging
            if (packageName == context.packageName) continue

            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val appLabel = packageManager.getApplicationLabel(appInfo).toString()
            val permissions = pkg.requestedPermissions?.toList() ?: emptyList()

            // 1. Triple Threat Combo: SMS/Call Logs + Accessibility + Overlay (Banking Trojan / Spyware pattern)
            val hasSms = permissions.any { it.contains("SMS") || it.contains("READ_CALL_LOG") }
            val hasOverlay = permissions.contains("android.permission.SYSTEM_ALERT_WINDOW")
            val hasAccessibility = permissions.contains("android.permission.BIND_ACCESSIBILITY_SERVICE")

            if (!isSystemApp && hasSms && (hasOverlay || hasAccessibility)) {
                findings.add(
                    SecurityFinding(
                        id = "threat_combo_$packageName",
                        title = "نمط برمجي مشبوه عالي الخطورة (Triple Threat)",
                        description = "التطبيق \"$appLabel\" يجمع بين صلاحيات الرسائل الحساسة وإمكانية العرض فوق التطبيقات أو خدمات الوصول، وهو نمط شائع لبرمجيات سرقة الحسابات المصرفية.",
                        category = FindingCategory.APPS,
                        severity = Severity.CRITICAL,
                        packageName = packageName,
                        appName = appLabel,
                        deepLinkAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        deepLinkData = "package:$packageName",
                        canAutoFix = false
                    )
                )
            }

            // 2. Hidden App (No Launcher Icon + Non-System)
            if (!isSystemApp && !launcherPackages.contains(packageName)) {
                // Ignore standard input methods or known services if user installed
                findings.add(
                    SecurityFinding(
                        id = "hidden_app_$packageName",
                        title = "تطبيق مخفي بدون واجهة تشغيل (Hidden App)",
                        description = "التطبيق \"$appLabel\" مثبت بدون أي أيقونة على الشاشة الرئيسية مما يمكنه من العمل خفية.",
                        category = FindingCategory.APPS,
                        severity = Severity.HIGH,
                        packageName = packageName,
                        appName = appLabel,
                        deepLinkAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        deepLinkData = "package:$packageName"
                    )
                )
            }

            // 3. Device Administrator Abuse
            if (adminPackages.contains(packageName) && !isSystemApp) {
                findings.add(
                    SecurityFinding(
                        id = "device_admin_$packageName",
                        title = "صلاحيات مسؤول الجهاز نشطة (Device Admin)",
                        description = "التطبيق \"$appLabel\" يمتلك صلاحيات إدارة الجهاز كاملة، مما يمنعه من الحذف العادي ويتيح له قفل الهاتف أو محوه.",
                        category = FindingCategory.APPS,
                        severity = Severity.HIGH,
                        packageName = packageName,
                        appName = appLabel,
                        deepLinkAction = Settings.ACTION_SECURITY_SETTINGS
                    )
                )
            }

            // 4. Notification Listener Access (Can read 2FA / OTP codes)
            if (!isSystemApp && enabledNotificationListeners.contains(packageName)) {
                findings.add(
                    SecurityFinding(
                        id = "notif_listener_$packageName",
                        title = "الوصول لقراءة الإشعارات ورموز التحقق (2FA/OTP)",
                        description = "التطبيق \"$appLabel\" مخوّل بقراءة كافة إشعاراتك، مما يسمح له باعتراض رموز التحقق البنكية والرسائل الخاصة.",
                        category = FindingCategory.APPS,
                        severity = Severity.HIGH,
                        packageName = packageName,
                        appName = appLabel,
                        deepLinkAction = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                    )
                )
            }

            // 5. Battery Optimization Bypassed (Persistent Background Execution)
            if (!isSystemApp && powerManager?.isIgnoringBatteryOptimizations(packageName) == true) {
                findings.add(
                    SecurityFinding(
                        id = "battery_bypass_$packageName",
                        title = "تجاوز إدارة استهلاك البطارية (Always-On)",
                        description = "التطبيق \"$appLabel\" مستثنى من توفير البطارية ويعمل في الخلفية بصورة غير مقيدة.",
                        category = FindingCategory.APPS,
                        severity = Severity.LOW,
                        packageName = packageName,
                        appName = appLabel,
                        deepLinkAction = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                    )
                )
            }

            // 6. Recently Installed / Sideloaded Non-Store Apps
            if (!isSystemApp && pkg.firstInstallTime > twoDaysAgo) {
                val installer = getInstallerPackageName(packageName)
                val isSideloaded = installer == null || !installer.contains("vending") // com.android.vending is Play Store

                if (isSideloaded) {
                    findings.add(
                        SecurityFinding(
                            id = "recent_sideload_$packageName",
                            title = "تطبيق جديد من مصدر خارجي غير رسمي",
                            description = "تم تثبيت \"$appLabel\" مؤخراً من مصدر خارجي دون فحص متجر التطبيقات الرسمي.",
                            category = FindingCategory.APPS,
                            severity = Severity.MEDIUM,
                            packageName = packageName,
                            appName = appLabel,
                            deepLinkAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            deepLinkData = "package:$packageName"
                        )
                    )
                }
            }
        }

        return findings
    }

    private fun getInstallerPackageName(packageName: String): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstallerPackageName(packageName)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun computeApkSha256(packageName: String): String? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val file = File(appInfo.sourceDir)
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            null
        }
    }
}
