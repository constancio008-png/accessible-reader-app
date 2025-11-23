package com.accessibility.screenreader.accessible_reader

import android.content.Context
import android.content.Intent
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.accessibility.screenreader/service"
    
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "isServiceEnabled" -> {
                    val isEnabled = ScreenReaderService.isServiceEnabled
                    result.success(isEnabled)
                }
                "openAccessibilitySettings" -> {
                    try {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        result.success(true)
                    } catch (e: Exception) {
                        result.error("ERROR", "Failed to open accessibility settings: ${e.message}", null)
                    }
                }
                "startVoiceControl" -> {
                    try {
                        val service = ScreenReaderService.instance
                        if (service != null) {
                            service.startVoiceControl()
                            result.success(true)
                        } else {
                            result.error("SERVICE_NOT_ENABLED", "Accessibility service is not enabled", null)
                        }
                    } catch (e: Exception) {
                        result.error("ERROR", "Failed to start voice control: ${e.message}", null)
                    }
                }
                "stopVoiceControl" -> {
                    try {
                        val service = ScreenReaderService.instance
                        if (service != null) {
                            service.stopVoiceControl()
                            result.success(true)
                        } else {
                            result.error("SERVICE_NOT_ENABLED", "Accessibility service is not enabled", null)
                        }
                    } catch (e: Exception) {
                        result.error("ERROR", "Failed to stop voice control: ${e.message}", null)
                    }
                }
                "speak" -> {
                    try {
                        val text = call.argument<String>("text")
                        val service = ScreenReaderService.instance
                        if (service != null && text != null) {
                            service.speak(text)
                            result.success(true)
                        } else {
                            result.error("SERVICE_NOT_ENABLED", "Accessibility service is not enabled or text is null", null)
                        }
                    } catch (e: Exception) {
                        result.error("ERROR", "Failed to speak text: ${e.message}", null)
                    }
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }
}
