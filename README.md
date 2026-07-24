# Charging Animation Pro ⚡

A premium Android application built with **Kotlin** and **Jetpack Compose** that displays floating transparent charging animations overlaying all applications when the device is connected to a charger.

Designed with **Glassmorphism**, **Nothing OS**, **Samsung OneUI**, and **iPhone Dynamic Island** aesthetics.

---

## 🌟 Key Features

- 🔋 **Automatic Charger Detection**: Listens to system broadcasts for `ACTION_POWER_CONNECTED` & `ACTION_POWER_DISCONNECTED`.
- 🪟 **System Alert Window Floating Overlay**: Displays clean, rounded glassmorphic islands directly over any open app.
- ⚡ **Detailed Telemetry**:
  - Live Battery %
  - Fast Charging (67W / 120W SuperVOOC / USB / Wireless)
  - Voltage (mV) & Temperature (°C)
  - Battery Health & Time Remaining Estimates
- 🎨 **Multiple Preset Animations**: Neon Blue, Green Energy, Purple Glow, Orange Fire, Cyberpunk HUD, Minimal Glass, Nothing OS Dot.
- 🛠️ **Full Settings Engine**:
  - Auto-hide duration (8s, 15s, or Always On)
  - Animation speed & size customization
  - Connection chime sound & haptic vibration support
  - Dark / Light / AMOLED themes

---

## 📱 How to Build in Android Studio

1. **Extract ZIP file**: Unzip `ChargingAnimationPro_AndroidStudio.zip`.
2. **Open in Android Studio**:
   - Select **File -> Open...**
   - Choose the extracted root directory.
3. **Sync Gradle**: Allow Gradle to resolve dependencies.
4. **Run on Device / Emulator**:
   - Ensure Minimum SDK is Android 8.0 (API level 26) or higher.
   - Click **Run 'app'**.
5. **Grant Overlay Permission**:
   - On first launch, grant **Display over other apps** (`SYSTEM_ALERT_WINDOW`) permission.

---

## 🧪 CI/CD GitHub Actions

This project includes a fully configured `.github/workflows/build.yml` file that runs:

```bash
./gradlew assembleDebug
```

Target SDK: 35 | Minimum SDK: 26 | Kotlin 2.0.21
