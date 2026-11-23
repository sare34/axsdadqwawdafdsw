package com.phoneguardian.security.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.phoneguardian.security.PhoneGuardianApp
import java.util.concurrent.TimeUnit

class SecurityScanWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? PhoneGuardianApp ?: return Result.failure()
        return try {
            app.decisionEngine.runDeepScan()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "phone_guardian_periodic_scan"

        fun schedule(context: Context, intervalHours: Long = 1) {
            val workRequest = PeriodicWorkRequestBuilder<SecurityScanWorker>(
                intervalHours.coerceAtLeast(1),
                TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }
}
