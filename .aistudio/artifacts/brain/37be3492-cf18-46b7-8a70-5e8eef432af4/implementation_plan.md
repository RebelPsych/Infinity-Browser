# NanoBrowser — Stripped Minimal UI & Tab Management System

A radical redesign that strips away all bottom navigation pills, floating overlays, and home clutter. The entire interface is streamlined into a single, compact top bar with an omnibar (URL / Google search), a tab count button leading to a thumbnail preview grid, and an infinity symbol (`∞`) dropdown menu.

## User Review & Critical Decisions

> [!IMPORTANT]
> Based on your prompt and Phase 1 answers, here is the exact interface specification:

- **Top Bar**: A clean, single-row top bar containing:
  1. **URL / Search Box**: Direct navigation if a URL is entered, or instant Google search if a query is typed.
  2. **Tabs Button (`[ N ]`)**: Shows the count of open tabs; tapping opens a visual grid of active tab cards with close buttons.
  3. **Infinity Button (`∞`)**: Opens the dedicated quick-actions and tools dropdown menu.
- **Infinity Dropdown Layout**:
  - **Top Action Row**: Back arrow, Forward arrow, Bookmark star (toggles saved status), and Refresh button.
  - **Menu List**:
    - **New Tab**: Opens a fresh tab and switches to it.
    - **New Stealth Tab (Incognito)**: Opens an isolated private tab in dark stealth styling with no history or cookie retention.
    - **History**: Opens a full-screen view of visited pages with search and clear actions.
    - **Bookmarks**: Opens a full-screen list of starred bookmarks with search and deletion.
    - **Desktop Site**: Toggle checkbox that switches the WebView user agent between Mobile and Desktop mode.
    - **Install**: In-app web app installation prompt / guidance.
    - **Create Shortcut**: Pins a home screen shortcut for the active page via Android `ShortcutManager`.
- **Active Tab Previews**: Grid of thumbnail cards showing tab titles, URLs, and close buttons (`✕`), plus a "+" button to add tabs.

---

### 1. Overview & Core Concept

- **What It Does**: Replaces the entire previous layout with a single-header web browser. When opening the app, you see only the top bar and the web viewport.
- **Target Audience / Persona**: Users wanting a distraction-free, hyper-clean browsing experience with standard power-user tools (tabs, incognito, desktop view, home shortcuts) concealed neatly behind the infinity menu.
- **Key Value**: 100% of the viewport is dedicated to the webpage, while all browser controls remain exactly 1 tap away in the top bar.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Omnibar Input**: Tap the top URL bar to type an address (e.g. `wikipedia.org`) or search terms; pressing Enter immediately loads Google search or navigates to the page.
  2. **Tab Switcher View**: Tap the tab badge button (`[ 1 ]`, `[ 2 ]`, etc.) to open the grid preview. Tap any card to switch to it, tap `✕` to close a tab, or tap `+ New Tab` to spawn a new one.
  3. **Infinity Menu (`∞`)**: Tap `∞` to open an elevated dropdown:
     - Top row gives instant access to `[←] [→] [★] [↻]`.
     - Below, select `New Tab`, `New Stealth Tab`, `History`, `Bookmarks`, `Desktop Site [✓]`, `Install`, or `Create Shortcut`.
  4. **Desktop Site**: Check the box to immediately re-render the page in full desktop desktop mode (`Linux x86_64` user-agent).
  5. **History & Bookmarks**: Full-screen overlay screens with instant search and clear actions, styled cleanly with edge-to-edge support.

- **Visual Identity**:
  - *Palette*: Sleek Charcoal & Cyan (`#0F172A` / `#00E5FF`). Stealth mode switches to Obsidian & Amber (`#05070A` / `#F59E0B` with incognito icon).
  - *Top Bar*: Slim 56.dp height respecting `statusBarsPadding()`.
  - *Dropdown*: Elevated surface with subtle glass border and clean typography.

---

### 3. Key Product Decisions & Architecture

- **Decision 1: Multi-Tab State Model (`BrowserTab`)**
  - *Chosen Approach*: Manage an active list of `BrowserTab` objects in `BrowserViewModel` (`id`, `title`, `url`, `isIncognito`, `isDesktopSite`, `canGoBack`, `canGoForward`).
  - *Why*: Enables seamless tab switching, stealth tabs, and responsive thumbnail previews without runaway process memory.

- **Decision 2: Google Search Default & Auto-URL Detection**
  - *Chosen Approach*: If user enters valid URL scheme or domain (`site.com`), open directly; otherwise query Google (`https://www.google.com/search?q=...`).

- **Decision 3: Android Dynamic Shortcuts for "Create Shortcut"**
  - *Chosen Approach*: Use `ShortcutManagerCompat` / `requestPinShortcut` with intent to open the active page directly from the Android launcher.

---

### 4. Technical Architecture & Component Hierarchy

```
┌─────────────────────────────────────────────────────────────┐
│                          Top Bar                            │
│  ┌───────────────────────┐  ┌─────────────┐  ┌───────────┐  │
│  │   Omnibar (URL/Google)│  │ Tabs: [ N ] │  │   [ ∞ ]   │  │
│  └───────────────────────┘  └─────────────┘  └─────┬─────┘  │
└────────────────────────────────────────────────────┼────────┘
                                                     │
                                           ┌─────────▼────────┐
                                           │ Infinity Dropdown│
                                           │ [←] [→] [★] [↻]  │
                                           ├──────────────────┤
                                           │ • New Tab        │
                                           │ • New Stealth Tab│
                                           │ • History        │
                                           │ • Bookmarks      │
                                           │ • Desktop Site ☑ │
                                           │ • Install        │
                                           │ • Create Shortcut│
                                           └──────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                 Viewport / Sub-Screens                      │
│                                                             │
│   Active Tab WebView   OR   Tab Preview Grid                │
│                        OR   History Screen                  │
│                        OR   Bookmarks Screen                │
└─────────────────────────────────────────────────────────────┘
```

- **Data Models**:
  - `BrowserTab`: Holds per-tab state (`id`, `url`, `title`, `isIncognito`, `isDesktopMode`, `canGoBack`, `canGoForward`).
  - `HistoryItem` & `BookmarkItem`: Stored locally for instant recall.
- **Verification**: Local Robolectric test execution and `compile_applet`.
