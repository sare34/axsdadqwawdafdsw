package com.phoneguardian.security.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.phoneguardian.security.data.local.dao.ActionLogDao
import com.phoneguardian.security.data.local.dao.DismissedThreatDao
import com.phoneguardian.security.data.local.dao.FindingsDao
import com.phoneguardian.security.data.local.dao.ScanHistoryDao
import com.phoneguardian.security.data.local.dao.ThreatRulesDao
import com.phoneguardian.security.data.local.entity.ActionLogEntity
import com.phoneguardian.security.data.local.entity.DismissedThreatEntity
import com.phoneguardian.security.data.local.entity.SecurityFindingEntity
import com.phoneguardian.security.data.local.entity.ScanHistoryEntity
import com.phoneguardian.security.data.local.entity.ThreatRuleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SecurityFindingEntity::class,
        ActionLogEntity::class,
        ScanHistoryEntity::class,
        DismissedThreatEntity::class,
        ThreatRuleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SecurityDatabase : RoomDatabase() {

    abstract fun findingsDao(): FindingsDao
    abstract fun actionLogDao(): ActionLogDao
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun dismissedThreatDao(): DismissedThreatDao
    abstract fun threatRulesDao(): ThreatRulesDao

    companion object {
        @Volatile
        private var INSTANCE: SecurityDatabase? = null

        fun getInstance(context: Context): SecurityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SecurityDatabase::class.java,
                    "phone_guardian_security.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default malware & stalkerware threat rules
                        CoroutineScope(Dispatchers.IO).launch {
                            val seedRules = listOf(
                                ThreatRuleEntity(
                                    ruleId = "rule_stalker_cerberus",
                                    ruleType = "PACKAGE_NAME",
                                    pattern = "com.fsp.android.c",
                                    threatName = "Cerberus Phone Security / Spyware",
                                    description = "Known commercial stalkerware capable of silent audio/location surveillance.",
                                    severity = "CRITICAL"
                                ),
                                ThreatRuleEntity(
                                    ruleId = "rule_trojan_anubis",
                                    ruleType = "PACKAGE_NAME",
                                    pattern = "org.anubis.banking",
                                    threatName = "Anubis Banking Trojan",
                                    description = "High-risk banking overlay trojan with SMS interception capabilities.",
                                    severity = "CRITICAL"
                                ),
                                ThreatRuleEntity(
                                    ruleId = "rule_spyware_pegasus_sig",
                                    ruleType = "PACKAGE_NAME",
                                    pattern = "com.network.monitoring.daemon",
                                    threatName = "Suspicious Background Monitor",
                                    description = "Anomalous disguised daemon harvesting telemetry without a launcher icon.",
                                    severity = "HIGH"
                                ),
                                ThreatRuleEntity(
                                    ruleId = "rule_sms_stealer_generic",
                                    ruleType = "PACKAGE_NAME",
                                    pattern = "com.sms.forwarder.stealth",
                                    threatName = "Stealth SMS Forwarder",
                                    description = "Intercepts 2FA OTP tokens and routes them to external endpoints.",
                                    severity = "CRITICAL"
                                )
                            )
                            getInstance(context).threatRulesDao().insertRules(seedRules)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
