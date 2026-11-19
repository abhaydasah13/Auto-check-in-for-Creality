package com.creality.autocheckin.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.creality.autocheckin.data.CrealityApiRepository
import com.creality.autocheckin.data.CrealityPreferences
import com.creality.autocheckin.data.model.CheckInResult
import java.io.IOException

class CheckInWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "CheckInWorker"
        const val WORK_NAME = "CrealityDailyCheckIn"
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "Starting automated daily check-in background job...")

        val preferences = CrealityPreferences(appContext)
        val repository = CrealityApiRepository()

        val cookie = preferences.getCookie()
        val deviceId = preferences.getOrCreateDeviceId()

        if (cookie.isBlank()) {
            Log.w(TAG, "No cookie credentials stored. Check-in aborted.")
            NotificationHelper.showCheckInNotification(
                context = appContext,
                title = "Creality Check-In Alert",
                message = "Session Expired • Tap to log in",
                isSuccess = false
            )
            val errorResult = CheckInResult(
                isSuccess = false,
                code = -100,
                message = "No stored cookie found. Please log in to enable automated check-in.",
                rawJson = """{"error": "Missing cookie credentials"}"""
            )
            preferences.saveCheckInResult(errorResult)
            return Result.failure()
        }

        return try {
            val result = repository.performCheckIn(cookieStr = cookie, fallbackDeviceId = deviceId)
            preferences.saveCheckInResult(result)

            if (result.isSuccess) {
                val rewardText = if (result.rewardValue > 0) " (+${result.rewardValue} pt)" else ""
                val msg = "Check-in Successful • Streak: ${result.continueDays} days$rewardText"
                Log.i(TAG, msg)
                NotificationHelper.showCheckInNotification(
                    context = appContext,
                    title = "Creality Cloud Check-In",
                    message = msg,
                    isSuccess = true
                )
                Result.success()
            } else {
                Log.w(TAG, "Check-in returned code ${result.code}: ${result.message}")
                val isSessionIssue = result.code == 10004 || result.message.contains("expired", ignoreCase = true) || result.message.contains("token", ignoreCase = true)
                val notifMsg = if (isSessionIssue) {
                    "Session Expired • Tap to re-login"
                } else {
                    "Check-in Alert • ${result.message}"
                }
                NotificationHelper.showCheckInNotification(
                    context = appContext,
                    title = "Creality Cloud Check-In",
                    message = notifMsg,
                    isSuccess = false
                )
                Result.failure()
            }
        } catch (ioe: IOException) {
            Log.w(TAG, "Transient network error during check-in, scheduling retry: ${ioe.message}")
            Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in CheckInWorker: ${e.message}", e)
            val exceptionResult = CheckInResult(
                isSuccess = false,
                code = -999,
                message = "Worker error: ${e.localizedMessage}",
                rawJson = """{"exception": "${e.localizedMessage}""""
            )
            preferences.saveCheckInResult(exceptionResult)
            Result.failure()
        }
    }
}
