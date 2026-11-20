package com.creality.autocheckin.ui.screens

import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
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
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlinx.coroutines.launch

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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var isCookieSectionExpanded by remember { mutableStateOf(false) }
    var manualCookieText by remember(state.cookie) { mutableStateOf(state.cookie) }
    var showCredentialsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onClearUserMessage()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                state = state,
                onCloseDrawer = { coroutineScope.launch { drawerState.close() } },
                onOpenCredentialsDialog = {
                    coroutineScope.launch { drawerState.close() }
                    showCredentialsDialog = true
                },
                onOpenAboutDialog = {
                    coroutineScope.launch { drawerState.close() }
                    showAboutDialog = true
                },
                onNavigateToWebLogin = {
                    coroutineScope.launch { drawerState.close() }
                    onNavigateToWebLogin()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu",
                                tint = TextPrimary
                            )
                        }
                    },
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
                    actions = {
                        IconButton(onClick = onCheckInNow) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync Streak",
                                tint = TextPrimary
                            )
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

    // Credential Status Dialog
    if (showCredentialsDialog) {
        CredentialStatusDialog(
            state = state,
            manualCookieText = manualCookieText,
            onManualCookieChange = { manualCookieText = it },
            onSaveCookie = { onSaveCookie(manualCookieText) },
            onClearSession = onClearSession,
            onOpenWebLogin = onNavigateToWebLogin,
            onDismiss = { showCredentialsDialog = false }
        )
    }

    // About Developer & Products Dialog
    if (showAboutDialog) {
        AboutDeveloperDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
}

