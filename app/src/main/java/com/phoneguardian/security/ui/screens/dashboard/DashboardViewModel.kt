package com.phoneguardian.security.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phoneguardian.security.PhoneGuardianApp
import com.phoneguardian.security.domain.model.DeviceSecurityPosture
import com.phoneguardian.security.domain.model.SecurityFinding
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PhoneGuardianApp
    private val engine = app.decisionEngine

    val posture: StateFlow<DeviceSecurityPosture> = engine.posture
    val isScanning: StateFlow<Boolean> = engine.isScanning

    val topFindings: StateFlow<List<SecurityFinding>> = app.database.findingsDao()
        .getAllActiveFindings()
        .map { list -> list.map { it.toDomain() }.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun triggerDeepScan() {
        viewModelScope.launch {
            engine.runDeepScan()
        }
    }

    fun dismissFinding(finding: SecurityFinding) {
        viewModelScope.launch {
            engine.dismissThreat(finding)
        }
    }

    fun autoFixFinding(finding: SecurityFinding) {
        viewModelScope.launch {
            app.remediationManager.attemptAutonomousRemediation(finding)
            engine.runDeepScan()
        }
    }
}
