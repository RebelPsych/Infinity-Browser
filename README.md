# Infinity Browser ♾️

An ultra-lean, hyper-fast, stripped-down Android web browser engineered strictly for speed and minimal RAM footprint.

## ✨ Key Highlights
- **Single-Header Minimal UI**: Zero visual bloat. The screen is 100% focused on web content with a sleek omnibar (Google Search & direct URL navigation), active tabs counter `[ N ]`, and infinity dropdown button `[ ∞ ]`.
- **Default Dark Mode**: All web pages and browser views render in an eye-friendly, power-efficient dark aesthetic with native force-dark engine support and zero white-flash on page load.
- **Visual Tab Previews**: Switch tabs via a responsive 2-column card grid showing thumbnails, site domains, and one-tap close buttons (`✕`).
- **Stealth Mode (Incognito)**: Isolated private browsing sessions with disabled cookies, no browsing history logging, and amber stealth accents.
- **Infinity (`∞`) Quick Dropdown**:
  - Top Action Row: Back (`←`), Forward (`→`), Bookmark Star (`★`), Reload (`↻`).
  - Tools: New Tab, New Stealth Tab, History, Bookmarks, Desktop Site Toggle (`☑`), Install App, Create Home Screen Shortcut.
- **Aggressive RAM Management**: Pauses timers on backgrounding, automatically trims cache on low memory signals, and prevents background sensor/telemetry drain.

## 📦 APK Download
The pre-compiled Android APK is available in the [`test/`](./test) folder:
- **`test/InfinityBrowser.apk`** (Ready for sideloading and GitHub releases)

## 🛠️ Building From Source
```bash
# Clone the repository
git clone https://github.com/your-username/infinity-browser.git
cd infinity-browser

# Build debug APK
gradle assembleDebug
```
The compiled APK will be at `app/build/outputs/apk/debug/app-debug.apk` and copied to `test/InfinityBrowser.apk`.
