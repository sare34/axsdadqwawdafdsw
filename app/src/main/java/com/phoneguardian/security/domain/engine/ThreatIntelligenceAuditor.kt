package com.phoneguardian.security.domain.engine

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import com.phoneguardian.security.data.local.dao.ThreatRulesDao
import com.phoneguardian.security.data.remote.VirusTotalApiClient
import com.phoneguardian.security.domain.model.FindingCategory
import com.phoneguardian.security.domain.model.SecurityFinding
import com.phoneguardian.security.domain.model.Severity

class ThreatIntelligenceAuditor(
    private val context: Context,
    private val threatRulesDao: ThreatRulesDao,
    private val appAuditor: AppAuditor,
    private val virusTotalApiClient: VirusTotalApiClient
) {

    suspend fun auditThreatIntel(): List<SecurityFinding> {
        val findings = mutableListOf<SecurityFinding>()
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(0)

        // 1. Check local malware rules
        val rules = threatRulesDao.getRulesByType("PACKAGE_NAME")
        val ruleMap = rules.associateBy { it.pattern }

        for (app in installedApps) {
            val pkg = app.packageName
            val label = pm.getApplicationLabel(app).toString()

            val matchingRule = ruleMap[pkg]
            if (matchingRule != null) {
                findings.add(
                    SecurityFinding(
                        id = "threat_intel_${matchingRule.ruleId}_$pkg",
                        title = "تطابق مع قاعدة بيانات التهديدات: ${matchingRule.threatName}",
                        description = "${matchingRule.description} (تم تحديد التطبيق: $label)",
                        category = FindingCategory.THREAT_INTEL,
                        severity = try { Severity.valueOf(matchingRule.severity) } catch (e: Exception) { Severity.CRITICAL },
                        packageName = pkg,
                        appName = label,
                        deepLinkAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        deepLinkData = "package:$pkg"
                    )
                )
            }
        }

        return findings
    }

    suspend fun verifyHashWithVirusTotal(packageName: String): Result<VirusTotalApiClient.HashReport> {
        val hash = appAuditor.computeApkSha256(packageName)
            ?: return Result.failure(Exception("Cannot compute SHA-256 for package $packageName"))
        return virusTotalApiClient.checkFileHash(hash)
    }
}
