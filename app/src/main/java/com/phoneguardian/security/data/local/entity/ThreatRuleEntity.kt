package com.phoneguardian.security.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "threat_rules")
data class ThreatRuleEntity(
    @PrimaryKey
    val ruleId: String,
    val ruleType: String, // "PACKAGE_NAME", "CERT_HASH", "FILE_HASH", "STALKERWARE_SIGNATURE"
    val pattern: String,
    val threatName: String,
    val description: String,
    val severity: String,
    val isBlocked: Boolean = true
)
