package com.phoneguardian.security.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phoneguardian.security.domain.model.Severity
import com.phoneguardian.security.ui.theme.GuardAmber
import com.phoneguardian.security.ui.theme.GuardCrimson
import com.phoneguardian.security.ui.theme.GuardCyan
import com.phoneguardian.security.ui.theme.GuardEmerald

@Composable
fun SeverityBadge(severity: Severity) {
    val (label, bg, fg) = when (severity) {
        Severity.CRITICAL -> Triple("حرج جداً", GuardCrimson.copy(alpha = 0.2f), GuardCrimson)
        Severity.HIGH -> Triple("عالي الخطورة", Color(0xFFFF5722).copy(alpha = 0.2f), Color(0xFFFF5722))
        Severity.MEDIUM -> Triple("متوسط", GuardAmber.copy(alpha = 0.2f), GuardAmber)
        Severity.LOW -> Triple("منخفض", GuardCyan.copy(alpha = 0.2f), GuardCyan)
        Severity.INFO -> Triple("معلوماتي", GuardEmerald.copy(alpha = 0.2f), GuardEmerald)
    }

    Text(
        text = label,
        color = fg,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}
