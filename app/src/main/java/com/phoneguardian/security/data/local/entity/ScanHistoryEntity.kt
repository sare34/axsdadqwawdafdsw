package com.phoneguardian.security.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.phoneguardian.security.domain.model.ScanHistory

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val score: Int,
    val findingsCount: Int,
    val criticalCount: Int,
    val durationMs: Long
) {
    fun toDomain(): ScanHistory = ScanHistory(
        id = id,
        timestamp = timestamp,
        score = score,
        findingsCount = findingsCount,
        criticalCount = criticalCount,
        durationMs = durationMs
    )

    companion object {
        fun fromDomain(history: ScanHistory): ScanHistoryEntity = ScanHistoryEntity(
            id = history.id,
            timestamp = history.timestamp,
            score = history.score,
            findingsCount = history.findingsCount,
            criticalCount = history.criticalCount,
            durationMs = history.durationMs
        )
    }
}
