package com.phoneguardian.security.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phoneguardian.security.ui.theme.CardDark
import com.phoneguardian.security.ui.theme.GuardCyan
import com.phoneguardian.security.ui.theme.GuardEmerald
import com.phoneguardian.security.ui.theme.GuardPurple
import com.phoneguardian.security.ui.theme.SurfaceDark
import com.phoneguardian.security.ui.theme.TextMuted
import com.phoneguardian.security.ui.theme.TextPrimary
import com.phoneguardian.security.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val geminiKey by viewModel.geminiApiKey.collectAsState()
    val vtKey by viewModel.virusTotalApiKey.collectAsState()
    val scanFreq by viewModel.scanFrequencyHours.collectAsState()
    val isVpnEnabled by viewModel.isVpnEnabled.collectAsState()
    val isShizukuEnabled by viewModel.isShizukuEnabled.collectAsState()
    val isAutoFixEnabled by viewModel.isAutonomousFixEnabled.collectAsState()
    val shizukuStatus by viewModel.shizukuStatusText.collectAsState()

    var tempGeminiKey by remember(geminiKey) { mutableStateOf(geminiKey) }
    var tempVtKey by remember(vtKey) { mutableStateOf(vtKey) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "الإعدادات والتكاملات",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
            Text(
                text = "إدارة مفاتيح الذكاء الاصطناعي، ومراقبة الشبكة، والوضع المتقدم",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
            )
        }

        // Gemini AI API Key
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = GuardPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مفتاح Gemini API (تحليل أمني ذكي)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Text(
                        text = "يُستخدم لاستشارة نماذج Gemini في تقييم المخاطر واقتراح خطوات المعالجة. (يتم إرسال أسماء الحزم والصلاحيات فقط، بدون أي بيانات شخصية).",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 12.sp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempGeminiKey,
                        onValueChange = { tempGeminiKey = it },
                        placeholder = { Text("AIzaSy...", color = TextMuted) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.saveGeminiApiKey(tempGeminiKey) },
                        colors = ButtonDefaults.buttonColors(containerColor = GuardPurple)
                    ) {
                        Text("حفظ مفتاح Gemini", color = Color.White)
                    }
                }
            }
        }

        // VirusTotal API Key
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = GuardCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مفتاح VirusTotal API (بصمات التهديدات)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Text(
                        text = "للتحقق التلقائي من بصمات SHA-256 للتطبيقات المشبوهة عالمياً عبر VirusTotal.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 12.sp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempVtKey,
                        onValueChange = { tempVtKey = it },
                        placeholder = { Text("أدخل مفتاح VirusTotal...", color = TextMuted) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.saveVirusTotalApiKey(tempVtKey) },
                        colors = ButtonDefaults.buttonColors(containerColor = GuardCyan)
                    ) {
                        Text("حفظ مفتاح VirusTotal", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Scan Frequency
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("تكرار الفحص الدوري التلقائي (WorkManager)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("يقوم العميل الأمني بالفحص في الخلفية دون أي تدخل يدوي.", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 12.sp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1 to "كل ساعة", 6 to "كل 6 ساعات", 24 to "يومياً").forEach { (hours, label) ->
                            FilterChip(
                                selected = scanFreq == hours,
                                onClick = { viewModel.setScanFrequency(hours) },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GuardEmerald.copy(alpha = 0.2f),
                                    selectedLabelColor = GuardEmerald
                                )
                            )
                        }
                    }
                }
            }
        }

        // Local VPN Traffic Monitor Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VpnLock, contentDescription = null, tint = GuardCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("مراقب وحاجز الشبكة المحلي (VPN)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("يعمل محلياً بالكامل على جهازك لفحص الاتصالات وحظر الخوادم الخبيثة الصادرة.", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 12.sp))
                    }
                    Switch(
                        checked = isVpnEnabled,
                        onCheckedChange = { viewModel.toggleVpn(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = GuardCyan)
                    )
                }
            }
        }

        // Shizuku Advanced Automation Mode
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = GuardPurple)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("الوضع المتقدم (Shizuku Automation)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("يتيح للعميل الأمني سحب الصلاحيات الخطرة وتعطيل الإعدادات المشبوهة ذاتياً دون الحاجة للنقر اليدوي.", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 12.sp))
                        }
                        Switch(
                            checked = isShizukuEnabled,
                            onCheckedChange = { viewModel.toggleShizuku(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = GuardPurple)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "الحالة: $shizukuStatus",
                        style = MaterialTheme.typography.bodyMedium.copy(color = GuardPurple, fontSize = 12.sp)
                    )
                }
            }
        }

        // Safety Rules Disclaimer Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161E29)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ميثاق الأمان والخصوصية (قواعد غير قابلة للخرق):",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GuardEmerald)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. لن يتم إطلاقاً حذف أو تعطيل أي تطبيق أو ملف دون موافقتك الصريحة في كل مرة.\n2. يتم إرسال بيانات مجهولة الهوية فقط للذكاء الاصطناعي (أسماء الحزم وحالات الإعدادات)، ولا يتم المساس مطلقاً برسائلك، صورك، جهات اتصالك، أو ملفاتك الشخصية.\n3. كافة البيانات وسجلات الفحص مشفرة ومحفوظة محلياً على جهازك.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                    )
                }
            }
        }
    }
}
