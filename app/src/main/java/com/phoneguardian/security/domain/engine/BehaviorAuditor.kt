package com.phoneguardian.security.domain.engine

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.phoneguardian.security.domain.model.FindingCategory
import com.phoneguardian.security.domain.model.SecurityFinding
import com.phoneguardian.security.domain.model.Severity

class BehaviorAuditor(private val context: Context) {

    private val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
    private val packageManager: PackageManager = context.packageManager

    fun auditBehavior(): List<SecurityFinding> {
        val findings = mutableListOf<SecurityFinding>()

        if (appOpsManager == null) return findings

        val installedPackages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        for (app in installedPackages) {
            // Ignore system packages & our app
            if ((app.flags and ApplicationInfo.FLAG_SYSTEM) != 0 || app.packageName == context.packageName) {
                continue
            }

            val uid = app.uid
            val packageName = app.packageName
            val label = packageManager.getApplicationLabel(app).toString()

            // 1. Check background camera access via AppOps
            val cameraOp = AppOpsManager.OPSTR_CAMERA
            val cameraAllowed = checkOpAllowed(cameraOp, uid, packageName)

            // 2. Check record audio / microphone in background
            val micOp = AppOpsManager.OPSTR_RECORD_AUDIO
            val micAllowed = checkOpAllowed(micOp, uid, packageName)

            // 3. Check background fine location
            val locOp = AppOpsManager.OPSTR_FINE_LOCATION
            val locAllowed = checkOpAllowed(locOp, uid, packageName)

            if (cameraAllowed && micAllowed) {
                findings.add(
                    SecurityFinding(
                        id = "behavior_cam_mic_$packageName",
                        title = "تطبيق يمتلك وصولاً متزامناً للكاميرا والميكروفون",
                        description = "التطبيق \"$label\" يمتلك إذناً نشطاً بالوصول للكاميرا وتسجيل الصوت، مما يتيح إمكانية التجسس السمعي والبصري.",
                        category = FindingCategory.BEHAVIOR,
                        severity = Severity.HIGH,
                        packageName = packageName,
                        appName = label,
                        deepLinkAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        deepLinkData = "package:$packageName"
                    )
                )
            } else if (micAllowed) {
                findings.add(
                    SecurityFinding(
                        id = "behavior_mic_$packageName",
                        title = "وصول نشط لتسجيل الصوت للميكروفون",
                        description = "التطبيق \"$label\" قادر على تسجيل الصوت في الخلفية.",
                        category = FindingCategory.BEHAVIOR,
                        severity = Severity.MEDIUM,
                        packageName = packageName,
                        appName = label,
                        deepLinkAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        deepLinkData = "package:$packageName"
                    )
                )
            }
        }

        return findings
    }

    private fun checkOpAllowed(opStr: String, uid: Int, packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val mode = appOpsManager?.unsafeCheckOpNoThrow(opStr, uid, packageName)
                mode == AppOpsManager.MODE_ALLOWED
            } else {
                @Suppress("DEPRECATION")
                val mode = appOpsManager?.checkOpNoThrow(opStr, uid, packageName)
                mode == AppOpsManager.MODE_ALLOWED
            }
        } catch (e: Exception) {
            false
        }
    }
}
