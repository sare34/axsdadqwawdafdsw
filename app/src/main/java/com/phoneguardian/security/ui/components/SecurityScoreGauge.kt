package com.phoneguardian.security.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phoneguardian.security.ui.theme.GuardAmber
import com.phoneguardian.security.ui.theme.GuardCrimson
import com.phoneguardian.security.ui.theme.GuardCyan
import com.phoneguardian.security.ui.theme.GuardEmerald
import com.phoneguardian.security.ui.theme.TextPrimary
import com.phoneguardian.security.ui.theme.TextSecondary

@Composable
fun SecurityScoreGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1200),
        label = "ScoreGaugeAnimation"
    )

    val gaugeColor = when {
        score >= 80 -> GuardEmerald
        score >= 50 -> GuardAmber
        else -> GuardCrimson
    }

    val gradient = Brush.sweepGradient(
        listOf(
            gaugeColor.copy(alpha = 0.6f),
            gaugeColor,
            GuardCyan,
            gaugeColor
        )
    )

    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            // Background track
            drawCircle(
                color = Color(0xFF21262D),
                radius = size.minDimension / 2 - 12.dp.toPx(),
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )

            // Progress arc (sweep 260 degrees)
            val sweep = (animatedScore / 100f) * 260f
            drawArc(
                brush = gradient,
                startAngle = 140f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${animatedScore.toInt()}%",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            )
            Text(
                text = when {
                    score >= 80 -> "محمي بالكامل"
                    score >= 50 -> "مخاطر معتدلة"
                    else -> "مخاطر حرجة"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = gaugeColor,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}
