package com.phoneguardian.security.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phoneguardian.security.data.local.entity.ThreatRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreatRulesDao {
    @Query("SELECT * FROM threat_rules")
    fun getAllRules(): Flow<List<ThreatRuleEntity>>

    @Query("SELECT * FROM threat_rules WHERE ruleType = :type")
    suspend fun getRulesByType(type: String): List<ThreatRuleEntity>

    @Query("SELECT * FROM threat_rules WHERE pattern = :pattern LIMIT 1")
    suspend fun findMatchingRule(pattern: String): ThreatRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<ThreatRuleEntity>)

    @Query("SELECT COUNT(*) FROM threat_rules")
    suspend fun getRuleCount(): Int
}
