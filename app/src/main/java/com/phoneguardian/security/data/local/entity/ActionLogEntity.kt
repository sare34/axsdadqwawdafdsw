package com.phoneguardian.security.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.phoneguardian.security.domain.model.ActionLog

@Entity(tableName = "action_logs")
data class ActionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val details: String,
    val isAutonomous: Boolean,
    val isSuccess: Boolean,
    val target: String?,
    val timestamp: Long
) {
    fun toDomain(): ActionLog = ActionLog(
        id = id,
        title = title,
        details = details,
        isAutonomous = isAutonomous,
        isSuccess = isSuccess,
        target = target,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(log: ActionLog): ActionLogEntity = ActionLogEntity(
            id = log.id,
            title = log.title,
            details = log.details,
            isAutonomous = log.isAutonomous,
            isSuccess = log.isSuccess,
            target = log.target,
            timestamp = log.timestamp
        )
    }
}
