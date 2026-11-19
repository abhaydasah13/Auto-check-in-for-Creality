package com.creality.autocheckin.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.creality.autocheckin.data.CrealityApiRepository
import com.creality.autocheckin.ui.theme.CrealityGreen
import com.creality.autocheckin.ui.theme.DarkBackground
import com.creality.autocheckin.ui.theme.TextPrimary
import com.creality.autocheckin.ui.theme.TextSecondary

private const val TAG = "WebLoginScreen"
private const val CREALITY_CHECKIN_URL = "https://www.crealitycloud.com/check-in"
private const val CREALITY_COOKIE_DOMAIN = "https://www.crealitycloud.com"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebLoginScreen(
    onNavigateBack: () -> Unit,
    onCookieCaptured: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { CrealityApiRepository() }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var pageProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var detectedCredentialsNotice by remember { mutableStateOf<String?>(null) }

    // Intercept back button to navigate internal webview history if possible
    BackHandler {
        if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Creality Cloud Login",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = detectedCredentialsNotice ?: "Log in to auto-capture session",
                            fontSize = 11.sp,
                            color = if (detectedCredentialsNotice != null) CrealityGreen else TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { webViewRef?.reload() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextPrimary
                        )
                    }

                    TextButton(
                        onClick = {
                            val cookie = CookieManager.getInstance().getCookie(CREALITY_COOKIE_DOMAIN) ?: ""
                            val details = repository.inspectCookie(cookie)
                            if (details.isValid) {
                                Toast.makeText(context, "Session captured! (UID: ${details.userId})", Toast.LENGTH_SHORT).show()
                                onCookieCaptured(cookie)
                                onNavigateBack()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Saved current cookies (model_token pending login)",
                                    Toast.LENGTH_LONG
                                ).show()
                                onCookieCaptured(cookie)
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CrealityGreen
                        )
                        Text(
                            text = "Save & Close",
                            color = CrealityGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { pageProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = CrealityGreen,
                    trackColor = DarkBackground
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewRef = this

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                userAgentString = CrealityApiRepository.USER_AGENT
                            }

                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    pageProgress = newProgress / 100f
                                    isLoading = newProgress < 100
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false

                                    // Inspect cookies after page load
                                    val currentCookie = cookieManager.getCookie(CREALITY_COOKIE_DOMAIN) ?: ""
                                    Log.d(TAG, "Inspecting cookies from $url: length=${currentCookie.length}")

                                    val details = repository.inspectCookie(currentCookie)
                                    if (details.isValid) {
                                        Log.i(TAG, "Auto-captured valid credentials: UID=${details.userId}")
                                        detectedCredentialsNotice = "Session captured! (UID: ${details.userId})"
                                        Toast.makeText(
                                            ctx,
                                            "Login detected! Credentials saved.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onCookieCaptured(currentCookie)
                                        // Auto-close and return to Dashboard
                                        postDelayed({
                                            onNavigateBack()
                                        }, 1000)
                                    }
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    return false // keep navigation inside this webview
                                }
                            }

                            loadUrl(CREALITY_CHECKIN_URL)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.stopLoading()
            webViewRef?.destroy()
            webViewRef = null
        }
    }
}
