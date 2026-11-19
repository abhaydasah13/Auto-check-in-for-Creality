package com.creality.autocheckin.data

import android.util.Log
import com.creality.autocheckin.data.model.CheckInResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

data class CookieDetails(
    val token: String,
    val userId: String,
    val deviceId: String,
    val isValid: Boolean
)

class CrealityApiRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
) {

    companion object {
        private const val TAG = "CrealityApiRepo"
        const val CHECK_IN_URL = "https://www.crealitycloud.com/api/cxy/v2/task/checkinV2"
        const val REFERER_URL = "https://www.crealitycloud.com/check-in"
        const val ORIGIN_URL = "https://www.crealitycloud.com"
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }

    fun parseCookies(cookieStr: String): Map<String, String> {
        val cookieMap = mutableMapOf<String, String>()
        if (cookieStr.isBlank()) return cookieMap

        val parts = cookieStr.split(";")
        for (part in parts) {
            val trimmed = part.trim()
            val eqIdx = trimmed.indexOf('=')
            if (eqIdx > 0) {
                val key = trimmed.substring(0, eqIdx).trim()
                val value = trimmed.substring(eqIdx + 1).trim()
                if (key.isNotEmpty()) {
                    cookieMap[key] = value
                }
            }
        }
        return cookieMap
    }

    fun inspectCookie(cookieStr: String, fallbackDeviceId: String = ""): CookieDetails {
        val map = parseCookies(cookieStr)
        val token = map["model_token"] ?: ""
        val userId = map["model_user_id"] ?: ""
        val deviceId = map["model_device_id"] ?: fallbackDeviceId
        val isValid = token.isNotBlank() && userId.isNotBlank()
        return CookieDetails(token = token, userId = userId, deviceId = deviceId, isValid = isValid)
    }

    suspend fun performCheckIn(
        cookieStr: String,
        fallbackDeviceId: String = UUID.randomUUID().toString()
    ): CheckInResult = withContext(Dispatchers.IO) {
        if (cookieStr.isBlank()) {
            return@withContext CheckInResult(
                isSuccess = false,
                code = -100,
                message = "Missing cookie credentials. Please log in or paste cookies.",
                rawJson = """{"error": "Cookie string is empty"}"""
            )
        }

        val parsedCookies = parseCookies(cookieStr)
        val token = parsedCookies["model_token"] ?: ""
        val userId = parsedCookies["model_user_id"] ?: ""
        val deviceId = parsedCookies["model_device_id"]?.takeIf { it.isNotBlank() } ?: fallbackDeviceId

        if (token.isBlank() || userId.isBlank()) {
            return@withContext CheckInResult(
                isSuccess = false,
                code = -101,
                message = "Invalid cookie credentials. Missing model_token or model_user_id.",
                rawJson = """{"error": "model_token or model_user_id missing in provided cookie"}"""
            )
        }

        val emptyJsonBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaType())
        val requestId = UUID.randomUUID().toString()

        val request = Request.Builder()
            .url(CHECK_IN_URL)
            .post(emptyJsonBody)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("Origin", ORIGIN_URL)
            .header("Referer", REFERER_URL)
            .header("Cookie", cookieStr)
            .header("__cxy_app_ch_", "Chrome 157.0.0.0")
            .header("__cxy_app_id_", "cxy-gen2")
            .header("__cxy_app_ver_", "7.3.28")
            .header("__cxy_brand_", "creality")
            .header("__cxy_duid_", deviceId)
            .header("__cxy_os_lang_", "0")
            .header("__cxy_os_ver_", "Windows 10")
            .header("__cxy_platform_", "2")
            .header("__cxy_requestid_", requestId)
            .header("__cxy_timezone_", "19800")
            .header("__cxy_token_", token)
            .header("__cxy_uid_", userId)
            .build()

        try {
            Log.d(TAG, "Executing check-in for user: $userId, reqId: $requestId")
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                val httpCode = response.code

                if (!response.isSuccessful) {
                    Log.w(TAG, "HTTP error $httpCode: $responseBody")
                    return@withContext CheckInResult(
                        isSuccess = false,
                        code = httpCode,
                        message = "HTTP Server Error ($httpCode): ${response.message}",
                        rawJson = responseBody
                    )
                }

                if (responseBody.isBlank()) {
                    return@withContext CheckInResult(
                        isSuccess = false,
                        code = -102,
                        message = "Empty response received from Creality API",
                        rawJson = "{}"
                    )
                }

                try {
                    val json = JSONObject(responseBody)
                    val apiCode = json.optInt("code", -1)
                    val msg = json.optString("msg", json.optString("message", "Unknown"))
                    val isCodeZero = apiCode == 0

                    val resultObj = json.optJSONObject("result")
                    val continueDays = resultObj?.optInt("continueDays", 0) ?: 0
                    val hasDid = resultObj?.optBoolean("hasDid", false) ?: false

                    var rewardVal = 0
                    val rewardsArray = resultObj?.optJSONArray("rewards")
                    if (rewardsArray != null && rewardsArray.length() > 0) {
                        rewardVal = rewardsArray.optJSONObject(0)?.optInt("rewardValue", 0) ?: 0
                    }

                    // Format JSON nicely with 2-space indentation for display
                    val formattedJson = try {
                        json.toString(2)
                    } catch (e: Exception) {
                        responseBody
                    }

                    if (isCodeZero) {
                        Log.i(TAG, "Check-in successful! continueDays=$continueDays, reward=$rewardVal")
                        CheckInResult(
                            isSuccess = true,
                            code = 0,
                            message = if (hasDid) "Checked in today ($continueDays days streak)" else "Check-in successful! (+$rewardVal pt)",
                            continueDays = continueDays,
                            hasDid = hasDid,
                            rewardValue = rewardVal,
                            rawJson = formattedJson
                        )
                    } else {
                        Log.w(TAG, "Check-in failed with code $apiCode: $msg")
                        CheckInResult(
                            isSuccess = false,
                            code = apiCode,
                            message = if (apiCode == 10004 || msg.contains("token", ignoreCase = true)) {
                                "Session Expired (Code $apiCode). Please re-login."
                            } else {
                                "API Error ($apiCode): $msg"
                            },
                            continueDays = continueDays,
                            hasDid = hasDid,
                            rewardValue = rewardVal,
                            rawJson = formattedJson
                        )
                    }
                } catch (jsonErr: Exception) {
                    Log.e(TAG, "JSON parsing error: ${jsonErr.message}", jsonErr)
                    CheckInResult(
                        isSuccess = false,
                        code = -103,
                        message = "Failed to parse API response JSON: ${jsonErr.message}",
                        rawJson = responseBody
                    )
                }
            }
        } catch (ioe: IOException) {
            Log.e(TAG, "Network connection error: ${ioe.message}", ioe)
            throw ioe // Let caller / worker catch IOException to support Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            CheckInResult(
                isSuccess = false,
                code = -999,
                message = "Exception: ${e.localizedMessage ?: "Unknown error"}",
                rawJson = """{"exception": "${e.localizedMessage}"""".trimMargin()
            )
        }
    }
}
