package com.phoneguardian.security.ui.screens.actionlog

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phoneguardian.security.PhoneGuardianApp
import com.phoneguardian.security.domain.model.ActionLog
import com.phoneguardian.security.ui.components.ActionLogItem
import com.phoneguardian.security.ui.theme.CardDark
import com.phoneguardian.security.ui.theme.TextMuted
import com.phoneguardian.security.ui.theme.TextPrimary
import com.phoneguardian.security.ui.theme.TextSecondary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ActionLogViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PhoneGuardianApp

    val logs: StateFlow<List<ActionLog>> = app.database.actionLogDao()
        .getAllLogs()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun ActionLogScreen(
    viewModel: ActionLogViewModel = viewModel()
) {
    val logs by viewModel.logs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "سجل إجراءات العميل الأمني الذكي",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )
        Text(
            text = "توثيق شامل لكافة المعالجات الذاتية وحظر الاتصالات والتنبيهات",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (logs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "لا توجد إجراءات مسجلة بعد",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = "سيقوم العميل الذكي بتسجيل كل خطوة ومعالجة تلقائية ينفذها لحماية جهازك هنا.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    ActionLogItem(log = log)
                }
            }
        }
    }
}
