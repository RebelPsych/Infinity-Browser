package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `multi tab management test`() {
        val viewModel = BrowserViewModel()
        assertEquals(1, viewModel.uiState.value.tabs.size)

        // Open New Tab
        viewModel.openNewTab()
        assertEquals(2, viewModel.uiState.value.tabs.size)

        // Close Tab
        val secondTabId = viewModel.uiState.value.activeTabId
        viewModel.closeTab(secondTabId)
        assertEquals(1, viewModel.uiState.value.tabs.size)
    }

    @Test
    fun `view model url and google search resolution test`() {
        val viewModel = BrowserViewModel()

        // Direct URL load
        viewModel.loadUrl("https://example.com")
        assertEquals("https://example.com", viewModel.uiState.value.activeTab.url)

        // Domain auto-prefixing
        viewModel.loadUrl("wikipedia.org")
        assertEquals("https://wikipedia.org", viewModel.uiState.value.activeTab.url)

        // Google search routing for search queries
        viewModel.loadUrl("android speed test")
        assertTrue(viewModel.uiState.value.activeTab.url.contains("google.com/search?q=") == true)

        // Toggle Desktop site
        assertFalse(viewModel.uiState.value.activeTab.isDesktopSite)
        viewModel.toggleDesktopSite()
        assertTrue(viewModel.uiState.value.activeTab.isDesktopSite)

        // Bookmark toggle
        assertFalse(viewModel.isCurrentPageBookmarked())
        viewModel.toggleBookmark()
        assertTrue(viewModel.isCurrentPageBookmarked())
    }
}
