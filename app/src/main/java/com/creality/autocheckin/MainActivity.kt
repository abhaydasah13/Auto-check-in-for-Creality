package com.creality.autocheckin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.creality.autocheckin.ui.screens.DashboardScreen
import com.creality.autocheckin.ui.screens.WebLoginScreen
import com.creality.autocheckin.ui.theme.CrealityAutoCheckInTheme
import com.creality.autocheckin.ui.theme.DarkBackground
import com.creality.autocheckin.ui.viewmodel.MainViewModel

enum class AppScreen {
    DASHBOARD,
    WEB_LOGIN
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CrealityAutoCheckInTheme(darkTheme = true) {
                // Request POST_NOTIFICATIONS permission on Android 13+
                RequestNotificationPermission()

                val uiState by viewModel.uiState.collectAsState()
                var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.DASHBOARD -> {
                                DashboardScreen(
                                    state = uiState,
                                    onCheckInNow = { viewModel.performManualCheckIn() },
                                    onNavigateToWebLogin = { currentScreen = AppScreen.WEB_LOGIN },
                                    onToggleAutoCheckIn = { viewModel.toggleAutoCheckIn(it) },
                                    onSaveCookie = { viewModel.saveManualCookie(it) },
                                    onClearSession = { viewModel.clearSession() },
                                    onClearUserMessage = { viewModel.clearUserMessage() }
                                )
                            }
                            AppScreen.WEB_LOGIN -> {
                                WebLoginScreen(
                                    onNavigateBack = { currentScreen = AppScreen.DASHBOARD },
                                    onCookieCaptured = { cookie ->
                                        viewModel.onCookieCapturedFromWeb(cookie)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { /* Permission handled */ }
        )

        val context = androidx.compose.ui.platform.LocalContext.current
        LaunchedEffect(Unit) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!isGranted) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
