package com.phoneguardian.security.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phoneguardian.security.domain.model.SecurityFinding
import com.phoneguardian.security.ui.components.FindingCard
import com.phoneguardian.security.ui.components.SecurityScoreGauge
import com.phoneguardian.security.ui.theme.CardDark
import com.phoneguardian.security.ui.theme.GuardAmber
import com.phoneguardian.security.ui.theme.GuardCrimson
import com.phoneguardian.security.ui.theme.GuardCyan
import com.phoneguardian.security.ui.theme.GuardEmerald
import com.phoneguardian.security.ui.theme.GuardPurple
import com.phoneguardian.security.ui.theme.SurfaceDark
import com.phoneguardian.security.ui.theme.TextMuted
import com.phoneguardian.security.ui.theme.TextPrimary
import com.phoneguardian.security.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToFindings: () -> Unit,
    onNavigateToChatWithFinding: (SecurityFinding) -> Unit,
    onOpenSettingsIntent: (SecurityFinding) -> Unit
) {
    val posture by viewModel.posture.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val topFindings by viewModel.topFindings.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "حارس الهاتف (Phone Guardian)",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(GuardEmerald.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "بدون روت 100%",
                                color = GuardEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "العميل الأمني الذكي والمستقل على جهازك (يعمل بصلاحيات النظام القياسية)",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(GuardEmerald, CircleShape)
                )
            }
        }

        // Circular Security Score
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SecurityScoreGauge(score = posture.score)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.triggerDeepScan() },
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(containerColor = GuardEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جارٍ الفحص العميق…", color = Color.Black, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("بدء فحص أمني شامل الآن", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Agent Posture Status Indicators
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusIndicatorChip(
                    title = "الخدمة الدائمة",
                    isActive = posture.isAgentRunning,
                    icon = Icons.Default.Bolt,
                    activeColor = GuardEmerald,
                    modifier = Modifier.weight(1f)
                )
                StatusIndicatorChip(
                    title = "حاجز VPN",
                    isActive = posture.isVpnActive,
                    icon = Icons.Default.VpnLock,
                    activeColor = GuardCyan,
                    modifier = Modifier.weight(1f)
                )
                StatusIndicatorChip(
                    title = "وضع Shizuku",
                    isActive = posture.isShizukuActive,
                    icon = Icons.Default.Security,
                    activeColor = GuardPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Findings Counter Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThreatCounterCard("حرجة", posture.criticalCount, GuardCrimson, Modifier.weight(1f))
                ThreatCounterCard("عالية", posture.highCount, Color(0xFFFF5722), Modifier.weight(1f))
                ThreatCounterCard("متوسطة", posture.mediumCount, GuardAmber, Modifier.weight(1f))
                ThreatCounterCard("منخفضة", posture.lowCount, GuardCyan, Modifier.weight(1f))
            }
        }

        // Top Findings Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المخاطر المكتشفة مؤخراً (${topFindings.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        if (topFindings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GuardEmerald,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لم يتم اكتشاف أي مخاطر أمنية نشطة!",
                            style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "جهازك محمي بنجاح ومحصن ضد التهديدات المعروفة.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                    }
                }
            }
        } else {
            items(topFindings, key = { it.id }) { finding ->
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

@Composable
fun StatusIndicatorChip(
    title: String,
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) activeColor else TextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = TextPrimary)
            )
            Text(
                text = if (isActive) "نشط" else "متوقف",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = if (isActive) activeColor else TextMuted,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun ThreatCounterCard(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
            )
        }
    }
}
