# Sharma Khata & Billing - Android App Build Instructions

## Prerequisites

Ensure you have installed:
1. **Java Development Kit (JDK)** - Version 11 or higher
   - Download from: https://www.oracle.com/java/technologies/downloads/
   - Set `JAVA_HOME` environment variable

2. **Android SDK**
   - Install via Android Studio or command-line tools
   - The project already has `sdk.dir=/opt/android/sdk` configured in `local.properties`

3. **Gradle** (Optional - we have a wrapper)
   - System Gradle or Android Studio's built-in Gradle

## Build Methods

### Method 1: Using Android Studio (Recommended)

1. Open Android Studio
2. Open the project folder: `e:\sharma-khata-&-billing`
3. Wait for Gradle sync to complete
4. Go to **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
5. APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### Method 2: Using Command Line (Windows)

```batch
cd e:\sharma-khata-&-billing
gradlew.bat build
```

Or with system Gradle:
```batch
cd e:\sharma-khata-&-billing
gradle build
```

### Method 3: Build Debug APK Only

```batch
cd e:\sharma-khata-&-billing
gradlew.bat assembleDebug
```

Or for release (requires keystore):
```batch
cd e:\sharma-khata-&-billing
gradlew.bat assembleRelease
```

## Expected Output

After a successful build, you should see:

```
BUILD SUCCESSFUL in XmXs
```

And the APK will be located at:
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release.apk` (if built)

## Troubleshooting

### Issue: "java.exe not found"
**Solution**: 
- Install JDK and set JAVA_HOME environment variable
- Verify: `java -version` from command prompt

### Issue: "Android SDK not found"
**Solution**:
- Update `local.properties`:
  ```
  sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
  ```
- Or set `ANDROID_HOME` environment variable

### Issue: Gradle build fails
**Solution**:
- Run: `gradlew.bat clean build`
- Check for sufficient disk space (at least 5GB free)
- Delete `.gradle` folder and retry: `rmdir /s .gradle`

### Issue: Port already in use
**Solution**:
- The app runs entirely offline using WebView - no port conflicts

## Build Output Locations

| File | Location |
|------|----------|
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` |
| Release APK | `app/build/outputs/apk/release/app-release.apk` |
| Build Reports | `app/build/reports/` |
| Generated Classes | `app/build/generated/` |

## Next Steps After Build

1. **Install on Device/Emulator**:
   ```batch
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

2. **View App**:
   - Open "Sharma Khata" app on your device
   - The app loads `dashboard.html` embedded in a WebView

3. **Start Backend (Optional)**:
   ```bash
   npm install
   node server.js
   ```
   This enables WhatsApp webhook functionality

## Build Configuration

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 15)
- **Compile SDK**: 36
- **Kotlin Version**: Latest from libs.versions.toml
- **Compose UI**: Latest Material 3

## Security Notes

- Release builds require a signing keystore
- Store environment variables securely (KEYSTORE_PATH, STORE_PASSWORD, KEY_PASSWORD)
- See `app/build.gradle.kts` for signing configuration

---

**Need help?** Check the main README.md for architecture details.
