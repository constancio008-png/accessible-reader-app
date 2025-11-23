# Build Instructions for Accessible Reader

## Quick Start

This document provides detailed instructions for building the Accessible Reader Android APK from source code.

## Prerequisites

Before building, ensure you have the following installed:

### 1. Flutter SDK
- Download from: https://flutter.dev/docs/get-started/install
- Version: 3.24.5 or later
- Add Flutter to your PATH

### 2. Android Studio
- Download from: https://developer.android.com/studio
- Install Android SDK through Android Studio
- Required SDK components:
  - Android SDK Platform 28 (Android 9.0)
  - Android SDK Build-Tools 33.0.1
  - Android SDK Command-line Tools

### 3. Java Development Kit (JDK)
- JDK 17 (OpenJDK recommended)
- Download from: https://adoptium.net/

## Setup Steps

### Step 1: Install Flutter

**Windows:**
```powershell
# Extract Flutter SDK to C:\src\flutter
# Add to PATH: C:\src\flutter\bin

# Verify installation
flutter doctor
```

**macOS/Linux:**
```bash
# Extract Flutter SDK
cd ~/development
unzip flutter_linux_3.24.5-stable.tar.xz
export PATH="$PATH:`pwd`/flutter/bin"

# Add to shell profile (.bashrc, .zshrc, etc.)
echo 'export PATH="$PATH:$HOME/development/flutter/bin"' >> ~/.bashrc

# Verify installation
flutter doctor
```

### Step 2: Install Android Studio and SDK

1. Download and install Android Studio
2. Open Android Studio
3. Go to: **Tools** > **SDK Manager**
4. Install the following:
   - **SDK Platforms** tab:
     - Android 9.0 (Pie) - API Level 28
   - **SDK Tools** tab:
     - Android SDK Build-Tools 33.0.1
     - Android SDK Command-line Tools
     - Android Emulator (optional, for testing)

5. Accept SDK licenses:
```bash
flutter doctor --android-licenses
```

### Step 3: Configure Environment Variables

**Windows:**
```powershell
# Set ANDROID_HOME
setx ANDROID_HOME "C:\Users\YourUsername\AppData\Local\Android\Sdk"

# Add to PATH
setx PATH "%PATH%;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\tools\bin"
```

**macOS/Linux:**
```bash
# Add to ~/.bashrc or ~/.zshrc
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/tools/bin
```

### Step 4: Verify Setup

Run Flutter doctor to check your setup:
```bash
flutter doctor -v
```

Expected output should show:
- ✓ Flutter (Channel stable, version 3.24.5)
- ✓ Android toolchain
- ✓ Android Studio

## Building the APK

### Method 1: Using Android Studio (Recommended)

1. **Open Project:**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to `accessible_reader` folder
   - Click "OK"

2. **Wait for Gradle Sync:**
   - Android Studio will automatically sync Gradle
   - Wait for "Gradle sync finished" message
   - This may take 5-10 minutes on first run

3. **Build APK:**
   - Select **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**
   - Wait for build to complete (2-5 minutes)
   - Click "locate" in the notification to find the APK

4. **APK Location:**
   ```
   accessible_reader/build/app/outputs/flutter-apk/app-release.apk
   ```

### Method 2: Using Command Line

1. **Navigate to Project:**
```bash
cd accessible_reader
```

2. **Clean Previous Builds (optional but recommended):**
```bash
flutter clean
```

3. **Get Dependencies:**
```bash
flutter pub get
```

4. **Build Release APK:**
```bash
flutter build apk --release
```

5. **Build Output:**
```
APK location: build/app/outputs/flutter-apk/app-release.apk
File size: ~20-30 MB
```

### Method 3: Build Split APKs (Smaller file size)

Build separate APKs for different CPU architectures:

```bash
flutter build apk --split-per-abi
```

This creates three APKs:
- `app-armeabi-v7a-release.apk` (for 32-bit ARM devices)
- `app-arm64-v8a-release.apk` (for 64-bit ARM devices)
- `app-x86_64-release.apk` (for x86 devices)

### Method 4: Build Debug APK (for testing)

```bash
flutter build apk --debug
```

Debug APKs are larger but include debugging symbols.

## Build Options

### Custom Build Configurations

**Build with specific target:**
```bash
flutter build apk --release --target lib/main.dart
```

**Build with obfuscation (for production):**
```bash
flutter build apk --release --obfuscate --split-debug-info=./debug-info
```

**Build for specific architecture:**
```bash
flutter build apk --release --target-platform android-arm64
```

## Troubleshooting

### Issue: "Flutter SDK not found"
**Solution:**
```bash
# Verify Flutter is in PATH
which flutter  # macOS/Linux
where flutter  # Windows

# If not found, add to PATH or reinstall Flutter
```

### Issue: "Android SDK not found"
**Solution:**
```bash
# Set ANDROID_HOME
export ANDROID_HOME=$HOME/Android/Sdk  # macOS/Linux
setx ANDROID_HOME "C:\Users\YourUsername\AppData\Local\Android\Sdk"  # Windows

# Run flutter doctor to verify
flutter doctor
```

### Issue: "License not accepted"
**Solution:**
```bash
flutter doctor --android-licenses
# Press 'y' to accept all licenses
```

### Issue: "Gradle build failed"
**Solution:**
```bash
# Clean and rebuild
cd accessible_reader
flutter clean
rm -rf build/
flutter pub get
flutter build apk --release
```

### Issue: "Out of memory during build"
**Solution:**

Edit `android/gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=512m
```

### Issue: "SDK version not found"
**Solution:**
- Open Android Studio
- Go to SDK Manager
- Install Android SDK Platform 28
- Install Build Tools 33.0.1

## Build Time Expectations

- **First build**: 5-15 minutes (downloads dependencies)
- **Subsequent builds**: 2-5 minutes
- **Clean builds**: 3-8 minutes

## APK Signing (Optional)

For production release, sign the APK:

1. **Generate keystore:**
```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

2. **Create key.properties:**
```properties
storePassword=<password>
keyPassword=<password>
keyAlias=my-key-alias
storeFile=<location of my-release-key.jks>
```

3. **Update build.gradle** (already configured in the project)

4. **Build signed APK:**
```bash
flutter build apk --release
```

## Testing the APK

### On Emulator:
```bash
flutter install
```

### On Physical Device:
```bash
# Enable USB debugging on device
# Connect device via USB
adb install build/app/outputs/flutter-apk/app-release.apk
```

## Build Verification

After building, verify the APK:

```bash
# Check APK size
ls -lh build/app/outputs/flutter-apk/app-release.apk

# Verify APK contents
unzip -l build/app/outputs/flutter-apk/app-release.apk

# Check APK signature
jarsigner -verify -verbose -certs build/app/outputs/flutter-apk/app-release.apk
```

## Next Steps

After successful build:
1. Transfer APK to Android device
2. Install the APK
3. Follow usage instructions in README.md
4. Enable accessibility service in Android Settings

## Additional Resources

- Flutter Documentation: https://flutter.dev/docs
- Android Developer Guide: https://developer.android.com/guide
- Accessibility Services: https://developer.android.com/guide/topics/ui/accessibility/service

## Support

If you encounter issues not covered here:
1. Check `flutter doctor` output
2. Review build logs in `build/` directory
3. Ensure all prerequisites are correctly installed
4. Try building with `--verbose` flag for detailed output:
   ```bash
   flutter build apk --release --verbose
   ```
