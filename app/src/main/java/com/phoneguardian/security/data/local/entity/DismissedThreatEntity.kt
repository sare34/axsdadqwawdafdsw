package com.phoneguardian.security.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dismissed_threats")
data class DismissedThreatEntity(
    @PrimaryKey
    val threatKey: String, // e.g. "package:com.example.app:overlay" or "system:adb_enabled"
    val reason: String? = null,
    val dismissedAt: Long = System.currentTimeMillis()
)
