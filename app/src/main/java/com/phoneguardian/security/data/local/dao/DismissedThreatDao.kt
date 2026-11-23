package com.phoneguardian.security.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phoneguardian.security.data.local.entity.DismissedThreatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DismissedThreatDao {
    @Query("SELECT * FROM dismissed_threats")
    suspend fun getAllDismissed(): List<DismissedThreatEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM dismissed_threats WHERE threatKey = :key)")
    suspend fun isThreatDismissed(key: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dismissThreat(dismissedThreat: DismissedThreatEntity)

    @Query("DELETE FROM dismissed_threats WHERE threatKey = :key")
    suspend fun restoreThreat(key: String)

    @Query("DELETE FROM dismissed_threats")
    suspend fun clearAll()
}
