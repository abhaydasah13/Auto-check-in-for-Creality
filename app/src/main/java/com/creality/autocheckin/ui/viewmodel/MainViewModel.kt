package com.creality.autocheckin.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.creality.autocheckin.data.CrealityApiRepository
import com.creality.autocheckin.data.CrealityPreferences
import com.creality.autocheckin.data.model.CheckInResult
import com.creality.autocheckin.worker.CheckInScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class CheckInStatusBadge {
    CHECKED_IN,
    PENDING,
    SESSION_EXPIRED
}

data class DashboardUiState(
    val cookie: String = "",
    val continueDays: Int = 0,
    val lastRunTimestamp: Long = 0L,
    val lastRawJson: String = "",
    val isAutoCheckInEnabled: Boolean = false,
    val lastStatusCode: Int = -1,
    val lastStatusMsg: String = "No check-in performed yet",
    val hasCheckedInToday: Boolean = false,
    val rewardValue: Int = 0,
    val deviceId: String = "",
    val isLoading: Boolean = false,
    val statusBadge: CheckInStatusBadge = CheckInStatusBadge.PENDING,
    val tokenFound: Boolean = false,
    val userIdFound: Boolean = false,
    val extractedToken: String = "",
    val extractedUserId: String = "",
    val isWorkManagerScheduled: Boolean = false,
    val userMessage: String? = null,
    val scheduleHour: Int = 6,
    val scheduleMinute: Int = 0
) {
    val formattedLastRun: String
        get() {
            if (lastRunTimestamp <= 0L) return "Never"
            val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(lastRunTimestamp))
        }

    val formattedScheduleTime: String
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, scheduleHour)
                set(Calendar.MINUTE, scheduleMinute)
            }
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(cal.time)
        }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = CrealityPreferences(application)
    private val repository = CrealityApiRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Collect preferences
        viewModelScope.launch {
            preferences.preferencesFlow.collectLatest { prefs ->
                val cookieDetails = repository.inspectCookie(prefs.cookie, prefs.deviceId)
                val badge = computeBadge(
                    cookie = prefs.cookie,
                    hasCheckedInToday = prefs.hasCheckedInToday,
                    lastStatusCode = prefs.lastStatusCode,
                    lastRunTimestamp = prefs.lastRunTimestamp
                )

                _uiState.update { current ->
                    current.copy(
                        cookie = prefs.cookie,
                        continueDays = prefs.continueDays,
                        lastRunTimestamp = prefs.lastRunTimestamp,
                        lastRawJson = prefs.lastRawJson,
                        isAutoCheckInEnabled = prefs.isAutoCheckInEnabled,
                        lastStatusCode = prefs.lastStatusCode,
                        lastStatusMsg = prefs.lastStatusMsg,
                        hasCheckedInToday = prefs.hasCheckedInToday,
                        rewardValue = prefs.rewardValue,
                        deviceId = prefs.deviceId,
                        statusBadge = badge,
                        tokenFound = cookieDetails.token.isNotBlank(),
                        userIdFound = cookieDetails.userId.isNotBlank(),
                        extractedToken = cookieDetails.token,
                        extractedUserId = cookieDetails.userId,
                        scheduleHour = prefs.scheduleHour,
                        scheduleMinute = prefs.scheduleMinute
                    )
                }
            }
        }

        // Observe WorkManager periodic task status
        viewModelScope.launch {
            CheckInScheduler.getWorkInfoFlow(getApplication()).collectLatest { workInfoList ->
                val isEnqueued = workInfoList.any {
                    it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
                }
                _uiState.update { it.copy(isWorkManagerScheduled = isEnqueued) }
            }
        }
    }

    private fun computeBadge(
        cookie: String,
        hasCheckedInToday: Boolean,
        lastStatusCode: Int,
        lastRunTimestamp: Long
    ): CheckInStatusBadge {
        if (cookie.isBlank()) return CheckInStatusBadge.SESSION_EXPIRED
        if (lastStatusCode == 10004 || lastStatusCode == -100 || lastStatusCode == -101) {
            return CheckInStatusBadge.SESSION_EXPIRED
        }

        // Check if last successful check-in was today (within 24 hours & same day)
        if (lastRunTimestamp > 0L) {
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val todayStr = sdf.format(Date())
            val lastRunStr = sdf.format(Date(lastRunTimestamp))
            if (todayStr == lastRunStr && (hasCheckedInToday || lastStatusCode == 0)) {
                return CheckInStatusBadge.CHECKED_IN
            }
        }

        return CheckInStatusBadge.PENDING
    }

    fun performManualCheckIn() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, userMessage = null) }
            val cookie = _uiState.value.cookie
            val deviceId = preferences.getOrCreateDeviceId()

            try {
                val result = repository.performCheckIn(cookieStr = cookie, fallbackDeviceId = deviceId)
                preferences.saveCheckInResult(result)

                val message = if (result.isSuccess) {
                    "Success: Streak ${result.continueDays} days (+${result.rewardValue} pt)"
                } else {
                    "Notice: ${result.message}"
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userMessage = message
                    )
                }
            } catch (ioe: IOException) {
                val errResult = CheckInResult(
                    isSuccess = false,
                    code = -500,
                    message = "Network error: ${ioe.localizedMessage ?: "Connection timed out"}",
                    rawJson = """{"network_error": "${ioe.localizedMessage}"}"""
                )
                preferences.saveCheckInResult(errResult)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userMessage = "Network error: Please check your internet connection."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userMessage = "Error: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun toggleAutoCheckIn(enable: Boolean) {
        viewModelScope.launch {
            preferences.saveAutoCheckInEnabled(enable)
            if (enable) {
                CheckInScheduler.scheduleDailyCheckIn(
                    context = getApplication(),
                    targetHour = _uiState.value.scheduleHour,
                    targetMinute = _uiState.value.scheduleMinute
                )
                _uiState.update { it.copy(userMessage = "Daily auto check-in scheduled for ${_uiState.value.formattedScheduleTime}") }
            } else {
                CheckInScheduler.cancelDailyCheckIn(getApplication())
                _uiState.update { it.copy(userMessage = "Daily check-in automation disabled.") }
            }
        }
    }

    fun updateScheduleTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferences.saveScheduleTime(hour, minute)
            if (_uiState.value.isAutoCheckInEnabled) {
                CheckInScheduler.scheduleDailyCheckIn(
                    context = getApplication(),
                    targetHour = hour,
                    targetMinute = minute
                )
            }
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
            _uiState.update { it.copy(userMessage = "Check-in time updated to $timeStr") }
        }
    }

    fun saveManualCookie(cookieInput: String) {
        viewModelScope.launch {
            val trimmed = cookieInput.trim()
            preferences.saveCookie(trimmed)
            val details = repository.inspectCookie(trimmed)
            val msg = if (details.isValid) {
                "Cookie saved successfully! (UID: ${details.userId})"
            } else {
                "Cookie saved, but model_token or model_user_id was not detected."
            }
            _uiState.update { it.copy(userMessage = msg) }
        }
    }

    fun onCookieCapturedFromWeb(cookie: String) {
        viewModelScope.launch {
            preferences.saveCookie(cookie.trim())
            val details = repository.inspectCookie(cookie)
            val msg = if (details.isValid) {
                "Web login session captured successfully! User ID: ${details.userId}"
            } else {
                "Session cookies updated."
            }
            _uiState.update { it.copy(userMessage = msg) }
        }
    }

    fun clearSession() {
        viewModelScope.launch {
            preferences.clearSession()
            _uiState.update { it.copy(userMessage = "Session and credentials cleared.") }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
