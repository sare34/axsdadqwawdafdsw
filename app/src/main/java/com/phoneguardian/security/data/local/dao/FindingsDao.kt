package com.phoneguardian.security.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.phoneguardian.security.data.local.entity.SecurityFindingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FindingsDao {
    @Query("SELECT * FROM security_findings WHERE isDismissed = 0 ORDER BY detectedAt DESC")
    fun getAllActiveFindings(): Flow<List<SecurityFindingEntity>>

    @Query("SELECT * FROM security_findings ORDER BY detectedAt DESC")
    fun getAllFindings(): Flow<List<SecurityFindingEntity>>

    @Query("SELECT * FROM security_findings WHERE id = :id LIMIT 1")
    suspend fun getFindingById(id: String): SecurityFindingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFindings(findings: List<SecurityFindingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinding(finding: SecurityFindingEntity)

    @Update
    suspend fun updateFinding(finding: SecurityFindingEntity)

    @Query("UPDATE security_findings SET isDismissed = 1 WHERE id = :id")
    suspend fun dismissFinding(id: String)

    @Query("UPDATE security_findings SET autoRemediated = 1 WHERE id = :id")
    suspend fun markRemediated(id: String)

    @Query("DELETE FROM security_findings WHERE id = :id")
    suspend fun deleteFinding(id: String)

    @Query("DELETE FROM security_findings")
    suspend fun clearAll()
}
