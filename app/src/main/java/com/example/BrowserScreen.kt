package com.example

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.RamAmber

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val controller = remember { BrowserController() }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val activeTab = uiState.activeTab

    // Handle back button hierarchically
    BackHandler(enabled = true) {
        when {
            uiState.isTabSwitcherOpen -> viewModel.closeTabSwitcher()
            uiState.isHistoryOpen -> viewModel.closeHistory()
            uiState.isBookmarksOpen -> viewModel.closeBookmarks()
            uiState.isMenuOpen -> viewModel.closeMenu()
            activeTab.canGoBack -> controller.goBack()
            uiState.tabs.size > 1 -> viewModel.closeTab(activeTab.id)
            else -> {
                // Exit or minimize app
                (context as? android.app.Activity)?.finish()
            }
        }
    }

    // Auto-dismiss snackbar
    LaunchedEffect(uiState.snackBarMessage) {
        uiState.snackBarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackBar()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (activeTab.isIncognito) Color(0xFF090D14) else MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Stripped down Top Bar: URL box (search with google / enter url) + tab count [N] + infinity [∞]
            BrowserTopBar(
                activeTab = activeTab,
                tabCount = uiState.tabs.size,
                isMenuOpen = uiState.isMenuOpen,
                isBookmarked = viewModel.isCurrentPageBookmarked(),
                onNavigate = { url -> viewModel.loadUrl(url) },
                onToggleTabSwitcher = { viewModel.toggleTabSwitcher() },
                onToggleMenu = { viewModel.toggleMenu() },
                onDismissMenu = { viewModel.closeMenu() },
                onBack = { controller.goBack() },
                onForward = { controller.goForward() },
                onReload = { controller.reload() },
                onToggleBookmark = { viewModel.toggleBookmark() },
                onNewTab = { viewModel.openNewTab(isIncognito = false) },
                onNewStealthTab = { viewModel.openNewTab(isIncognito = true) },
                onOpenHistory = { viewModel.openHistory() },
                onOpenBookmarks = { viewModel.openBookmarks() },
                onToggleDesktopSite = { viewModel.toggleDesktopSite() },
                onInstall = { viewModel.triggerInstall() },
                onCreateShortcut = { viewModel.triggerCreateShortcut(context) }
            )

            // Top Hairline Loading Progress Bar
            if (activeTab.isLoading) {
                LinearProgressIndicator(
                    progress = { activeTab.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .testTag("top_progress_bar"),
                    color = if (activeTab.isIncognito) RamAmber else CyanAccent,
                    trackColor = Color.Transparent
                )
            }

            // Main Web View Area
            BrowserWebView(
                tab = activeTab,
                controller = controller,
                onPageStarted = { url -> viewModel.onPageStarted(activeTab.id, url) },
                onProgressChanged = { progress -> viewModel.onProgressChanged(activeTab.id, progress) },
                onPageFinished = { url, title, canBack, canForward ->
                    viewModel.onPageFinished(activeTab.id, url, title, canBack, canForward)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        // Tab Previews Grid Overlay
        AnimatedVisibility(
            visible = uiState.isTabSwitcherOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TabSwitcherScreen(
                tabs = uiState.tabs,
                activeTabId = uiState.activeTabId,
                onSelectTab = { viewModel.selectTab(it) },
                onCloseTab = { viewModel.closeTab(it) },
                onNewTab = { viewModel.openNewTab(isIncognito = false) },
                onNewStealthTab = { viewModel.openNewTab(isIncognito = true) },
                onCloseSwitcher = { viewModel.closeTabSwitcher() }
            )
        }

        // History Full-Screen Overlay
        AnimatedVisibility(
            visible = uiState.isHistoryOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            HistoryScreen(
                historyList = uiState.historyList,
                onSelectUrl = {
                    viewModel.loadUrl(it)
                    viewModel.closeHistory()
                },
                onDeleteItem = { viewModel.deleteHistoryItem(it) },
                onClearAll = { viewModel.clearAllHistory() },
                onClose = { viewModel.closeHistory() }
            )
        }

        // Bookmarks Full-Screen Overlay
        AnimatedVisibility(
            visible = uiState.isBookmarksOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BookmarksScreen(
                bookmarksList = uiState.bookmarksList,
                onSelectUrl = {
                    viewModel.loadUrl(it)
                    viewModel.closeBookmarks()
                },
                onDeleteBookmark = { viewModel.deleteBookmark(it) },
                onClose = { viewModel.closeBookmarks() }
            )
        }

        // Floating Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) { data ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (activeTab.isIncognito) RamAmber.copy(alpha = 0.5f) else CyanAccent.copy(alpha = 0.5f)
                ),
                shadowElevation = 6.dp
            ) {
                Text(
                    text = data.visuals.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}