@Composable
private fun AppDrawerContent(
    state: DashboardUiState,
    onCloseDrawer: () -> Unit,
    onOpenCredentialsDialog: () -> Unit,
    onOpenAboutDialog: () -> Unit,
    onNavigateToWebLogin: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    ModalDrawerSheet(
        drawerContainerColor = DarkSurface,
        drawerContentColor = TextPrimary,
        modifier = Modifier
            .width(320.dp)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CrealityGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CrealityGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Auto Check-In",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "for Creality Cloud • v1.2.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                IconButton(onClick = onCloseDrawer) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Menu",
                        tint = TextSecondary
                    )
                }
            }

            HorizontalDivider(color = DarkSurfaceVariant)

            // Section 1: User Credential Quick Card
            Text(
                text = "ACCOUNT & CREDENTIALS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCredentialsDialog() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = if (state.tokenFound && state.userIdFound) CrealityGreen else AccentRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Credential Status",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (state.tokenFound && state.userIdFound) CrealityGreen.copy(alpha = 0.15f)
                                    else AccentRed.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (state.tokenFound && state.userIdFound) "ACTIVE" else "MISSING",
                                color = if (state.tokenFound && state.userIdFound) CrealityLightGreen else AccentRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.userIdFound) {
                        Text(
                            text = "UID: ${state.extractedUserId}",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Text(
                            text = "No active session detected",
                            fontSize = 12.sp,
                            color = AccentOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap to view details & cookies →",
                        fontSize = 11.sp,
                        color = AccentBlue
                    )
                }
            }

            NavigationDrawerItem(
                label = { Text("Web Login (Auto-Capture)", fontSize = 13.sp) },
                icon = { Icon(Icons.Default.Language, contentDescription = null, tint = AccentBlue) },
                selected = false,
                onClick = onNavigateToWebLogin,
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                    unselectedTextColor = TextPrimary
                )
            )

            NavigationDrawerItem(
                label = { Text("Credential Manager", fontSize = 13.sp) },
                icon = { Icon(Icons.Default.Key, contentDescription = null, tint = CrealityGreen) },
                selected = false,
                onClick = onOpenCredentialsDialog,
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                    unselectedTextColor = TextPrimary
                )
            )

            HorizontalDivider(color = DarkSurfaceVariant)

            // Section 2: Developer & About
            Text(
                text = "ABOUT DEVELOPER & PRODUCTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            // Developer Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAboutDialog() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Abhay Das",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Maker • 3D Printing • Developer",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Social & Product Links
            SocialLinkItem(
                icon = Icons.Default.PhotoCamera,
                title = "Instagram",
                subtitle = "@printinindia",
                accentColor = Color(0xFFE1306C),
                onClick = { launchUrl(context, "https://www.instagram.com/printinindia/") }
            )

            SocialLinkItem(
                icon = Icons.Default.ShoppingBag,
                title = "Online Shop",
                subtitle = "Diecastdripz Shop",
                accentColor = Color(0xFFFF9800),
                onClick = { launchUrl(context, "https://linktr.ee/Diecastdripz/shop") }
            )

            SocialLinkItem(
                icon = Icons.Default.BusinessCenter,
                title = "LinkedIn",
                subtitle = "Abhay Das",
                accentColor = Color(0xFF0A66C2),
                onClick = { launchUrl(context, "https://www.linkedin.com/in/abhaydasah/") }
            )

            HorizontalDivider(color = DarkSurfaceVariant)

            // Legal & Privacy Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        launchUrl(
                            context,
                            "https://github.com/abhaydasah13/Auto-check-in-for-Creality/blob/main/PRIVACY_POLICY.md"
                        )
                    }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Policy,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Privacy Policy",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer disclaimer
            Text(
                text = "Independent community utility. Not affiliated with Shenzhen Creality 3D Technology Co., Ltd.",
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun SocialLinkItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Open Link",
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun AboutDeveloperDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "About Developer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Abhay Das",
                        style = MaterialTheme.typography.labelSmall,
                        color = CrealityGreen
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Maker & 3D printing enthusiast building tools, products, and automation utilities for the community.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                HorizontalDivider(color = DarkSurfaceVariant)

                Text(
                    text = "CONNECT & EXPLORE PRODUCTS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )

                SocialLinkItem(
                    icon = Icons.Default.PhotoCamera,
                    title = "Instagram (@printinindia)",
                    subtitle = "3D printing creations & builds",
                    accentColor = Color(0xFFE1306C),
                    onClick = { launchUrl(context, "https://www.instagram.com/printinindia/") }
                )

                SocialLinkItem(
                    icon = Icons.Default.ShoppingBag,
                    title = "Online Shop (Diecastdripz)",
                    subtitle = "Exclusive custom products & shop",
                    accentColor = Color(0xFFFF9800),
                    onClick = { launchUrl(context, "https://linktr.ee/Diecastdripz/shop") }
                )

                SocialLinkItem(
                    icon = Icons.Default.BusinessCenter,
                    title = "LinkedIn (Abhay Das)",
                    subtitle = "Professional profile & connect",
                    accentColor = Color(0xFF0A66C2),
                    onClick = { launchUrl(context, "https://www.linkedin.com/in/abhaydasah/") }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "App: Auto Check-In for Creality Cloud\nVersion: 1.2.0 • Target SDK 34",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrealityDarkGreen,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun CredentialStatusDialog(
    state: DashboardUiState,
    manualCookieText: String,
    onManualCookieChange: (String) -> Unit,
    onSaveCookie: () -> Unit,
    onClearSession: () -> Unit,
    onOpenWebLogin: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isEditingCookie by remember { mutableStateOf(false) }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (state.tokenFound && state.userIdFound) CrealityGreen.copy(alpha = 0.2f)
                            else AccentRed.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = if (state.tokenFound && state.userIdFound) CrealityGreen else AccentRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Credential Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (state.tokenFound && state.userIdFound) "Session Active" else "Session Incomplete",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.tokenFound && state.userIdFound) CrealityLightGreen else AccentRed
                    )
                }
            }
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // User ID
                CredentialItemRow(
                    label = "User ID (model_user_id)",
                    value = state.extractedUserId.ifBlank { "Not Found" },
                    isValid = state.userIdFound,
                    onCopy = if (state.userIdFound) { { copyToClipboard("User ID", state.extractedUserId) } } else null
                )

                // Token
                val maskedToken = if (state.tokenFound) {
                    if (state.extractedToken.length > 16) {
                        "${state.extractedToken.take(8)}...${state.extractedToken.takeLast(8)}"
                    } else state.extractedToken
                } else "Not Found"

                CredentialItemRow(
                    label = "Auth Token (model_token)",
                    value = maskedToken,
                    isValid = state.tokenFound,
                    onCopy = if (state.tokenFound) { { copyToClipboard("Auth Token", state.extractedToken) } } else null
                )

                // Device ID
                CredentialItemRow(
                    label = "Device UUID",
                    value = state.deviceId.ifBlank { "Auto-generated" },
                    isValid = state.deviceId.isNotBlank(),
                    onCopy = { copyToClipboard("Device ID", state.deviceId) }
                )

                HorizontalDivider(color = DarkSurfaceVariant)

                // Actions: Web Login / Manual Edit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenWebLogin()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Web Login", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { isEditingCookie = !isEditingCookie },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TextMuted)
                    ) {
                        Text(if (isEditingCookie) "Hide Edit" else "Edit Cookie", fontSize = 12.sp)
                    }
                }

                if (isEditingCookie) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Manual Cookie String:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        OutlinedTextField(
                            value = manualCookieText,
                            onValueChange = onManualCookieChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            placeholder = {
                                Text(
                                    text = "model_token=...; model_user_id=...",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = onClearSession,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f))
                            ) {
                                Text("Clear", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onSaveCookie()
                                    isEditingCookie = false
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CrealityDarkGreen)
                            ) {
                                Text("Save", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrealityDarkGreen,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun CredentialItemRow(
    label: String,
    value: String,
    isValid: Boolean,
    onCopy: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 10.sp, color = TextMuted)
            Text(
                text = value,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isValid) TextPrimary else AccentRed,
                fontWeight = if (isValid) FontWeight.Medium else FontWeight.Normal
            )
        }

        if (onCopy != null) {
            IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = AccentBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun launchUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
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
                Text(
                    text = "CHECK-IN STREAK",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    letterSpacing = 1.2.sp
                )
                CheckInBadgePill(badge = state.statusBadge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${state.continueDays}",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    color = if (state.continueDays > 0) CrealityGreen else TextPrimary,
                    lineHeight = 56.sp
                )
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = "Days",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Text(
                        text = "Consecutive Streak",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DarkSurfaceVariant)
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailInfoItem(
                    label = "Today's Status",
                    value = if (state.hasCheckedInToday) "Checked In" else "Pending Check-In",
                    valueColor = if (state.hasCheckedInToday) CrealityLightGreen else AccentOrange
                )
                DetailInfoItem(
                    label = "Cuvacoins Earned",
                    value = if (state.rewardValue > 0) "+${state.rewardValue} Cuvacoins" else "--",
                    valueColor = AccentYellow
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            DetailInfoItem(
                label = "Last Execution",
                value = state.formattedLastRun,
                valueColor = TextSecondary
            )
        }
    }
}

@Composable
private fun DetailInfoItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun CheckInBadgePill(badge: CheckInStatusBadge) {
    val (text, bgColor, textColor, icon) = when (badge) {
        CheckInStatusBadge.CHECKED_IN -> Quadruple(
            "Checked In Today",
            CrealityDarkGreen.copy(alpha = 0.35f),
            CrealityLightGreen,
            Icons.Default.CheckCircle
        )
        CheckInStatusBadge.PENDING -> Quadruple(
            "Pending",
            AccentYellow.copy(alpha = 0.2f),
            AccentYellow,
            Icons.Default.HourglassTop
        )
        CheckInStatusBadge.SESSION_EXPIRED -> Quadruple(
            "Session Expired",
            AccentRed.copy(alpha = 0.2f),
            AccentRed,
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
