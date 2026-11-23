package com.phoneguardian.security.service.shizuku

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

class ShizukuManager(private val context: Context) {

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    fun hasShizukuPermission(): Boolean {
        return try {
            if (!isShizukuAvailable()) return false
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Throwable) {
            false
        }
    }

    fun requestShizukuPermission(requestCode: Int) {
        try {
            if (isShizukuAvailable() && !hasShizukuPermission()) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (e: Throwable) {
            // Ignore
        }
    }

    /**
     * Executes shell command via Shizuku binder.
     * Note: Respects Safety Rule: NEVER uninstall, delete, or disable apps without explicit approval.
     */
    fun executeShell(command: String): Result<String> {
        if (!hasShizukuPermission()) {
            return Result.failure(IllegalStateException("Shizuku is not running or permission is denied"))
        }

        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val error = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                Result.success(output.trim())
            } else {
                Result.failure(Exception("Shell exited with $exitCode: $error"))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    /**
     * Revoke a specific permission for a package via Shizuku
     */
    fun revokePermission(packageName: String, permission: String): Result<String> {
        val cmd = "pm revoke $packageName $permission"
        return executeShell(cmd)
    }

    /**
     * Turn off unsafe global setting like adb_wifi_enabled
     */
    fun disableUnsafeSetting(table: String, settingName: String): Result<String> {
        val cmd = "settings put $table $settingName 0"
        return executeShell(cmd)
    }
}
