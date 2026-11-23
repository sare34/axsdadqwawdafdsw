package com.phoneguardian.security.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.phoneguardian.security.domain.model.FindingCategory
import com.phoneguardian.security.domain.model.SecurityFinding
import com.phoneguardian.security.domain.model.Severity

@Entity(tableName = "security_findings")
data class SecurityFindingEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val severity: String,
    val packageName: String?,
    val appName: String?,
    val detectedAt: Long,
    val deepLinkAction: String?,
    val deepLinkData: String?,
    val autoRemediated: Boolean,
    val canAutoFix: Boolean,
    val aiRecommendation: String?,
    val isDismissed: Boolean
) {
    fun toDomain(): SecurityFinding = SecurityFinding(
        id = id,
        title = title,
        description = description,
        category = try { FindingCategory.valueOf(category) } catch (e: Exception) { FindingCategory.SYSTEM },
        severity = try { Severity.valueOf(severity) } catch (e: Exception) { Severity.MEDIUM },
        packageName = packageName,
        appName = appName,
        detectedAt = detectedAt,
        deepLinkAction = deepLinkAction,
        deepLinkData = deepLinkData,
        autoRemediated = autoRemediated,
        canAutoFix = canAutoFix,
        aiRecommendation = aiRecommendation,
        isDismissed = isDismissed
    )

    companion object {
        fun fromDomain(finding: SecurityFinding): SecurityFindingEntity = SecurityFindingEntity(
            id = finding.id,
            title = finding.title,
            description = finding.description,
            category = finding.category.name,
            severity = finding.severity.name,
            packageName = finding.packageName,
            appName = finding.appName,
            detectedAt = finding.detectedAt,
            deepLinkAction = finding.deepLinkAction,
            deepLinkData = finding.deepLinkData,
            autoRemediated = finding.autoRemediated,
            canAutoFix = finding.canAutoFix,
            aiRecommendation = finding.aiRecommendation,
            isDismissed = finding.isDismissed
        )
    }
}
