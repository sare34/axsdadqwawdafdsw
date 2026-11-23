package com.phoneguardian.security.domain.engine

import android.content.Context
import com.phoneguardian.security.data.local.SecurityPreferences
import com.phoneguardian.security.data.local.dao.DismissedThreatDao
import com.phoneguardian.security.data.local.dao.FindingsDao
import com.phoneguardian.security.data.local.dao.ScanHistoryDao
import com.phoneguardian.security.data.local.entity.DismissedThreatEntity
import com.phoneguardian.security.data.local.entity.ScanHistoryEntity
import com.phoneguardian.security.data.local.entity.SecurityFindingEntity
import com.phoneguardian.security.data.remote.GeminiApiClient
import com.phoneguardian.security.domain.model.DeviceSecurityPosture
import com.phoneguardian.security.domain.model.SecurityFinding
import com.phoneguardian.security.domain.model.Severity
import com.phoneguardian.security.service.LocalVpnFirewallService
import com.phoneguardian.security.service.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AutonomousDecisionEngine(
    private val context: Context,
    private val appAuditor: AppAuditor,
    private val systemAuditor: SystemPostureAuditor,
    private val networkAuditor: NetworkAuditor,
    private val behaviorAuditor: BehaviorAuditor,
    private val threatIntelAuditor: ThreatIntelligenceAuditor,
    private val remediationManager: RemediationManager,
    private val geminiApiClient: GeminiApiClient,
    private val preferences: SecurityPreferences,
    private val findingsDao: FindingsDao,
    private val scanHistoryDao: ScanHistoryDao,
    private val dismissedThreatDao: DismissedThreatDao,
    private val shizukuManager: ShizukuManager
) {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _posture = MutableStateFlow(
        DeviceSecurityPosture(
            score = 100,
            totalFindings = 0,
            criticalCount = 0,
            highCount = 0,
            mediumCount = 0,
            lowCount = 0,
            lastScanTime = System.currentTimeMillis(),
            isAgentRunning = true,
            isVpnActive = false,
            isShizukuActive = false
        )
    )
    val posture: StateFlow<DeviceSecurityPosture> = _posture.asStateFlow()

    suspend fun runDeepScan(): List<SecurityFinding> = withContext(Dispatchers.IO) {
        _isScanning.value = true
        val startTime = System.currentTimeMillis()

        try {
            val rawFindings = mutableListOf<SecurityFinding>()

            // 1. Audit Installed Apps
            rawFindings.addAll(appAuditor.auditInstalledApps())

            // 2. Audit System Posture
            rawFindings.addAll(systemAuditor.auditSystemPosture())

            // 3. Audit Network
            rawFindings.addAll(networkAuditor.auditNetwork())

            // 4. Audit Behaviors & AppOps
            rawFindings.addAll(behaviorAuditor.auditBehavior())

            // 5. Audit Known Threat Intelligence
            rawFindings.addAll(threatIntelAuditor.auditThreatIntel())

            // Filter out user-dismissed threats
            val dismissedKeys = dismissedThreatDao.getAllDismissed().map { it.threatKey }.toSet()
            val activeFindings = rawFindings.filterNot { dismissedKeys.contains(it.id) }

            // 6. Consult Gemini AI if key is present (Strictly anonymized data!)
            val enrichedFindings = if (preferences.geminiApiKey.isNotBlank() && activeFindings.isNotEmpty()) {
                enrichWithGemini(activeFindings)
            } else {
                activeFindings
            }

            // 7. Execute Autonomous Remediation where allowed
            val finalFindings = enrichedFindings.map { finding ->
                var updatedFinding = finding
                if (finding.canAutoFix || preferences.isShizukuModeEnabled) {
                    val fixed = remediationManager.attemptAutonomousRemediation(finding)
                    if (fixed) {
                        updatedFinding = finding.copy(autoRemediated = true)
                    }
                }
                updatedFinding
            }

            // Save to Room DB
            findingsDao.clearAll()
            findingsDao.insertFindings(finalFindings.map { SecurityFindingEntity.fromDomain(it) })

            // Calculate Security Score
            val score = calculateSecurityScore(finalFindings)
            val duration = System.currentTimeMillis() - startTime

            val critCount = finalFindings.count { it.severity == Severity.CRITICAL && !it.autoRemediated }
            val highCount = finalFindings.count { it.severity == Severity.HIGH && !it.autoRemediated }
            val medCount = finalFindings.count { it.severity == Severity.MEDIUM && !it.autoRemediated }
            val lowCount = finalFindings.count { it.severity == Severity.LOW && !it.autoRemediated }

            // Record Scan History
            scanHistoryDao.insertScan(
                ScanHistoryEntity(
                    timestamp = System.currentTimeMillis(),
                    score = score,
                    findingsCount = finalFindings.size,
                    criticalCount = critCount,
                    durationMs = duration
                )
            )

            _posture.value = DeviceSecurityPosture(
                score = score,
                totalFindings = finalFindings.size,
                criticalCount = critCount,
                highCount = highCount,
                mediumCount = medCount,
                lowCount = lowCount,
                lastScanTime = System.currentTimeMillis(),
                isAgentRunning = true,
                isVpnActive = LocalVpnFirewallService.isVpnRunning,
                isShizukuActive = shizukuManager.hasShizukuPermission()
            )

            finalFindings
        } finally {
            _isScanning.value = false
        }
    }

    private suspend fun enrichWithGemini(findings: List<SecurityFinding>): List<SecurityFinding> {
        // Anonymized summary: package names and titles only, never personal info!
        val anonymizedBuilder = StringBuilder()
        for ((idx, f) in findings.withIndex()) {
            anonymizedBuilder.append("${idx + 1}. [${f.severity}] ${f.category}: ${f.title} (Target: ${f.packageName ?: "System"})\n")
        }

        val aiResult = geminiApiClient.analyzeThreat(anonymizedBuilder.toString())
        val aiAdvice = aiResult.getOrNull()

        return if (aiAdvice != null) {
            findings.map { it.copy(aiRecommendation = aiAdvice) }
        } else {
            findings
        }
    }

    private fun calculateSecurityScore(findings: List<SecurityFinding>): Int {
        var deductions = 0
        for (f in findings) {
            if (!f.autoRemediated && !f.isDismissed) {
                deductions += f.severity.weight
            }
        }
        val score = 100 - deductions
        return score.coerceIn(5, 100)
    }

    suspend fun dismissThreat(finding: SecurityFinding, reason: String? = null) {
        dismissedThreatDao.dismissThreat(DismissedThreatEntity(threatKey = finding.id, reason = reason))
        findingsDao.dismissFinding(finding.id)
        remediationManager.logAction(
            title = "تم تجاهل التنبيه بناء على طلب المستخدم",
            details = "لن يتم تكرار هذا التحذير مستقبلاً: ${finding.title}",
            isAutonomous = false,
            isSuccess = true,
            target = finding.packageName ?: finding.id
        )
    }
}
