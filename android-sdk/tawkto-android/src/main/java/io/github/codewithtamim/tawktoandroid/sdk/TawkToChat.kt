/**
 * Tawk.to Android SDK — Jetpack Compose chat widget.
 *
 * Embeds the [Tawk.to](https://www.tawk.to/) chat in an Android app via WebView with
 * configurable options and a native-like experience (no copy, no zoom, theme-aware).
 *
 * @see TawkToChat
 * @see TawkToChatOptions
 */
package io.github.codewithtamim.tawktoandroid.sdk

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Configuration for [TawkToChat] behavior and appearance.
 *
 * Use [TawkToChatOptions] with [TawkToChat] to control copy behavior, loading UI,
 * link handling, and error/retry text. All parameters have defaults.
 *
 * @param disableCopy When `true` (default), prevents copying/selecting chat content;
 *   input fields remain editable. Set to `false` to allow selection/copy.
 * @param showLoadingIndicator When `true` (default), shows the top progress bar and
 *   center spinner while the chat page is loading.
 * @param openLinksExternally When `true` (default), links to other domains open in
 *   the system browser. When `false`, they open inside the WebView.
 * @param errorTitle Custom title for the error state; `null` uses the default "Couldn't load chat".
 * @param retryButtonText Label for the retry button (default: "Retry").
 * @param backgroundColor Background color behind the chat; `null` uses
 *   [MaterialTheme.colorScheme.surface].
 */
data class TawkToChatOptions(
    val disableCopy: Boolean = true,
    val showLoadingIndicator: Boolean = true,
    val openLinksExternally: Boolean = true,
    val errorTitle: String? = null,
    val retryButtonText: String = "Retry",
    val backgroundColor: ComposeColor? = null,
)

/**
 * Embeds the Tawk.to chat widget in a full-size Composable using a WebView.
 *
 * Load the [Direct Chat Link](https://help.tawk.to/article/how-to-add-the-tawkto-widget-to-your-mobile-application)
 * from your Tawk.to dashboard. The widget is displayed with a native-like experience:
 * no zoom, optional copy protection, and theme-aware background. Loading and error
 * states are handled with optional indicators and a retry action.
 *
 * @param chatUrl Direct chat link URL (e.g. `https://tawk.to/chat/YOUR_PROPERTY_ID/YOUR_WIDGET_ID`).
 * @param modifier [Modifier] for the root layout (e.g. padding, background).
 * @param options [TawkToChatOptions] for copy behavior, loading UI, links, and error/retry text.
 * @param onLoadFailed Callback when the page fails to load; receives the error message, or `null`.
 * @param onLoadFinished Callback when the chat page has finished loading successfully.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TawkToChat(
    chatUrl: String,
    modifier: Modifier = Modifier,
    options: TawkToChatOptions = TawkToChatOptions(),
    onLoadFailed: ((String?) -> Unit)? = null,
    onLoadFinished: (() -> Unit)? = null
) {
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastLoadedUrl by remember { mutableStateOf("") }
    val context = LocalContext.current
    val animatedProgress by animateFloatAsState(
        targetValue = loadProgress,
        animationSpec = tween(150),
        label = "progress"
    )
    val surfaceColor = options.backgroundColor ?: MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .background(surfaceColor)
    ) {
        AndroidView(
            factory = {
                val disableCopy = options.disableCopy
                val openExternally = options.openLinksExternally
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(Color.TRANSPARENT)
                    if (disableCopy) {
                        isLongClickable = false
                        setOnLongClickListener { true }
                    }
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                        setSupportZoom(false)
                        builtInZoomControls = false
                        displayZoomControls = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                    }
                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    }
                    webViewClient = object : WebViewClient() {
                        private val chatHost = Uri.parse(chatUrl).host

                        @RequiresApi(24)
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            if (!openExternally) return false
                            val url = request?.url ?: return false
                            return openExternalIfDifferentHost(url.toString(), view)
                        }

                        @Suppress("DEPRECATION")
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            url: String?
                        ): Boolean {
                            if (!openExternally) return false
                            return openExternalIfDifferentHost(url ?: return false, view)
                        }

                        private fun openExternalIfDifferentHost(
                            targetUrl: String,
                            view: WebView?
                        ): Boolean {
                            if (chatHost == null) return false
                            val targetHost = Uri.parse(targetUrl).host ?: return false
                            if (targetHost == chatHost) return false
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                )
                            } catch (_: Exception) { }
                            return true
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                            errorMessage = null
                            loadProgress = 0f
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            if (disableCopy) {
                                view?.evaluateJavascript(
                                    "(function(){ var s=document.createElement('style'); s.textContent='*:not(input):not(textarea):not([contenteditable]){-webkit-user-select:none;user-select:none;-webkit-touch-callout:none;}'; document.head.appendChild(s); })();",
                                    null
                                )
                            }
                            loadProgress = 100f
                            isLoading = false
                            onLoadFinished?.invoke()
                        }

                        @RequiresApi(23)
                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            if (request?.isForMainFrame == true) {
                                errorMessage = error?.description?.toString() ?: "Unknown error"
                                isLoading = false
                                onLoadFailed?.invoke(errorMessage)
                            }
                        }

                        @Suppress("DEPRECATION")
                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            errorMessage = description ?: "Unknown error"
                            isLoading = false
                            onLoadFailed?.invoke(errorMessage)
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            loadProgress = newProgress / 100f
                        }
                    }
                }
            },
            update = { webView ->
                if (chatUrl.isNotBlank() && chatUrl != lastLoadedUrl) {
                    lastLoadedUrl = chatUrl
                    errorMessage = null
                    isLoading = true
                    loadProgress = 0f
                    webView.loadUrl(chatUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = options.showLoadingIndicator && isLoading && errorMessage == null,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(80)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            }
        }

        AnimatedVisibility(
            visible = options.showLoadingIndicator && isLoading && errorMessage == null && loadProgress < 1f,
            enter = fadeIn(tween(200)) + scaleIn(
                initialScale = 0.92f,
                animationSpec = tween(200)
            ),
            exit = fadeOut(tween(150)) + scaleOut(
                targetScale = 0.92f,
                animationSpec = tween(150)
            ),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            errorMessage?.let { message ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Text(
                        text = options.errorTitle ?: "Couldn't load chat",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            lastLoadedUrl = ""
                            errorMessage = null
                            isLoading = true
                            loadProgress = 0f
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(options.retryButtonText)
                    }
                }
            }
        }
    }
}
