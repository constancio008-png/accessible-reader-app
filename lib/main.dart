import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const AccessibleReaderApp());
}

class AccessibleReaderApp extends StatelessWidget {
  const AccessibleReaderApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Accessible Reader',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        useMaterial3: true,
        // High contrast theme for better accessibility
        brightness: Brightness.light,
        textTheme: const TextTheme(
          displayLarge: TextStyle(fontSize: 32, fontWeight: FontWeight.bold),
          displayMedium: TextStyle(fontSize: 28, fontWeight: FontWeight.bold),
          bodyLarge: TextStyle(fontSize: 20),
          bodyMedium: TextStyle(fontSize: 18),
        ),
      ),
      home: const HomeScreen(),
    );
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  static const platform = MethodChannel('com.accessibility.screenreader/service');
  
  bool _isServiceEnabled = false;
  bool _isVoiceControlActive = false;
  String _statusMessage = 'Screen Reader Service: Disabled';

  @override
  void initState() {
    super.initState();
    _checkServiceStatus();
  }

  Future<void> _checkServiceStatus() async {
    try {
      final bool isEnabled = await platform.invokeMethod('isServiceEnabled');
      setState(() {
        _isServiceEnabled = isEnabled;
        _statusMessage = isEnabled 
            ? 'Screen Reader Service: Enabled' 
            : 'Screen Reader Service: Disabled';
      });
    } on PlatformException catch (e) {
      setState(() {
        _statusMessage = 'Error: ${e.message}';
      });
    }
  }

  Future<void> _openAccessibilitySettings() async {
    try {
      await platform.invokeMethod('openAccessibilitySettings');
    } on PlatformException catch (e) {
      _showMessage('Error opening settings: ${e.message}');
    }
  }

  Future<void> _toggleVoiceControl() async {
    try {
      if (_isVoiceControlActive) {
        await platform.invokeMethod('stopVoiceControl');
        setState(() {
          _isVoiceControlActive = false;
        });
        _showMessage('Voice control stopped');
      } else {
        await platform.invokeMethod('startVoiceControl');
        setState(() {
          _isVoiceControlActive = true;
        });
        _showMessage('Voice control started - Say a command');
      }
    } on PlatformException catch (e) {
      _showMessage('Error: ${e.message}');
    }
  }

  Future<void> _speakText(String text) async {
    try {
      await platform.invokeMethod('speak', {'text': text});
    } on PlatformException catch (e) {
      _showMessage('Error: ${e.message}');
    }
  }

  void _showMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message, style: const TextStyle(fontSize: 18)),
        duration: const Duration(seconds: 3),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Accessible Reader', style: TextStyle(fontSize: 24)),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Status Card
            Card(
              elevation: 4,
              child: Padding(
                padding: const EdgeInsets.all(20.0),
                child: Column(
                  children: [
                    Icon(
                      _isServiceEnabled ? Icons.check_circle : Icons.cancel,
                      size: 64,
                      color: _isServiceEnabled ? Colors.green : Colors.red,
                      semanticLabel: _isServiceEnabled ? 'Service enabled' : 'Service disabled',
                    ),
                    const SizedBox(height: 16),
                    Text(
                      _statusMessage,
                      style: Theme.of(context).textTheme.displayMedium,
                      textAlign: TextAlign.center,
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),

            // Enable Service Button
            if (!_isServiceEnabled)
              ElevatedButton.icon(
                onPressed: _openAccessibilitySettings,
                icon: const Icon(Icons.settings_accessibility, size: 32),
                label: const Text('Enable Screen Reader', style: TextStyle(fontSize: 20)),
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.all(20),
                  backgroundColor: Colors.blue,
                  foregroundColor: Colors.white,
                ),
              ),

            const SizedBox(height: 16),

            // Voice Control Section
            Card(
              elevation: 4,
              child: Padding(
                padding: const EdgeInsets.all(20.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text(
                      'Voice Control',
                      style: Theme.of(context).textTheme.displayMedium,
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton.icon(
                      onPressed: _isServiceEnabled ? _toggleVoiceControl : null,
                      icon: Icon(
                        _isVoiceControlActive ? Icons.mic_off : Icons.mic,
                        size: 28,
                      ),
                      label: Text(
                        _isVoiceControlActive ? 'Stop Voice Control' : 'Start Voice Control',
                        style: const TextStyle(fontSize: 18),
                      ),
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.all(16),
                        backgroundColor: _isVoiceControlActive ? Colors.red : Colors.green,
                        foregroundColor: Colors.white,
                      ),
                    ),
                    const SizedBox(height: 16),
                    Text(
                      'Voice Commands:',
                      style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 8),
                    const Text(
                      '• "Go back" - Navigate back\n'
                      '• "Go home" - Go to home screen\n'
                      '• "Recent apps" - Open recent apps\n'
                      '• "Notifications" - Open notifications\n'
                      '• "Quick settings" - Open quick settings\n'
                      '• "Read screen" - Read current screen\n'
                      '• "Stop speaking" - Stop speech output\n'
                      '• "Take screenshot" - Capture screen\n'
                      '• "Lock screen" - Lock the device',
                      style: TextStyle(fontSize: 16, height: 1.5),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 16),

            // Test TTS Button
            Card(
              elevation: 4,
              child: Padding(
                padding: const EdgeInsets.all(20.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text(
                      'Test Speech Output',
                      style: Theme.of(context).textTheme.displayMedium,
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton.icon(
                      onPressed: _isServiceEnabled 
                          ? () => _speakText('Screen reader is working correctly')
                          : null,
                      icon: const Icon(Icons.volume_up, size: 28),
                      label: const Text('Test Speech', style: TextStyle(fontSize: 18)),
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.all(16),
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 16),

            // Features Card
            Card(
              elevation: 4,
              child: Padding(
                padding: const EdgeInsets.all(20.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Features',
                      style: Theme.of(context).textTheme.displayMedium,
                    ),
                    const SizedBox(height: 16),
                    _buildFeatureItem(Icons.record_voice_over, 'Full Screen Reading'),
                    _buildFeatureItem(Icons.touch_app, 'Touch Exploration'),
                    _buildFeatureItem(Icons.gesture, 'Gesture Navigation'),
                    _buildFeatureItem(Icons.mic, 'Voice Commands'),
                    _buildFeatureItem(Icons.keyboard_voice, 'Voice Dictation'),
                    _buildFeatureItem(Icons.accessibility_new, 'TalkBack-style Feedback'),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 24),

            // Refresh Status Button
            OutlinedButton.icon(
              onPressed: _checkServiceStatus,
              icon: const Icon(Icons.refresh, size: 24),
              label: const Text('Refresh Status', style: TextStyle(fontSize: 18)),
              style: OutlinedButton.styleFrom(
                padding: const EdgeInsets.all(16),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFeatureItem(IconData icon, String text) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Row(
        children: [
          Icon(icon, size: 28, color: Colors.blue),
          const SizedBox(width: 16),
          Expanded(
            child: Text(
              text,
              style: const TextStyle(fontSize: 18),
            ),
          ),
        ],
      ),
    );
  }
}
