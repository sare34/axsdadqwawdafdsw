package com.phoneguardian.security.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phoneguardian.security.data.local.entity.ActionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionLogDao {
    @Query("SELECT * FROM action_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActionLogEntity): Long

    @Query("SELECT COUNT(*) FROM action_logs WHERE isAutonomous = 1")
    fun getAutonomousActionCount(): Flow<Int>

    @Query("DELETE FROM action_logs")
    suspend fun clearLogs()
}
