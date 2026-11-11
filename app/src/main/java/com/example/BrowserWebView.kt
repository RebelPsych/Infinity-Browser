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
import android.webkit.WebResourceResponse
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

// Windows 11 Chrome Desktop User Agent - universally recognized by Google AI Studio, Google Docs, and desktop sites
private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    tab: BrowserTab,
    controller: BrowserController,
    onPageStarted: (String?) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onPageFinished: (String?, String?, Boolean, Boolean) -> Unit,
    onPageError: (Int, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val defaultMobileUserAgent = remember { WebSettings.getDefaultUserAgent(context) }

    val webView = remember(tab.id) {
        createConfiguredWebView(
            context = context,
            tab = tab,
            defaultMobileUserAgent = defaultMobileUserAgent,
            onPageStarted = onPageStarted,
            onProgressChanged = onProgressChanged,
            onPageFinished = onPageFinished,
            onPageError = onPageError
        ).also {
            controller.webView = it
        }
    }

    // React to Desktop Site toggle with viewport scaling and user-agent swap
    LaunchedEffect(tab.isDesktopSite) {
        val targetAgent = if (tab.isDesktopSite) DESKTOP_USER_AGENT else defaultMobileUserAgent
        if (webView.settings.userAgentString != targetAgent) {
            webView.settings.userAgentString = targetAgent
            webView.settings.useWideViewPort = true
            webView.settings.loadWithOverviewMode = true
            if (tab.isDesktopSite) {
                webView.setInitialScale(100)
            } else {
                webView.setInitialScale(0)
            }
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
    defaultMobileUserAgent: String,
    onPageStarted: (String?) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onPageFinished: (String?, String?, Boolean, Boolean) -> Unit,
    onPageError: (Int, String?) -> Unit
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Dark background prevents white flash during page load
        setBackgroundColor(Color.parseColor("#070A0F"))

        // Scrollbar configuration
        isVerticalScrollBarEnabled = true
        isHorizontalScrollBarEnabled = false
        scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY

        // Cookies setup: Google AI Studio and modern auth strictly require third-party cookies
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(this, true)

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(false)
            allowFileAccess = true
            allowContentAccess = true
            setGeolocationEnabled(false)
            mediaPlaybackRequiresUserGesture = true
            cacheMode = WebSettings.LOAD_DEFAULT

            // Viewport & zoom for desktop site compatibility
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false

            userAgentString = if (tab.isDesktopSite) DESKTOP_USER_AGENT else defaultMobileUserAgent
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

        if (tab.isDesktopSite) {
            setInitialScale(100)
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

                // Inject purple scrollbar and dark mode preference CSS
                val isDesktop = tab.isDesktopSite
                view?.evaluateJavascript(
                    """
                    (function() {
                        // Light purple scrollbar styling matching infinity symbol (#D8B4FE)
                        if (!document.getElementById('__infinity_scrollbar_style')) {
                            var sb = document.createElement('style');
                            sb.id = '__infinity_scrollbar_style';
                            sb.innerHTML = '::-webkit-scrollbar { width: 8px !important; height: 8px !important; } ::-webkit-scrollbar-track { background: transparent !important; } ::-webkit-scrollbar-thumb { background: #D8B4FE !important; border-radius: 4px !important; } ::-webkit-scrollbar-thumb:hover { background: #C084FC !important; } * { scrollbar-color: #D8B4FE transparent !important; scrollbar-width: thin !important; }';
                            document.head && document.head.appendChild(sb);
                        }

                        // Dark mode stylesheet injection
                        if (!document.getElementById('__infinity_dark_style')) {
                            var s = document.createElement('style');
                            s.id = '__infinity_dark_style';
                            s.innerHTML = '@media (prefers-color-scheme: light) { html { color-scheme: dark; } }';
                            document.head && document.head.appendChild(s);
                        }

                        // Desktop site viewport width reinforcement for Google AI Studio & desktop apps
                        if ($isDesktop) {
                            var vp = document.querySelector('meta[name="viewport"]');
                            if (vp) {
                                vp.setAttribute('content', 'width=1280, initial-scale=0.8, user-scalable=yes');
                            } else {
                                var m = document.createElement('meta');
                                m.name = 'viewport';
                                m.content = 'width=1280, initial-scale=0.8, user-scalable=yes';
                                document.head && document.head.appendChild(m);
                            }
                        }
                    })();
                    """.trimIndent(),
                    null
                )
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    onPageError(error?.errorCode ?: -1, error?.description?.toString())
                }
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (request?.isForMainFrame == true && (errorResponse?.statusCode ?: 200) >= 400) {
                    onPageError(errorResponse?.statusCode ?: 404, errorResponse?.reasonPhrase)
                }
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
