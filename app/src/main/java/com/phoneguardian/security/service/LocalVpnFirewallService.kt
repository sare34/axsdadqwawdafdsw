package com.phoneguardian.security.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.phoneguardian.security.R
import com.phoneguardian.security.data.local.SecurityPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class LocalVpnFirewallService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var workerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var preferences: SecurityPreferences

    override fun onCreate() {
        super.onCreate()
        preferences = SecurityPreferences(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopVpn()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildVpnNotification())
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if (vpnInterface != null) return

        try {
            val builder = Builder()
                .setSession("PhoneGuardianShield")
                .addAddress("10.0.0.2", 32)
                .addDnsServer("1.1.1.1")
                // Protect our own app package from looping into VPN
                .addDisallowedApplication(packageName)

            vpnInterface = builder.establish()
            val pfd = vpnInterface ?: return

            isVpnRunning = true

            workerJob = scope.launch {
                val inputStream = FileInputStream(pfd.fileDescriptor)
                val outputStream = FileOutputStream(pfd.fileDescriptor)
                val packet = ByteBuffer.allocate(32767)

                val blockedIps = preferences.blockedIpList

                while (isActive) {
                    val length = try {
                        inputStream.read(packet.array())
                    } catch (e: Exception) {
                        break
                    }

                    if (length > 0) {
                        packet.limit(length)
                        // Simple IPv4 Header Parsing
                        val versionAndIhl = packet.get(0).toInt()
                        val ipVersion = (versionAndIhl shr 4) and 0x0F

                        if (ipVersion == 4 && length >= 20) {
                            val destIp = "${packet.get(16).toUByte()}.${packet.get(17).toUByte()}.${packet.get(18).toUByte()}.${packet.get(19).toUByte()}"

                            // Autonomous firewall drop rule
                            if (blockedIps.contains(destIp)) {
                                // Drop packet silently (block malicious connection)
                                packet.clear()
                                continue
                            }
                        }

                        packet.clear()
                    }
                }
            }
        } catch (e: Exception) {
            stopVpn()
        }
    }

    private fun stopVpn() {
        isVpnRunning = false
        workerJob?.cancel()
        workerJob = null
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            // Ignore
        }
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    private fun buildVpnNotification(): Notification {
        val channelId = "vpn_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "حاجز الحماية المحلي (Local VPN Firewall)",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("مراقب حركة الشبكة المحلي نشط")
            .setContentText("يتم فحص وحظر الاتصالات الخبيثة مباشرة على جهازك دون إرسالها لأي خادم خارجي.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_START = "com.phoneguardian.vpn.START"
        const val ACTION_STOP = "com.phoneguardian.vpn.STOP"
        const val NOTIFICATION_ID = 9002

        @Volatile
        var isVpnRunning = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, LocalVpnFirewallService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocalVpnFirewallService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
