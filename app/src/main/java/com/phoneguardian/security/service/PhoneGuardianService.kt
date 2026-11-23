package com.phoneguardian.security.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.phoneguardian.security.PhoneGuardianApp
import com.phoneguardian.security.R
import com.phoneguardian.security.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PhoneGuardianService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var monitorJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_TRIGGER_SCAN) {
            triggerAutonomousScan()
        }

        val notification = buildOngoingNotification("العميل الأمني نشط - مستوى الحماية مستقر", 100)
        startForeground(NOTIFICATION_ID, notification)

        startContinuousMonitoring()
        return START_STICKY
    }

    private fun startContinuousMonitoring() {
        if (monitorJob != null) return

        monitorJob = serviceScope.launch {
            val app = application as? PhoneGuardianApp ?: return@launch
            while (isActive) {
                // Execute autonomous scan
                try {
                    val findings = app.decisionEngine.runDeepScan()
                    val posture = app.decisionEngine.posture.value

                    val text = if (posture.criticalCount > 0) {
                        "تحذير: تم اكتشاف ${posture.criticalCount} تهديد حرج!"
                    } else if (posture.highCount > 0) {
                        "تنبيه: يوجد ${posture.highCount} مخاطر بحاجة للمراجعة"
                    } else {
                        "النظام محمي ومستقر (درجة الأمان: ${posture.score}%)"
                    }

                    updateNotification(text, posture.score)
                } catch (e: Exception) {
                    // Ignore and retry next cycle
                }

                // Check interval from preferences (e.g. 1 hour, or shorter poll)
                val hours = (application as? PhoneGuardianApp)?.preferences?.scanFrequencyHours ?: 1
                val delayMs = (hours * 60 * 60 * 1000L).coerceAtLeast(15 * 60 * 1000L)
                delay(delayMs)
            }
        }
    }

    private fun triggerAutonomousScan() {
        serviceScope.launch {
            val app = application as? PhoneGuardianApp ?: return@launch
            app.decisionEngine.runDeepScan()
        }
    }

    private fun buildOngoingNotification(statusText: String, score: Int): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val scanIntent = Intent(this, PhoneGuardianService::class.java).apply {
            action = ACTION_TRIGGER_SCAN
        }
        val scanPendingIntent = PendingIntent.getService(
            this,
            1,
            scanIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("حارس الهاتف (Phone Guardian AI)")
            .setContentText(statusText)
            .setSubText("مستوى الأمان: $score%")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "فحص فوري", scanPendingIntent)
            .build()
    }

    private fun updateNotification(statusText: String, score: Int) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildOngoingNotification(statusText, score))
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "حارس الهاتف - المراقبة المستمرة",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعار مستمر يوضح حالة الأمان والعميل الذكي"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "phone_guardian_agent_channel"
        const val NOTIFICATION_ID = 9001
        const val ACTION_TRIGGER_SCAN = "com.phoneguardian.action.TRIGGER_SCAN"

        fun start(context: Context) {
            val intent = Intent(context, PhoneGuardianService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
