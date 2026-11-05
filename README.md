# QuickCalc Android

A lightweight native Android calculator (Kotlin-free Java Activity, no third-party libraries).

## Build APK
- Open this project in Android Studio or AndroidIDE.
- Use JDK 17 and Android SDK Platform 35.
- Run `gradle assembleDebug`.
- APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Build without a laptop
1. Create a GitHub repository from these project files and push them.
2. Open the repository's **Actions** tab.
3. Run **Build Android APK** (or push to `main`).
4. Download the `QuickCalc-debug-apk` artifact from the completed workflow.
5. Extract `app-debug.apk` on your Android phone and install it. Allow installs from that source if Android asks.

Features: basic arithmetic, decimals, percent, sign toggle, clear and delete.
