package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.URLEncoder

class BrowserViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    fun loadUrl(rawInput: String) {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return

        val resolvedUrl = resolveUrl(trimmed)
        val activeId = _uiState.value.activeTabId

        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == activeId) {
                    tab.copy(
                        url = resolvedUrl,
                        isLoading = true,
                        progress = 10
                    )
                } else tab
            }
            state.copy(
                tabs = updatedTabs,
                isTabSwitcherOpen = false,
                isMenuOpen = false
            )
        }
    }

    fun openNewTab(isIncognito: Boolean = false) {
        val newTab = BrowserTab(
            url = "https://www.google.com",
            title = if (isIncognito) "Stealth Tab" else "Google",
            isIncognito = isIncognito
        )
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + newTab,
                activeTabId = newTab.id,
                isTabSwitcherOpen = false,
                isMenuOpen = false
            )
        }
    }

    fun closeTab(tabId: String) {
        _uiState.update { state ->
            val remaining = state.tabs.filter { it.id != tabId }
            if (remaining.isEmpty()) {
                val fresh = BrowserTab()
                state.copy(tabs = listOf(fresh), activeTabId = fresh.id)
            } else {
                val newActiveId = if (state.activeTabId == tabId) {
                    remaining.last().id
                } else {
                    state.activeTabId
                }
                state.copy(tabs = remaining, activeTabId = newActiveId)
            }
        }
    }

    fun selectTab(tabId: String) {
        _uiState.update {
            it.copy(activeTabId = tabId, isTabSwitcherOpen = false, isMenuOpen = false)
        }
    }

    fun toggleTabSwitcher() {
        _uiState.update { it.copy(isTabSwitcherOpen = !it.isTabSwitcherOpen, isMenuOpen = false) }
    }

    fun closeTabSwitcher() {
        _uiState.update { it.copy(isTabSwitcherOpen = false) }
    }

    fun toggleMenu() {
        _uiState.update { it.copy(isMenuOpen = !it.isMenuOpen) }
    }

    fun closeMenu() {
        _uiState.update { it.copy(isMenuOpen = false) }
    }

    fun toggleDesktopSite() {
        val activeId = _uiState.value.activeTabId
        _uiState.update { state ->
            val updated = state.tabs.map { tab ->
                if (tab.id == activeId) tab.copy(isDesktopSite = !tab.isDesktopSite) else tab
            }
            state.copy(tabs = updated, isMenuOpen = false)
        }
    }

    fun toggleBookmark() {
        val tab = _uiState.value.activeTab
        val existing = _uiState.value.bookmarksList.find { it.url == tab.url }
        if (existing != null) {
            _uiState.update { state ->
                state.copy(
                    bookmarksList = state.bookmarksList.filter { it.id != existing.id },
                    snackBarMessage = "Bookmark removed",
                    isMenuOpen = false
                )
            }
        } else {
            val newBookmark = BookmarkItem(title = tab.title.ifBlank { tab.url }, url = tab.url)
            _uiState.update { state ->
                state.copy(
                    bookmarksList = listOf(newBookmark) + state.bookmarksList,
                    snackBarMessage = "Saved to Bookmarks",
                    isMenuOpen = false
                )
            }
        }
    }

    fun isCurrentPageBookmarked(): Boolean {
        val tab = _uiState.value.activeTab
        return _uiState.value.bookmarksList.any { it.url == tab.url }
    }

    fun deleteBookmark(id: String) {
        _uiState.update { state ->
            state.copy(bookmarksList = state.bookmarksList.filter { it.id != id })
        }
    }

    fun openBookmarks() {
        _uiState.update { it.copy(isBookmarksOpen = true, isMenuOpen = false, isTabSwitcherOpen = false) }
    }

    fun closeBookmarks() {
        _uiState.update { it.copy(isBookmarksOpen = false) }
    }

    fun openHistory() {
        _uiState.update { it.copy(isHistoryOpen = true, isMenuOpen = false, isTabSwitcherOpen = false) }
    }

    fun closeHistory() {
        _uiState.update { it.copy(isHistoryOpen = false) }
    }

    fun deleteHistoryItem(id: String) {
        _uiState.update { state ->
            state.copy(historyList = state.historyList.filter { it.id != id })
        }
    }

    fun clearAllHistory() {
        _uiState.update { state ->
            state.copy(historyList = emptyList(), snackBarMessage = "Browsing history cleared")
        }
    }

    fun triggerInstall() {
        _uiState.update {
            it.copy(
                isMenuOpen = false,
                snackBarMessage = "Infinity Browser is installed and ready on your device."
            )
        }
    }

    fun triggerCreateShortcut(context: Context) {
        val active = _uiState.value.activeTab
        val shortcutLabel = active.title.ifBlank { "Web Page" }
        try {
            val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse(active.url))
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                val pinShortcutInfo = ShortcutInfoCompat.Builder(context, "shortcut_${System.currentTimeMillis()}")
                    .setShortLabel(shortcutLabel.take(18))
                    .setLongLabel(shortcutLabel)
                    .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                    .setIntent(launchIntent)
                    .build()
                ShortcutManagerCompat.requestPinShortcut(context, pinShortcutInfo, null)
                _uiState.update {
                    it.copy(
                        isMenuOpen = false,
                        snackBarMessage = "Shortcut '$shortcutLabel' pinned to home screen."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isMenuOpen = false,
                        snackBarMessage = "Shortcut pinning not supported by current launcher."
                    )
                }
            }
        } catch (_: Exception) {
            _uiState.update {
                it.copy(
                    isMenuOpen = false,
                    snackBarMessage = "Unable to create shortcut for this page."
                )
            }
        }
    }

    fun onPageStarted(tabId: String, url: String?) {
        _uiState.update { state ->
            val updated = state.tabs.map { tab ->
                if (tab.id == tabId) {
                    tab.copy(
                        url = url ?: tab.url,
                        isLoading = true,
                        progress = 15
                    )
                } else tab
            }
            state.copy(tabs = updated)
        }
    }

    fun onProgressChanged(tabId: String, progress: Int) {
        _uiState.update { state ->
            val updated = state.tabs.map { tab ->
                if (tab.id == tabId) {
                    tab.copy(
                        progress = progress,
                        isLoading = progress < 100
                    )
                } else tab
            }
            state.copy(tabs = updated)
        }
    }

    fun onPageFinished(tabId: String, url: String?, title: String?, canBack: Boolean, canForward: Boolean) {
        _uiState.update { state ->
            val updated = state.tabs.map { tab ->
                if (tab.id == tabId) {
                    val resolvedUrl = url ?: tab.url
                    val resolvedTitle = title ?: tab.title
                    // Save to history if not stealth tab and valid web url
                    if (!tab.isIncognito && !resolvedUrl.startsWith("about:") && resolvedUrl.isNotBlank()) {
                        addHistoryEntry(resolvedTitle, resolvedUrl)
                    }
                    tab.copy(
                        url = resolvedUrl,
                        title = resolvedTitle,
                        isLoading = false,
                        progress = 100,
                        canGoBack = canBack,
                        canGoForward = canForward
                    )
                } else tab
            }
            state.copy(tabs = updated)
        }
    }

    fun dismissSnackBar() {
        _uiState.update { it.copy(snackBarMessage = null) }
    }

    private fun addHistoryEntry(title: String, url: String) {
        val entry = HistoryItem(title = title.ifBlank { url }, url = url)
        val currentHistory = _uiState.value.historyList
        // Avoid duplicate identical entry at top
        if (currentHistory.firstOrNull()?.url != url) {
            _uiState.update { it.copy(historyList = (listOf(entry) + currentHistory).take(100)) }
        }
    }

    private fun resolveUrl(input: String): String {
        return when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.startsWith("about:") -> input
            isDomainOrUrl(input) -> "https://$input"
            else -> {
                val encodedQuery = try {
                    URLEncoder.encode(input, "UTF-8")
                } catch (e: Exception) {
                    input
                }
                "https://www.google.com/search?q=$encodedQuery"
            }
        }
    }

    private fun isDomainOrUrl(query: String): Boolean {
        if (query.contains(" ")) return false
        val parts = query.split("/")
        val host = parts[0]
        return host.contains(".") && host.length >= 3 && !host.endsWith(".") && !host.startsWith(".")
    }
}
