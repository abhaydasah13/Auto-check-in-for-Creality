package com.creality.autocheckin.ui.screens

import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creality.autocheckin.ui.theme.AccentBlue
import com.creality.autocheckin.ui.theme.AccentOrange
import com.creality.autocheckin.ui.theme.AccentRed
import com.creality.autocheckin.ui.theme.AccentYellow
import com.creality.autocheckin.ui.theme.CrealityDarkGreen
import com.creality.autocheckin.ui.theme.CrealityGreen
import com.creality.autocheckin.ui.theme.CrealityLightGreen
import com.creality.autocheckin.ui.theme.DarkBackground
import com.creality.autocheckin.ui.theme.DarkSurface
import com.creality.autocheckin.ui.theme.DarkSurfaceVariant
import com.creality.autocheckin.ui.theme.TextMuted
import com.creality.autocheckin.ui.theme.TextPrimary
import com.creality.autocheckin.ui.theme.TextSecondary
import com.creality.autocheckin.ui.viewmodel.CheckInStatusBadge
import com.creality.autocheckin.ui.viewmodel.DashboardUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onCheckInNow: () -> Unit,
    onNavigateToWebLogin: () -> Unit,
    onToggleAutoCheckIn: (Boolean) -> Unit,
    onUpdateScheduleTime: (Int, Int) -> Unit = { _, _ -> },
    onSaveCookie: (String) -> Unit,
    onClearSession: () -> Unit,
    onClearUserMessage: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val verticalScroll = rememberScrollState()

    var isCookieSectionExpanded by remember { mutableStateOf(false) }
    var manualCookieText by remember(state.cookie) { mutableStateOf(state.cookie) }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onClearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CrealityGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CrealityGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Auto Check-In",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = "for Creality Cloud",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(verticalScroll)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Status Card (Streak, Today's Status, Timestamp)
            StatusCard(state = state)

            // 2. Background Automation Toggle Card
            AutomationToggleCard(
                isEnabled = state.isAutoCheckInEnabled,
                isScheduled = state.isWorkManagerScheduled,
                formattedTime = state.formattedScheduleTime,
                currentHour = state.scheduleHour,
                currentMinute = state.scheduleMinute,
                onToggle = onToggleAutoCheckIn,
                onUpdateTime = onUpdateScheduleTime
            )

            // 3. Primary Action Buttons
            ActionButtonsSection(
                isLoading = state.isLoading,
                onCheckInNow = onCheckInNow,
                onOpenWebLogin = onNavigateToWebLogin
            )

            // 4. Cookie Configuration & Status (Expandable)
            CookieConfigCard(
                state = state,
                isExpanded = isCookieSectionExpanded,
                cookieText = manualCookieText,
                onCookieTextChange = { manualCookieText = it },
                onToggleExpand = { isCookieSectionExpanded = !isCookieSectionExpanded },
                onSaveCookie = { onSaveCookie(manualCookieText) },
                onClearSession = onClearSession
            )

            // 5. Execution Log Card
            ExecutionLogCard(
                rawJson = state.lastRawJson,
                lastRunTime = state.formattedLastRun,
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Creality Check-In Log", state.lastRawJson)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Log copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            )

            Text(
                text = "Unofficial community utility • Not affiliated with or endorsed by Shenzhen Creality 3D Technology Co., Ltd. • All credentials remain securely stored on-device.",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusCard(state: DashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak Display
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AccentOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Streak",
                            tint = AccentOrange,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "STREAK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${state.continueDays}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "days",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                        }
                    }
                }

                // Today Status Badge
                StatusBadgeView(badge = state.statusBadge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DarkSurfaceVariant.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-metrics: Last Run & Status Msg
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Last Check-in",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = state.formattedLastRun,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }

                if (state.rewardValue > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CrealityGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+${state.rewardValue} Pt",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrealityGreen
                        )
                    }
                }
            }

            if (state.lastStatusMsg.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.lastStatusMsg,
                    fontSize = 12.sp,
                    color = if (state.lastStatusCode == 0) CrealityLightGreen else AccentRed.copy(alpha = 0.9f),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun StatusBadgeView(badge: CheckInStatusBadge) {
    val (bgColor, textColor, text, icon) = when (badge) {
        CheckInStatusBadge.CHECKED_IN -> Quadruple(
            CrealityDarkGreen.copy(alpha = 0.2f),
            CrealityGreen,
            "Checked In",
            Icons.Default.CheckCircle
        )
        CheckInStatusBadge.PENDING -> Quadruple(
            AccentYellow.copy(alpha = 0.15f),
            AccentYellow,
            "Pending",
            Icons.Default.HourglassTop
        )
        CheckInStatusBadge.SESSION_EXPIRED -> Quadruple(
            AccentRed.copy(alpha = 0.15f),
            AccentRed,
            "Session Expired",
            Icons.Default.Error
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AutomationToggleCard(
    isEnabled: Boolean,
    isScheduled: Boolean,
    formattedTime: String,
    currentHour: Int,
    currentMinute: Int,
    onToggle: (Boolean) -> Unit,
    onUpdateTime: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    val timePickerDialog = remember(currentHour, currentMinute) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onUpdateTime(hourOfDay, minute)
            },
            currentHour,
            currentMinute,
            false // 12-hour view with AM/PM
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Daily Auto Check-In",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isScheduled && isEnabled) CrealityGreen else TextMuted)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEnabled) {
                            "Active • Runs daily at $formattedTime"
                        } else {
                            "Disabled • Toggle ON for autonomous daily runs"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isEnabled) CrealityLightGreen else TextSecondary
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CrealityGreen,
                        checkedTrackColor = CrealityDarkGreen.copy(alpha = 0.5f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Time Selector Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant.copy(alpha = 0.6f))
                    .clickable { timePickerDialog.show() }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Schedule Time",
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Target Check-In Time",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = formattedTime,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEnabled) CrealityGreen else TextPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Change",
                        color = AccentBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    isLoading: Boolean,
    onCheckInNow: () -> Unit,
    onOpenWebLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Primary Check In Now
        Button(
            onClick = onCheckInNow,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CrealityGreen,
                contentColor = DarkBackground,
                disabledContainerColor = DarkSurfaceVariant,
                disabledContentColor = TextMuted
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = DarkBackground,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Checking In...",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Check In Now",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Secondary Web Login (Auto-Capture)
        OutlinedButton(
            onClick = onOpenWebLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AccentBlue
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue.copy(alpha = 0.6f))
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Web Login (Auto-Capture Cookies)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CookieConfigCard(
    state: DashboardUiState,
    isExpanded: Boolean,
    cookieText: String,
    onCookieTextChange: (String) -> Unit,
    onToggleExpand: () -> Unit,
    onSaveCookie: () -> Unit,
    onClearSession: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = if (state.tokenFound) CrealityGreen else AccentRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Credential Status",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = if (state.tokenFound && state.userIdFound) {
                                "UID: ${state.extractedUserId} • Token Active"
                            } else {
                                "Missing valid model_token / model_user_id"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (state.tokenFound) CrealityLightGreen else AccentRed
                        )
                    }
                }

                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = TextSecondary
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = "Paste your full Creality Cookie string below if manual override is needed:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cookieText,
                        onValueChange = onCookieTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        placeholder = {
                            Text(
                                text = "_ga=...; model_token=your_token; model_user_id=1234567890; ...",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onClearSession,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f))
                        ) {
                            Text("Clear", fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = onSaveCookie,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrealityDarkGreen,
                                contentColor = TextPrimary
                            )
                        ) {
                            Text("Save Cookie", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExecutionLogCard(
    rawJson: String,
    lastRunTime: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Last Response Payload",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = lastRunTime,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                if (rawJson.isNotBlank()) {
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy JSON",
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val displayContent = if (rawJson.isBlank()) {
                "// No execution log yet. Tap 'Check In Now' to trigger an API call."
            } else {
                rawJson
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0B1120))
                    .padding(12.dp)
            ) {
                val horizontalScroll = rememberScrollState()
                val logVerticalScroll = rememberScrollState()

                Text(
                    text = displayContent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = if (rawJson.isBlank()) TextMuted else TextPrimary,
                    modifier = Modifier
                        .verticalScroll(logVerticalScroll)
                        .horizontalScroll(horizontalScroll)
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
