package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

class BrowserController {
    var webView: WebView? = null

    fun goBack(): Boolean {
        return if (webView?.canGoBack() == true) {
            webView?.goBack()
            true
        } else {
            false
        }
    }

    fun goForward(): Boolean {
        return if (webView?.canGoForward() == true) {
            webView?.goForward()
            true
        } else {
            false
        }
    }

    fun reload() {
        webView?.reload()
    }

    fun stop() {
        webView?.stopLoading()
    }

    fun loadUrl(url: String) {
        webView?.loadUrl(url)
    }

    fun purgeCache() {
        webView?.clearCache(false)
        webView?.freeMemory()
    }
}

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    tab: BrowserTab,
    controller: BrowserController,
    onPageStarted: (String?) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onPageFinished: (String?, String?, Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val webView = remember(tab.id) {
        createConfiguredWebView(
            context = context,
            tab = tab,
            onPageStarted = onPageStarted,
            onProgressChanged = onProgressChanged,
            onPageFinished = onPageFinished
        ).also {
            controller.webView = it
        }
    }

    // React to Desktop Site toggle
    LaunchedEffect(tab.isDesktopSite) {
        val currentAgent = webView.settings.userAgentString
        val targetAgent = if (tab.isDesktopSite) DESKTOP_USER_AGENT else null
        if (currentAgent != targetAgent) {
            webView.settings.userAgentString = targetAgent
            webView.settings.useWideViewPort = tab.isDesktopSite
            webView.settings.loadWithOverviewMode = tab.isDesktopSite
            webView.reload()
        }
    }

    // Load new URL when tab.url changes
    LaunchedEffect(tab.url) {
        if (webView.url != tab.url && tab.url.isNotBlank()) {
            webView.loadUrl(tab.url)
        }
    }

    // Lifecycle handling: pause timers & JS execution when backgrounded to save RAM & battery
    DisposableEffect(lifecycleOwner, webView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    webView.onPause()
                    webView.pauseTimers()
                }
                Lifecycle.Event.ON_RESUME -> {
                    webView.onResume()
                    webView.resumeTimers()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    controller.webView = null
                    webView.stopLoading()
                    webView.removeAllViews()
                    webView.destroy()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}

@Suppress("DEPRECATION")
@SuppressLint("SetJavaScriptEnabled")
private fun createConfiguredWebView(
    context: Context,
    tab: BrowserTab,
    onPageStarted: (String?) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onPageFinished: (String?, String?, Boolean, Boolean) -> Unit
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Dark background prevents white flash during page load
        setBackgroundColor(Color.parseColor("#070A0F"))

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = !tab.isIncognito
            databaseEnabled = false
            setGeolocationEnabled(false)
            mediaPlaybackRequiresUserGesture = true
            cacheMode = if (tab.isIncognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
            useWideViewPort = tab.isDesktopSite
            loadWithOverviewMode = tab.isDesktopSite
            userAgentString = if (tab.isDesktopSite) DESKTOP_USER_AGENT else null
            builtInZoomControls = true
            displayZoomControls = false
            saveFormData = false
            savePassword = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

            // Force dark mode on all web pages natively
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                forceDark = WebSettings.FORCE_DARK_ON
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isAlgorithmicDarkeningAllowed = true
            }
        }

        // Stealth tab cookie isolation
        if (tab.isIncognito) {
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(false)
        }

        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onPageStarted(url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val canBack = view?.canGoBack() ?: false
                val canForward = view?.canGoForward() ?: false
                val title = view?.title
                onPageFinished(url, title, canBack, canForward)

                // Inject CSS preference for dark mode
                view?.evaluateJavascript(
                    """
                    (function() {
                        if (!document.getElementById('__infinity_dark_style')) {
                            var s = document.createElement('style');
                            s.id = '__infinity_dark_style';
                            s.innerHTML = '@media (prefers-color-scheme: light) { html { color-scheme: dark; } }';
                            document.head && document.head.appendChild(s);
                        }
                    })();
                    """.trimIndent(),
                    null
                )
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val reqUri = request?.url ?: return false
                val scheme = reqUri.scheme?.lowercase() ?: return false

                return when (scheme) {
                    "http", "https", "about" -> false
                    else -> {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, reqUri).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                        true
                    }
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onProgressChanged(newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
            }
        }
    }
}
