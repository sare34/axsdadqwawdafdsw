package com.phoneguardian.security.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phoneguardian.security.domain.model.FindingCategory
import com.phoneguardian.security.domain.model.SecurityFinding
import com.phoneguardian.security.ui.theme.BorderDark
import com.phoneguardian.security.ui.theme.CardDark
import com.phoneguardian.security.ui.theme.GuardEmerald
import com.phoneguardian.security.ui.theme.GuardPurple
import com.phoneguardian.security.ui.theme.TextMuted
import com.phoneguardian.security.ui.theme.TextPrimary
import com.phoneguardian.security.ui.theme.TextSecondary

@Composable
fun FindingCard(
    finding: SecurityFinding,
    onAutoFixClick: (SecurityFinding) -> Unit,
    onOpenSettingsClick: (SecurityFinding) -> Unit,
    onDismissClick: (SecurityFinding) -> Unit,
    onAskAiClick: (SecurityFinding) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SeverityBadge(severity = finding.severity)

                val categoryName = when (finding.category) {
                    FindingCategory.APPS -> "فحص التطبيقات"
                    FindingCategory.SYSTEM -> "أمان النظام"
                    FindingCategory.NETWORK -> "أمان الشبكة"
                    FindingCategory.BEHAVIOR -> "سلوك التطبيقات"
                    FindingCategory.THREAT_INTEL -> "استخبارات التهديدات"
                }

                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, color = TextMuted)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = finding.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )

            if (finding.appName != null) {
                Text(
                    text = "التطبيق المستهدف: ${finding.appName} (${finding.packageName})",
                    style = MaterialTheme.typography.bodyMedium.copy(color = GuardPurple, fontSize = 12.sp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = finding.description,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, lineHeight = 20.sp)
            )

            if (finding.autoRemediated) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "تم الإصلاح",
                        tint = GuardEmerald
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تمت المعالجة ذاتياً بواسطة العميل الأمني الذكي",
                        style = MaterialTheme.typography.bodyMedium.copy(color = GuardEmerald, fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            if (!finding.aiRecommendation.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B222D)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "تحليل AI",
                                tint = GuardPurple
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "توصية مستشار Gemini الأمني:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = GuardPurple)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = finding.aiRecommendation,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontSize = 12.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!finding.autoRemediated && finding.canAutoFix) {
                    Button(
                        onClick = { onAutoFixClick(finding) },
                        colors = ButtonDefaults.buttonColors(containerColor = GuardEmerald)
                    ) {
                        Text("إصلاح ذاتي", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                if (finding.deepLinkAction != null && !finding.autoRemediated) {
                    OutlinedButton(
                        onClick = { onOpenSettingsClick(finding) },
                        border = BorderStroke(1.dp, GuardPurple)
                    ) {
                        Icon(imageVector = Icons.Default.Launch, contentDescription = null, tint = GuardPurple)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الإعدادات", color = GuardPurple)
                    }
                }

                TextButton(onClick = { onAskAiClick(finding) }) {
                    Text("استشارة AI", color = GuardEmerald)
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = { onDismissClick(finding) }) {
                    Icon(imageVector = Icons.Default.VisibilityOff, contentDescription = null, tint = TextMuted)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تجاهل", color = TextMuted)
                }
            }
        }
    }
}
