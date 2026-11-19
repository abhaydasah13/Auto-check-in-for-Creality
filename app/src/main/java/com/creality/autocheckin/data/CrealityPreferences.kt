package com.creality.autocheckin.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.creality.autocheckin.data.model.CheckInResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "creality_settings")

data class CrealityPreferencesState(
    val cookie: String = "",
    val continueDays: Int = 0,
    val lastRunTimestamp: Long = 0L,
    val lastRawJson: String = "",
    val isAutoCheckInEnabled: Boolean = false,
    val lastStatusCode: Int = -1,
    val lastStatusMsg: String = "No check-in performed yet",
    val hasCheckedInToday: Boolean = false,
    val rewardValue: Int = 0,
    val deviceId: String = ""
)

class CrealityPreferences(private val context: Context) {

    companion object {
        val KEY_COOKIE = stringPreferencesKey("creality_cookie")
        val KEY_CONTINUE_DAYS = intPreferencesKey("creality_continue_days")
        val KEY_LAST_TIMESTAMP = longPreferencesKey("creality_last_timestamp")
        val KEY_LAST_RAW_JSON = stringPreferencesKey("creality_last_raw_json")
        val KEY_AUTO_ENABLED = booleanPreferencesKey("creality_auto_enabled")
        val KEY_LAST_STATUS_CODE = intPreferencesKey("creality_last_status_code")
        val KEY_LAST_STATUS_MSG = stringPreferencesKey("creality_last_status_msg")
        val KEY_TODAY_CHECKED_IN = booleanPreferencesKey("creality_today_checked_in")
        val KEY_REWARD_VALUE = intPreferencesKey("creality_reward_value")
        val KEY_DEVICE_ID = stringPreferencesKey("creality_device_id")
    }

    val preferencesFlow: Flow<CrealityPreferencesState> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            CrealityPreferencesState(
                cookie = preferences[KEY_COOKIE] ?: "",
                continueDays = preferences[KEY_CONTINUE_DAYS] ?: 0,
                lastRunTimestamp = preferences[KEY_LAST_TIMESTAMP] ?: 0L,
                lastRawJson = preferences[KEY_LAST_RAW_JSON] ?: "",
                isAutoCheckInEnabled = preferences[KEY_AUTO_ENABLED] ?: false,
                lastStatusCode = preferences[KEY_LAST_STATUS_CODE] ?: -1,
                lastStatusMsg = preferences[KEY_LAST_STATUS_MSG] ?: "No check-in performed yet",
                hasCheckedInToday = preferences[KEY_TODAY_CHECKED_IN] ?: false,
                rewardValue = preferences[KEY_REWARD_VALUE] ?: 0,
                deviceId = preferences[KEY_DEVICE_ID] ?: ""
            )
        }

    suspend fun saveCookie(cookie: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_COOKIE] = cookie.trim()
        }
    }

    suspend fun saveAutoCheckInEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_ENABLED] = enabled
        }
    }

    suspend fun saveCheckInResult(result: CheckInResult) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_TIMESTAMP] = result.timestamp
            preferences[KEY_LAST_STATUS_CODE] = result.code
            preferences[KEY_LAST_STATUS_MSG] = result.message
            preferences[KEY_LAST_RAW_JSON] = result.rawJson

            if (result.isSuccess) {
                preferences[KEY_CONTINUE_DAYS] = result.continueDays
                preferences[KEY_TODAY_CHECKED_IN] = true
                preferences[KEY_REWARD_VALUE] = result.rewardValue
            } else if (result.hasDid) {
                // Already did checkin today
                preferences[KEY_TODAY_CHECKED_IN] = true
                if (result.continueDays > 0) {
                    preferences[KEY_CONTINUE_DAYS] = result.continueDays
                }
            }
        }
    }

    suspend fun getOrCreateDeviceId(): String {
        val current = context.dataStore.data.first()[KEY_DEVICE_ID]
        if (!current.isNullOrBlank()) {
            return current
        }
        val newId = UUID.randomUUID().toString()
        context.dataStore.edit { it[KEY_DEVICE_ID] = newId }
        return newId
    }

    suspend fun getCookie(): String {
        return context.dataStore.data.first()[KEY_COOKIE] ?: ""
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_COOKIE)
            preferences[KEY_LAST_STATUS_CODE] = -1
            preferences[KEY_LAST_STATUS_MSG] = "Session cleared"
            preferences[KEY_TODAY_CHECKED_IN] = false
        }
    }
}
