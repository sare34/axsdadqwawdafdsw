package com.phoneguardian.security.ui.screens.findings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phoneguardian.security.domain.model.SecurityFinding
import com.phoneguardian.security.domain.model.Severity
import com.phoneguardian.security.ui.components.FindingCard
import com.phoneguardian.security.ui.screens.dashboard.DashboardViewModel
import com.phoneguardian.security.ui.theme.GuardCrimson
import com.phoneguardian.security.ui.theme.GuardEmerald
import com.phoneguardian.security.ui.theme.TextMuted
import com.phoneguardian.security.ui.theme.TextPrimary

@Composable
fun FindingsScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToChatWithFinding: (SecurityFinding) -> Unit,
    onOpenSettingsIntent: (SecurityFinding) -> Unit
) {
    val findings by viewModel.topFindings.collectAsState()
    var selectedSeverity by remember { mutableStateOf<Severity?>(null) }

    val filteredFindings = remember(findings, selectedSeverity) {
        if (selectedSeverity == null) findings else findings.filter { it.severity == selectedSeverity }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "المخاطر والتهديدات الأمنية",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )
        Text(
            text = "فحص معمق لكافة التطبيقات وإعدادات النظام والشبكات",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Severity Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedSeverity == null,
                onClick = { selectedSeverity = null },
                label = { Text("الكل (${findings.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GuardEmerald.copy(alpha = 0.2f),
                    selectedLabelColor = GuardEmerald
                )
            )

            Severity.values().forEach { severity ->
                val count = findings.count { it.severity == severity }
                FilterChip(
                    selected = selectedSeverity == severity,
                    onClick = { selectedSeverity = severity },
                    label = {
                        Text(
                            when (severity) {
                                Severity.CRITICAL -> "حرج ($count)"
                                Severity.HIGH -> "عالي ($count)"
                                Severity.MEDIUM -> "متوسط ($count)"
                                Severity.LOW -> "منخفض ($count)"
                                Severity.INFO -> "معلوماتي ($count)"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GuardCrimson.copy(alpha = 0.2f),
                        selectedLabelColor = GuardCrimson
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredFindings, key = { it.id }) { finding ->
                FindingCard(
                    finding = finding,
                    onAutoFixClick = { viewModel.autoFixFinding(it) },
                    onOpenSettingsClick = { onOpenSettingsIntent(it) },
                    onDismissClick = { viewModel.dismissFinding(it) },
                    onAskAiClick = { onNavigateToChatWithFinding(it) }
                )
            }
        }
    }
}
