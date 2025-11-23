# Accessible Reader - Android Screen Reader App

A comprehensive screen reader application for blind and visually impaired users, built with Flutter and native Android accessibility services.

## Features

### Core Screen Reader Functionality
- **Full Screen Reading**: Reads all UI elements on the screen using Android's AccessibilityService API
- **Touch Exploration**: Speaks element descriptions when touched
- **Gesture Navigation**: Supports swipe gestures for navigation
- **TalkBack-style Feedback**: Provides spoken feedback similar to Google TalkBack
- **Window Content Reading**: Automatically announces app names and screen changes
- **Text-to-Speech (TTS)**: High-quality speech output for all content

### Voice Control & Dictation
- **Voice Commands**: Full voice control for device navigation
  - "Go back" - Navigate back
  - "Go home" - Go to home screen
  - "Recent apps" - Open recent apps
  - "Notifications" - Open notifications
  - "Quick settings" - Open quick settings
  - "Read screen" - Read current screen content
  - "Stop speaking" - Stop speech output
  - "Take screenshot" - Capture screen
  - "Lock screen" - Lock the device
  - "Power menu" - Open power dialog

- **Continuous Voice Recognition**: Real-time voice command processing
- **Voice Dictation**: Speak to input text in any field

### Accessibility Features
- High contrast UI with large text
- Semantic labels for screen readers
- Keyboard navigation support
- Focus management
- WCAG 2.2 compliant design

## Technical Architecture

### Native Android Components
- **ScreenReaderService.kt**: Main accessibility service that extends AccessibilityService
  - Handles all accessibility events
  - Manages Text-to-Speech engine
  - Processes touch exploration
  - Implements gesture recognition
  
- **VoiceControlHandler.kt**: Speech recognition and voice command processing
  - Continuous listening mode
  - Command interpretation
  - Action execution

- **MainActivity.kt**: Flutter activity with platform channel bridge
  - Bidirectional communication between Flutter and native Android
  - Service status monitoring
  - Settings management

### Flutter UI
- **main.dart**: User interface for app control and settings
  - Service enable/disable toggle
  - Voice control activation
  - Feature overview
  - Test speech functionality

### Configuration Files
- **AndroidManifest.xml**: Declares accessibility service and permissions
- **accessibility_service_config.xml**: Configures accessibility service behavior
- **build.gradle**: Android build configuration with SDK settings
- **local.properties**: SDK version configuration

## SDK Configuration

The app is configured to support:
- **Compile SDK**: 28 (Android 9.0)
- **Target SDK**: 28 (Android 9.0)
- **Min SDK**: 21 (Android 5.0 Lollipop)

This ensures broad compatibility across Android versions 5.0 to 9.0 and newer.

## Required Permissions

The app requires the following permissions:
- `BIND_ACCESSIBILITY_SERVICE`: Required for accessibility service
- `RECORD_AUDIO`: For voice control and dictation
- `MODIFY_AUDIO_SETTINGS`: For audio feedback control
- `VIBRATE`: For haptic feedback
- `INTERNET`: For potential future features

## Building the App

### Prerequisites
1. Flutter SDK (3.24.5 or later)
2. Android SDK with:
   - Platform 28 (Android 9.0)
   - Build Tools 33.0.1
3. Java JDK 17
4. Android Studio (recommended) or command line tools

### Build Instructions

#### Using Android Studio:
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Select "Build" > "Build Bundle(s) / APK(s)" > "Build APK(s)"
4. APK will be generated in `build/app/outputs/flutter-apk/app-release.apk`

#### Using Command Line:
```bash
# Navigate to project directory
cd accessible_reader

# Clean previous builds
flutter clean

# Get dependencies
flutter pub get

# Build release APK
flutter build apk --release

# APK location: build/app/outputs/flutter-apk/app-release.apk
```

#### Build Debug APK:
```bash
flutter build apk --debug
```

## Installation

### On Physical Device:
1. Enable "Unknown Sources" in Android Settings > Security
2. Transfer the APK to your device
3. Open the APK file and tap "Install"
4. Grant necessary permissions when prompted

### Enable the Screen Reader:
1. Open the Accessible Reader app
2. Tap "Enable Screen Reader"
3. This opens Android Accessibility Settings
4. Find "Accessible Reader" in the list
5. Toggle it ON
6. Confirm the permission dialog
7. Return to the app

## Usage

### Basic Operation:
1. Launch the Accessible Reader app
2. Enable the accessibility service (first time only)
3. The service will start reading screen elements automatically
4. Touch any element to hear its description
5. Use gestures to navigate:
   - Swipe left: Go back
   - Swipe right: Navigate forward
   - Swipe up/down: Scroll

### Voice Control:
1. In the app, tap "Start Voice Control"
2. Speak a command from the supported list
3. The app will execute the command and provide feedback
4. Tap "Stop Voice Control" to disable

### Test Speech:
1. Tap "Test Speech" button in the app
2. The app will speak a test message
3. Verifies TTS is working correctly

## Project Structure

```
accessible_reader/
├── android/
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── kotlin/com/accessibility/screenreader/accessible_reader/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ScreenReaderService.kt
│   │   │   │   └── VoiceControlHandler.kt
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   └── strings.xml
│   │   │   │   └── xml/
│   │   │   │       └── accessibility_service_config.xml
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle
│   └── local.properties
├── lib/
│   └── main.dart
├── pubspec.yaml
└── README.md
```

## Troubleshooting

### Service Not Starting:
- Ensure accessibility permission is granted in Settings
- Restart the device
- Reinstall the app

### No Speech Output:
- Check device volume
- Verify TTS engine is installed (Google Text-to-Speech)
- Test with "Test Speech" button in app

### Voice Control Not Working:
- Grant microphone permission
- Check internet connection (for some voice recognition features)
- Ensure Google app is installed and updated

### Build Errors:
- Run `flutter clean`
- Delete `build/` directory
- Run `flutter pub get`
- Ensure Android SDK is properly configured
- Check that all required SDK platforms and build tools are installed

## Compatibility

- **Android Version**: 5.0 (Lollipop) and above
- **Tested On**: Android 9.0, 10, 11, 12
- **Architecture**: ARM, ARM64, x86, x64

## Based on TalkBack Architecture

This app implements screen reader functionality inspired by Google TalkBack:
- Event-driven accessibility architecture
- Touch exploration mode
- Gesture-based navigation
- Comprehensive screen element description
- Focus management
- Content change detection

## Future Enhancements

Potential features for future versions:
- Braille display support
- OCR for image text extraction
- Customizable voice and speech rate
- Reading mode preferences
- App-specific navigation rules
- Gesture customization
- Multi-language support
- Cloud sync for settings

## License

This project is provided as-is for accessibility purposes.

## Support

For issues, questions, or contributions, please refer to the project documentation.

## Acknowledgments

- Inspired by Google TalkBack
- Built with Flutter framework
- Uses Android Accessibility Service API
- Implements Android Speech Recognition API
