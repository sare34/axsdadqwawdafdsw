package com.phoneguardian.security

import android.app.Application
import com.phoneguardian.security.data.local.SecurityDatabase
import com.phoneguardian.security.data.local.SecurityPreferences
import com.phoneguardian.security.data.remote.GeminiApiClient
import com.phoneguardian.security.data.remote.VirusTotalApiClient
import com.phoneguardian.security.domain.engine.AppAuditor
import com.phoneguardian.security.domain.engine.AutonomousDecisionEngine
import com.phoneguardian.security.domain.engine.BehaviorAuditor
import com.phoneguardian.security.domain.engine.NetworkAuditor
import com.phoneguardian.security.domain.engine.RemediationManager
import com.phoneguardian.security.domain.engine.SystemPostureAuditor
import com.phoneguardian.security.domain.engine.ThreatIntelligenceAuditor
import com.phoneguardian.security.service.PhoneGuardianService
import com.phoneguardian.security.service.SecurityScanWorker
import com.phoneguardian.security.service.shizuku.ShizukuManager

class PhoneGuardianApp : Application() {

    lateinit var database: SecurityDatabase
        private set

    lateinit var preferences: SecurityPreferences
        private set

    lateinit var geminiApiClient: GeminiApiClient
        private set

    lateinit var virusTotalApiClient: VirusTotalApiClient
        private set

    lateinit var shizukuManager: ShizukuManager
        private set

    lateinit var remediationManager: RemediationManager
        private set

    lateinit var decisionEngine: AutonomousDecisionEngine
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = SecurityDatabase.getInstance(this)
        preferences = SecurityPreferences(this)
        shizukuManager = ShizukuManager(this)

        geminiApiClient = GeminiApiClient { preferences.geminiApiKey }
        virusTotalApiClient = VirusTotalApiClient { preferences.virusTotalApiKey }

        val appAuditor = AppAuditor(this)
        val systemAuditor = SystemPostureAuditor(this)
        val networkAuditor = NetworkAuditor(this)
        val behaviorAuditor = BehaviorAuditor(this)
        val threatIntelAuditor = ThreatIntelligenceAuditor(
            context = this,
            threatRulesDao = database.threatRulesDao(),
            appAuditor = appAuditor,
            virusTotalApiClient = virusTotalApiClient
        )

        remediationManager = RemediationManager(
            context = this,
            preferences = preferences,
            actionLogDao = database.actionLogDao(),
            shizukuManager = shizukuManager
        )

        decisionEngine = AutonomousDecisionEngine(
            context = this,
            appAuditor = appAuditor,
            systemAuditor = systemAuditor,
            networkAuditor = networkAuditor,
            behaviorAuditor = behaviorAuditor,
            threatIntelAuditor = threatIntelAuditor,
            remediationManager = remediationManager,
            geminiApiClient = geminiApiClient,
            preferences = preferences,
            findingsDao = database.findingsDao(),
            scanHistoryDao = database.scanHistoryDao(),
            dismissedThreatDao = database.dismissedThreatDao(),
            shizukuManager = shizukuManager
        )

        // Start autonomous foreground service
        PhoneGuardianService.start(this)

        // Schedule periodic WorkManager scans
        SecurityScanWorker.schedule(this, preferences.scanFrequencyHours.toLong())
    }

    companion object {
        lateinit var instance: PhoneGuardianApp
            private set
    }
}
