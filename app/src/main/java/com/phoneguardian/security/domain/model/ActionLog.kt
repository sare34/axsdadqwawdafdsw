package com.phoneguardian.security.domain.model

data class ActionLog(
    val id: Long = 0,
    val title: String,
    val details: String,
    val isAutonomous: Boolean,
    val isSuccess: Boolean,
    val target: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
