package com.phoneguardian.security.ui.screens.history

import android.app.Application
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phoneguardian.security.PhoneGuardianApp
import com.phoneguardian.security.domain.model.ScanHistory
import com.phoneguardian.security.ui.theme.CardDark
import com.phoneguardian.security.ui.theme.GuardAmber
import com.phoneguardian.security.ui.theme.GuardCrimson
import com.phoneguardian.security.ui.theme.GuardEmerald
import com.phoneguardian.security.ui.theme.TextMuted
import com.phoneguardian.security.ui.theme.TextPrimary
import com.phoneguardian.security.ui.theme.TextSecondary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PhoneGuardianApp

    val historyList: StateFlow<List<ScanHistory>> = app.database.scanHistoryDao()
        .getAllHistory()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun ScanHistoryScreen(
    viewModel: ScanHistoryViewModel = viewModel()
) {
    val historyList by viewModel.historyList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "سجل الفحوصات الأمنية",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )
        Text(
            text = "تتبع تطور مستوى الأمان والفحوصات التلقائية واليدوية السابقة",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (historyList.isEmpty()) {
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
                        text = "لا توجد سجلات فحص سابقة",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = "قم بإجراء فحص أمني شامل لتوثيق حالة الجهاز هنا.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList, key = { it.id }) { scan ->
                    ScanHistoryCard(scan = scan)
                }
            }
        }
    }
}

@Composable
fun ScanHistoryCard(scan: ScanHistory) {
    val formattedDate = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault()).format(Date(scan.timestamp))
    val scoreColor = when {
        scan.score >= 80 -> GuardEmerald
        scan.score >= 50 -> GuardAmber
        else -> GuardCrimson
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "التهديدات: ${scan.findingsCount} (منها ${scan.criticalCount} حرج) • استغرق ${scan.durationMs} م.ث",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 12.sp)
                )
            }

            Text(
                text = "${scan.score}%",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = scoreColor)
            )
        }
    }
}
