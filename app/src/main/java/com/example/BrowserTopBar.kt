package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.InfinityPurpleLight
import com.example.ui.theme.RamAmber
import com.example.ui.theme.RamGreen

@Composable
fun BrowserTopBar(
    activeTab: BrowserTab,
    tabCount: Int,
    isMenuOpen: Boolean,
    isBookmarked: Boolean,
    onNavigate: (String) -> Unit,
    onHome: () -> Unit,
    onToggleTabSwitcher: () -> Unit,
    onToggleMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit,
    onToggleBookmark: () -> Unit,
    onNewTab: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onToggleDesktopSite: () -> Unit,
    onCreateShortcut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var urlText by remember { mutableStateOf(activeTab.url) }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(activeTab.url) {
        if (!isFocused) {
            urlText = activeTab.url
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home button set to google.com
            IconButton(
                onClick = onHome,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("top_bar_home_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home (google.com)",
                    tint = if (activeTab.url == "https://www.google.com") InfinityPurpleLight else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // URL / Google Search Box
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isFocused) CyanAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("top_bar_url_box")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (activeTab.url.startsWith("https://")) Icons.Default.Lock else Icons.Default.Search,
                        contentDescription = null,
                        tint = if (activeTab.url.startsWith("https://")) RamGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    BasicTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { isFocused = it.isFocused }
                            .testTag("url_text_input"),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Default
                        ),
                        cursorBrush = SolidColor(CyanAccent),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                if (urlText.isNotBlank()) {
                                    onNavigate(urlText)
                                }
                            }
                        ),
                        decorationBox = { innerTextField ->
                            if (urlText.isEmpty() && !isFocused) {
                                Text(
                                    text = "Search with Google or enter URL",
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 13.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (urlText.isNotEmpty() && isFocused) {
                        IconButton(
                            onClick = { urlText = "" },
                            modifier = Modifier.size(24.dp).testTag("clear_url_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Tab button with number of tabs: [ N ]
            Surface(
                onClick = onToggleTabSwitcher,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CyanAccent.copy(alpha = 0.7f)),
                modifier = Modifier
                    .size(36.dp)
                    .testTag("tab_count_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$tabCount",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Infinity button: [ ∞ ] - Light Purple Color
            Box {
                IconButton(
                    onClick = onToggleMenu,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("infinity_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AllInclusive,
                        contentDescription = "Options menu",
                        tint = InfinityPurpleLight,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Dropdown Menu
                DropdownMenu(
                    expanded = isMenuOpen,
                    onDismissRequest = onDismissMenu,
                    modifier = Modifier
                        .width(260.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .testTag("infinity_dropdown_menu")
                ) {
                    // Top Row: Back, Forward, Bookmark Star, Refresh
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            enabled = activeTab.canGoBack,
                            modifier = Modifier.size(36.dp).testTag("menu_action_back")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (activeTab.canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onForward,
                            enabled = activeTab.canGoForward,
                            modifier = Modifier.size(36.dp).testTag("menu_action_forward")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Forward",
                                tint = if (activeTab.canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier.size(36.dp).testTag("menu_action_bookmark")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) RamAmber else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onReload,
                            modifier = Modifier.size(36.dp).testTag("menu_action_refresh")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Option: Home (Google)
                    DropdownMenuItem(
                        text = { Text("Home (google.com)", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(Icons.Default.Home, contentDescription = null, tint = InfinityPurpleLight, modifier = Modifier.size(20.dp))
                        },
                        onClick = onHome,
                        modifier = Modifier.testTag("menu_item_home")
                    )

                    // Option: new tab
                    DropdownMenuItem(
                        text = { Text("New tab", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        onClick = onNewTab,
                        modifier = Modifier.testTag("menu_item_new_tab")
                    )

                    // Option: history
                    DropdownMenuItem(
                        text = { Text("History", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        onClick = onOpenHistory,
                        modifier = Modifier.testTag("menu_item_history")
                    )

                    // Option: bookmarks
                    DropdownMenuItem(
                        text = { Text("Bookmarks", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        onClick = onOpenBookmarks,
                        modifier = Modifier.testTag("menu_item_bookmarks")
                    )

                    // Option: desktopsite with a checkbox
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Desktop site", style = MaterialTheme.typography.bodyMedium)
                                Checkbox(
                                    checked = activeTab.isDesktopSite,
                                    onCheckedChange = { onToggleDesktopSite() },
                                    colors = CheckboxDefaults.colors(checkedColor = InfinityPurpleLight),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.DesktopWindows, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        onClick = onToggleDesktopSite,
                        modifier = Modifier.testTag("menu_item_desktop_site")
                    )

                    // Option: create shortcut
                    DropdownMenuItem(
                        text = { Text("Create shortcut", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(Icons.Default.AddBox, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        onClick = onCreateShortcut,
                        modifier = Modifier.testTag("menu_item_create_shortcut")
                    )
                }
            }
        }
    }
}
