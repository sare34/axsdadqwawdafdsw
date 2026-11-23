package com.phoneguardian.security.domain.model

data class SecurityFinding(
    val id: String,
    val title: String,
    val description: String,
    val category: FindingCategory,
    val severity: Severity,
    val packageName: String? = null,
    val appName: String? = null,
    val detectedAt: Long = System.currentTimeMillis(),
    val deepLinkAction: String? = null,
    val deepLinkData: String? = null,
    val autoRemediated: Boolean = false,
    val canAutoFix: Boolean = false,
    val aiRecommendation: String? = null,
    val isDismissed: Boolean = false
)
