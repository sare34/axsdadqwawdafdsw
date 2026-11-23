package com.phoneguardian.security.domain.model

data class ScanHistory(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val score: Int,
    val findingsCount: Int,
    val criticalCount: Int,
    val durationMs: Long
)
