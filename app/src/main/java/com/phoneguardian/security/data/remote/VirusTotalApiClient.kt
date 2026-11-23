package com.phoneguardian.security.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class VirusTotalApiClient(private val apiKeyProvider: () -> String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    data class HashReport(
        val isMalicious: Boolean,
        val maliciousVotes: Int,
        val totalVotes: Int,
        val threatClassification: String?
    )

    suspend fun checkFileHash(sha256Hash: String): Result<HashReport> = withContext(Dispatchers.IO) {
        val key = apiKeyProvider()
        if (key.isBlank()) {
            return@withContext Result.failure(IllegalStateException("VirusTotal API key is not configured."))
        }

        try {
            val url = "https://www.virustotal.com/api/v3/files/$sha256Hash"
            val request = Request.Builder()
                .url(url)
                .addHeader("x-apikey", key)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.code == 404) {
                // Unknown to VirusTotal
                return@withContext Result.success(HashReport(false, 0, 0, "Unknown hash"))
            }

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("VirusTotal HTTP ${response.code}"))
            }

            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            val data = json.getJSONObject("data")
            val attributes = data.getJSONObject("attributes")
            val lastAnalysisStats = attributes.getJSONObject("last_analysis_stats")
            val malicious = lastAnalysisStats.optInt("malicious", 0)
            val suspicious = lastAnalysisStats.optInt("suspicious", 0)
            val harmless = lastAnalysisStats.optInt("harmless", 0)
            val undetected = lastAnalysisStats.optInt("undetected", 0)
            val total = malicious + suspicious + harmless + undetected

            val threatCategory = attributes.optJSONObject("popular_threat_classification")
                ?.optString("suggested_threat_label", "Unknown threat")

            Result.success(
                HashReport(
                    isMalicious = malicious > 2,
                    maliciousVotes = malicious,
                    totalVotes = total,
                    threatClassification = threatCategory
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
