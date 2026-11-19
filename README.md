# Auto Check-In for Creality

[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![WorkManager](https://img.shields.io/badge/WorkManager-24h%20Periodic-FF6F00)](https://developer.android.com/topic/libraries/architecture/workmanager)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A production-ready native Android application built with **Kotlin** and **Jetpack Compose (Material 3)** that automates daily check-ins on [Creality Cloud](https://www.crealitycloud.com/check-in).

> ⚠️ **Disclaimer:** This is an independent, unofficial community utility. It is **not** affiliated with, endorsed by, or sponsored by Shenzhen Creality 3D Technology Co., Ltd. or Creality Cloud. All product names, logos, and brands are property of their respective owners.

---

## 🌟 Highlights & Features

- 🔥 **Streak & Rewards Tracker**: Live dashboard showing your consecutive streak days, today's check-in status badge (`Checked In` / `Pending` / `Session Expired`), and points earned.
- ⏰ **Automated 24-Hour Background Execution**: Integrated with Android `WorkManager` and `CoroutineWorker`. Runs autonomously every 24 hours with network-connectivity constraints.
- 🌐 **In-App WebView Login (Zero-Effort Cookie Capture)**: Built-in WebView automatically extracts your `model_token` and `model_user_id` upon login, saving them directly into encrypted local storage without manual copy-pasting.
- ✏️ **Manual Credential Override**: Full visibility into your session. Inspect token status or paste raw cookie strings manually anytime.
- 📋 **Live Response Payload Inspector**: Monospace viewer displaying formatted API JSON responses with one-tap clipboard copy for troubleshooting.
- 🛡️ **Privacy by Design**: Zero cloud tracking or analytics. All tokens, cookies, and device identifiers remain exclusively on your device.

---

## 📱 User Interface & Flow

```mermaid
flowchart LR
    A[Dashboard Screen] -->|1. Tap 'Web Login'| B[In-App WebView]
    B -->|2. Log into Creality| C{CookieManager}
    C -->|Auto-Captures model_token & UID| D[Jetpack DataStore]
    D -->|Populates Credentials| A
    A -->|3. Enable 24h Switch| E[WorkManager Scheduler]
    E -->|Background 24h Trigger| F[CheckInWorker]
    F -->|HTTPS POST| G[Creality Cloud API]
    G -->|JSON Code 0: OK| H[Android Notification]
```

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: Modern declarative Jetpack Compose with Material 3 styling (dark theme optimized).
- **Architecture**: Single-Activity pattern (`MainActivity.kt`), Clean Architecture with `ViewModel` and `StateFlow`.
- **Networking**: `com.squareup.okhttp3:okhttp:4.12.0` with verified Creality client headers (`__cxy_*`, user agent spoofing, and dynamic request IDs).
- **Background Jobs**: `androidx.work:work-runtime-ktx:2.9.1` (`CoroutineWorker` + `PeriodicWorkRequest`).
- **Data Persistence**: `androidx.datastore:datastore-preferences:1.1.1` (excluded from OS cloud backups for credential security).
- **Minimum SDK**: Android 8.0 (API 26) • **Target SDK**: Android 14 (API 34).

---

## 🚀 Getting Started

### Prerequisites
- [Android Studio Ladybug / Meerkat](https://developer.android.com/studio) or newer
- Android SDK Platform 34
- JDK 17 or JDK 21

### 1. Clone the Repository
```bash
git clone https://github.com/abhaydasah13/Auto-check-in-for-Creality.git
cd Auto-check-in-for-Creality
```

### 2. Build the Debug APK
```bash
./gradlew assembleDebug
```
The compiled APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 3. Install to Connected Device via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 4. Build Signed Play Store App Bundle (`.aab`)
```bash
./gradlew bundleRelease
```
The signed `.aab` bundle will be generated at:
```
app/build/outputs/bundle/release/app-release.aab
```

### 5. Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

---

## 🔒 Privacy Policy

Your privacy is paramount. This application stores all session tokens in your device's private sandbox storage and communicates directly with Creality Cloud's servers over TLS encryption. 

Read the full [Privacy Policy](PRIVACY_POLICY.md).

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
