# 📊 StockScan — NSE/BSE Stock Screener (Android WebView)

A full-featured stock screener Android app built with WebView.
All 50+ filters from screener.in, tap-to-configure, no typing needed.

---

## 🚀 How to Build & Install

### Prerequisites
- **Android Studio Hedgehog** (2023.1.1) or newer → https://developer.android.com/studio
- **JDK 17** (bundled with Android Studio)
- Android device or emulator running **Android 7.0+** (API 24+)

---

### Step 1 — Open the project
1. Launch Android Studio
2. Click **File → Open**
3. Select the `StockScan/` folder
4. Wait for Gradle sync to complete (~2 min first time)

---

### Step 2 — Add fonts (required)
The app uses **Syne** and **JetBrains Mono** fonts.

1. In Android Studio, go to **res/font/** folder
2. Right-click → **New → Font resource file** (or download manually)

**Download links:**
- Syne Bold: https://fonts.google.com/specimen/Syne → download → put `syne_bold.ttf` in `res/font/`
- JetBrains Mono: https://fonts.google.com/specimen/JetBrains+Mono → download → put `jetbrains_mono.ttf` in `res/font/`

> **Quick fix if you skip fonts:** Open `activity_splash.xml` and remove the
> `android:fontFamily` lines — the app will use the system default font.

---

### Step 3 — Run on emulator
1. Click **Device Manager** (right panel) → **Create Virtual Device**
2. Choose **Pixel 7** → **API 34** → Finish
3. Press ▶️ **Run** (Shift+F10)

### Step 3b — Run on real device
1. Enable **Developer Options** on your phone:
   - Settings → About Phone → tap *Build Number* 7 times
2. Enable **USB Debugging** in Developer Options
3. Connect phone via USB → Allow debugging
4. Select your device in Android Studio → Press ▶️ Run

---

### Step 4 — Build APK (for sharing/installing)
1. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. APK will be at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
3. Copy to phone → install (allow "Install unknown apps" in settings)

---

## 📁 Project Structure

```
StockScan/
├── app/
│   ├── src/main/
│   │   ├── java/com/stockscan/
│   │   │   ├── SplashActivity.kt      ← Animated splash screen
│   │   │   └── MainActivity.kt        ← WebView host + JS bridge
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_splash.xml
│   │   │   │   └── activity_main.xml
│   │   │   ├── values/
│   │   │   │   ├── colors.xml
│   │   │   │   ├── strings.xml
│   │   │   │   └── themes.xml
│   │   │   ├── anim/
│   │   │   │   ├── fade_in.xml
│   │   │   │   ├── fade_out.xml
│   │   │   │   └── slide_up.xml
│   │   │   ├── drawable/              ← Icons, glows, dots
│   │   │   ├── font/                  ← Add fonts here
│   │   │   └── xml/
│   │   │       └── network_security_config.xml
│   │   ├── assets/
│   │   │   └── index.html             ← THE FULL SCREENER UI
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## ⚡ JavaScript ↔ Android Bridge

The app exposes native Android functions to the HTML/JS via `AndroidBridge`:

```javascript
// Show a native Android toast
AndroidBridge.showToast("Filter applied!");

// Share a stock natively
AndroidBridge.shareStock("RELIANCE", "2,924");

// Check network connectivity
const online = AndroidBridge.isNetworkAvailable();

// Get device info
const info = JSON.parse(AndroidBridge.getDeviceInfo());
```

To add more bridge functions, add `@JavascriptInterface` methods in
`MainActivity.kt → AndroidBridge` inner class.

---

## 🔌 Adding Live Data (Next Steps)

### Option A — Screener.in API
```kotlin
// In MainActivity.kt, call JS after loading live data:
webView.evaluateJavascript("updateStocks(${jsonData})", null)
```

### Option B — Yahoo Finance (free, unofficial)
```
GET https://query1.finance.yahoo.com/v8/finance/chart/RELIANCE.NS
```

### Option C — NSE India (official, no API key needed)
```
GET https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%2050
```
Add cookie handling in OkHttp for NSE requests.

---

## 🎨 Customisation

| What | Where |
|------|-------|
| Add/edit filters | `assets/index.html` → `allFilters` object |
| Change colours | `res/values/colors.xml` + CSS variables in HTML |
| Add new screens | Create new `.html` files in `assets/`, navigate with JS |
| Change app name | `res/values/strings.xml` → `app_name` |
| App icon | Replace `res/drawable/ic_launcher_foreground.xml` |

---

## 📱 Tested On
- Android 14 (API 34) — Pixel 7 emulator
- Android 12 (API 31) — Samsung Galaxy S21
- Android 10 (API 29) — OnePlus 7T

---

## 🛠️ Troubleshooting

**Gradle sync fails**
→ File → Invalidate Caches → Restart

**Blank white screen on launch**
→ Check `assets/index.html` exists. Run: `Build → Clean Project`

**Fonts not found**
→ Remove `android:fontFamily` lines from `activity_splash.xml`

**JS bridge not working**
→ Make sure `javaScriptEnabled = true` in `MainActivity.kt` (already set)

---

Built with ❤️ for Indian retail investors.
