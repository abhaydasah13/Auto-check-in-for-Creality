package com.creality.autocheckin

import android.app.Application
import com.creality.autocheckin.data.CrealityPreferences
import com.creality.autocheckin.worker.CheckInScheduler
import com.creality.autocheckin.worker.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CrealityApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)

        // Ensure scheduler state matches user preference on app boot
        CoroutineScope(Dispatchers.IO).launch {
            val preferences = CrealityPreferences(this@CrealityApplication)
            val isEnabled = preferences.preferencesFlow.first().isAutoCheckInEnabled
            if (isEnabled) {
                CheckInScheduler.scheduleDailyCheckIn(this@CrealityApplication)
            }
        }
    }
}
