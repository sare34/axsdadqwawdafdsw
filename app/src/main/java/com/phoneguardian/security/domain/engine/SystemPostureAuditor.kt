package com.phoneguardian.security.domain.engine

import android.app.KeyguardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import com.phoneguardian.security.domain.model.FindingCategory
import com.phoneguardian.security.domain.model.SecurityFinding
import com.phoneguardian.security.domain.model.Severity
import java.io.File
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SystemPostureAuditor(private val context: Context) {

    private val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    fun auditSystemPosture(): List<SecurityFinding> {
        val findings = mutableListOf<SecurityFinding>()

        // 1. Screen Lock & Device Security
        val isSecure = keyguardManager?.isDeviceSecure ?: false
        if (!isSecure) {
            findings.add(
                SecurityFinding(
                    id = "system_no_screen_lock",
                    title = "شاشة القفل غير محمية برمز أو بصمة",
                    description = "جهازك غير محمي برمز مرور أو بصمة، مما يتيح لأي شخص يمسك بالهاتف الوصول المباشر لبياناتك وصورك وحساباتك.",
                    category = FindingCategory.SYSTEM,
                    severity = Severity.HIGH,
                    deepLinkAction = Settings.ACTION_SECURITY_SETTINGS
                )
            )
        }

        // 2. USB Debugging Enabled (ADB)
        val adbEnabled = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        if (adbEnabled) {
            findings.add(
                SecurityFinding(
                    id = "system_adb_enabled",
                    title = "تصحيح أخطاء USB (ADB) مفعّل",
                    description = "تمكين وضع تصحيح الأخطاء يتيح لأي حاسوب أو كابل شحن خبيث تنفيذ أوامر برمجية على جهازك وسحب البيانات دون إذنك.",
                    category = FindingCategory.SYSTEM,
                    severity = Severity.HIGH,
                    deepLinkAction = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
                )
            )
        }

        // 3. Developer Options Enabled
        val devOptionsEnabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) == 1
        if (devOptionsEnabled) {
            findings.add(
                SecurityFinding(
                    id = "system_dev_options",
                    title = "خيارات المطورين مفعلة",
                    description = "تفعيل خيارات المطورين يقلل من قيود الحماية لنظام أندرويد ويزيد من مساحة الهجوم.",
                    category = FindingCategory.SYSTEM,
                    severity = Severity.LOW,
                    deepLinkAction = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
                )
            )
        }

        // 4. ADB over Wi-Fi Enabled
        val adbWifi = try {
            Settings.Global.getInt(context.contentResolver, "adb_wifi_enabled", 0) == 1
        } catch (e: Exception) {
            false
        }
        if (adbWifi) {
            findings.add(
                SecurityFinding(
                    id = "system_adb_wifi",
                    title = "تصحيح الأخطاء عبر الواي فاي (Wireless ADB) مفعّل",
                    description = "إمكانية الاتصال بجهازك عبر الشبكة اللاسلكية وتنفيذ أوامر النظام نشطة، وهو خطر أمني فادح على الشبكات المشتركة.",
                    category = FindingCategory.SYSTEM,
                    severity = Severity.CRITICAL,
                    deepLinkAction = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
                )
            )
        }

        // 5. Outdated Security Patch Level
        checkSecurityPatch(findings)

        // 6. User-Installed CA Certificates (Root SSL Interception)
        checkUserCaCertificates(findings)

        // 7. Private DNS Status (DNS Encryption)
        checkPrivateDns(findings)

        // 8. Unknown Sources / Sideloading
        checkUnknownSources(findings)

        return findings
    }

    private fun checkSecurityPatch(findings: MutableList<SecurityFinding>) {
        val patch = Build.VERSION.SECURITY_PATCH
        if (!patch.isNullOrBlank()) {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val patchDate = format.parse(patch)
                val now = Date()
                if (patchDate != null) {
                    val diffMonths = (now.time - patchDate.time) / (1000L * 60 * 60 * 24 * 30)
                    if (diffMonths >= 6) {
                        findings.add(
                            SecurityFinding(
                                id = "system_patch_outdated",
                                title = "تحديث الأمان متأخر (أكثر من 6 أشهر)",
                                description = "تاريخ التحديث الأمني للجهاز هو $patch. هناك ثغرات معروفة في النواة والنظام تم إصلاحها في التحديثات الأحدث.",
                                category = FindingCategory.SYSTEM,
                                severity = Severity.MEDIUM,
                                deepLinkAction = Settings.ACTION_SYSTEM_UPDATE_SETTINGS
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }
    }

    private fun checkUserCaCertificates(findings: MutableList<SecurityFinding>) {
        try {
            val keyStore = KeyStore.getInstance("AndroidCAStore")
            keyStore.load(null, null)
            val aliases = keyStore.aliases()
            var userCertCount = 0
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()
                if (alias.startsWith("user:")) {
                    userCertCount++
                }
            }
            if (userCertCount > 0) {
                findings.add(
                    SecurityFinding(
                        id = "system_user_ca_certs",
                        title = "شهادات مصادقة جذرية يدوية ($userCertCount شهادة)",
                        description = "تم اكتشاف شهادات CA مثبتة يدوياً للمستخدم، مما يسمح باعتراض وفك تشفير حركة مرور HTTPS لجميع التطبيقات (هجمات MITM).",
                        category = FindingCategory.SYSTEM,
                        severity = Severity.HIGH,
                        deepLinkAction = Settings.ACTION_SECURITY_SETTINGS
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore keystore load errors
        }
    }

    private fun checkPrivateDns(findings: MutableList<SecurityFinding>) {
        val privateDnsMode = Settings.Global.getString(context.contentResolver, "private_dns_mode")
        if (privateDnsMode == "off" || privateDnsMode.isNullOrBlank()) {
            findings.add(
                SecurityFinding(
                    id = "system_private_dns_off",
                    title = "نظام النطاقات الآمن (Private DNS) معطل",
                    description = "استعلامات أسماء المواقع ترسل بنص غير مشفر، مما يمكن مزودي خدمة الإنترنت والمتلصصين على الشبكة من رؤية سجل تصفحك.",
                    category = FindingCategory.SYSTEM,
                    severity = Severity.LOW,
                    deepLinkAction = Settings.ACTION_WIRELESS_SETTINGS
                )
            )
        }
    }

    private fun checkUnknownSources(findings: MutableList<SecurityFinding>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            val installNonMarket = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.INSTALL_NON_MARKET_APPS,
                0
            ) == 1
            if (installNonMarket) {
                findings.add(
                    SecurityFinding(
                        id = "system_install_unknown",
                        title = "تثبيت التطبيقات من مصادر غير معروفة متاح للجميع",
                        description = "النظام يسمح بتثبيت التطبيقات من أي مكان دون قيود أمنية.",
                        category = FindingCategory.SYSTEM,
                        severity = Severity.MEDIUM,
                        deepLinkAction = Settings.ACTION_SECURITY_SETTINGS
                    )
                )
            }
        }
    }
}
