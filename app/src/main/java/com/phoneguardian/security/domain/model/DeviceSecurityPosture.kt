package com.phoneguardian.security.domain.model

data class DeviceSecurityPosture(
    val score: Int, // 0 - 100
    val totalFindings: Int,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val lastScanTime: Long,
    val isAgentRunning: Boolean,
    val isVpnActive: Boolean,
    val isShizukuActive: Boolean
)
