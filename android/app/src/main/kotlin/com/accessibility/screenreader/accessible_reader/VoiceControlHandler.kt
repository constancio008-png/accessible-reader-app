package com.accessibility.screenreader.accessible_reader

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.*

class VoiceControlHandler(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val TAG = "VoiceControlHandler"
    
    interface VoiceCommandListener {
        fun onVoiceCommand(command: String)
        fun onVoiceError(error: String)
    }
    
    private var commandListener: VoiceCommandListener? = null
    
    fun setVoiceCommandListener(listener: VoiceCommandListener) {
        commandListener = listener
    }
    
    fun initialize() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(recognitionListener)
            Log.d(TAG, "Speech recognizer initialized")
        } else {
            Log.e(TAG, "Speech recognition not available")
        }
    }
    
    fun startListening() {
        if (isListening) {
            Log.d(TAG, "Already listening")
            return
        }
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        
        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            Log.d(TAG, "Started listening for voice commands")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition: ${e.message}")
            commandListener?.onVoiceError("Failed to start listening: ${e.message}")
        }
    }
    
    fun stopListening() {
        if (!isListening) return
        
        try {
            speechRecognizer?.stopListening()
            isListening = false
            Log.d(TAG, "Stopped listening")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition: ${e.message}")
        }
    }
    
    fun destroy() {
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        Log.d(TAG, "Voice control handler destroyed")
    }
    
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "Ready for speech")
        }
        
        override fun onBeginningOfSpeech() {
            Log.d(TAG, "Beginning of speech")
        }
        
        override fun onRmsChanged(rmsdB: Float) {
            // Audio level changed
        }
        
        override fun onBufferReceived(buffer: ByteArray?) {
            // Audio buffer received
        }
        
        override fun onEndOfSpeech() {
            Log.d(TAG, "End of speech")
            isListening = false
        }
        
        override fun onError(error: Int) {
            isListening = false
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Unknown error: $error"
            }
            Log.e(TAG, "Speech recognition error: $errorMessage")
            commandListener?.onVoiceError(errorMessage)
        }
        
        override fun onResults(results: Bundle?) {
            isListening = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (matches != null && matches.isNotEmpty()) {
                val command = matches[0]
                Log.d(TAG, "Voice command recognized: $command")
                commandListener?.onVoiceCommand(command)
                processVoiceCommand(command)
            }
        }
        
        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (matches != null && matches.isNotEmpty()) {
                Log.d(TAG, "Partial result: ${matches[0]}")
            }
        }
        
        override fun onEvent(eventType: Int, params: Bundle?) {
            Log.d(TAG, "Recognition event: $eventType")
        }
    }
    
    private fun processVoiceCommand(command: String) {
        val lowerCommand = command.lowercase(Locale.getDefault())
        val service = ScreenReaderService.instance
        
        when {
            lowerCommand.contains("go back") || lowerCommand.contains("back") -> {
                service?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
                service?.speak("Going back")
            }
            lowerCommand.contains("go home") || lowerCommand.contains("home") -> {
                service?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME)
                service?.speak("Going home")
            }
            lowerCommand.contains("recent apps") || lowerCommand.contains("recents") -> {
                service?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS)
                service?.speak("Opening recent apps")
            }
            lowerCommand.contains("notifications") -> {
                service?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
                service?.speak("Opening notifications")
            }
            lowerCommand.contains("quick settings") -> {
                service?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
                service?.speak("Opening quick settings")
            }
            lowerCommand.contains("power dialog") || lowerCommand.contains("power menu") -> {
                service?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
                service?.speak("Opening power dialog")
            }
            lowerCommand.contains("lock screen") -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    service?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
                    service?.speak("Locking screen")
                }
            }
            lowerCommand.contains("take screenshot") || lowerCommand.contains("screenshot") -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    service?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
                    service?.speak("Taking screenshot")
                }
            }
            lowerCommand.contains("scroll up") -> {
                service?.speak("Scrolling up")
                // Implement scroll action
            }
            lowerCommand.contains("scroll down") -> {
                service?.speak("Scrolling down")
                // Implement scroll action
            }
            lowerCommand.contains("stop speaking") || lowerCommand.contains("be quiet") -> {
                service?.stopSpeaking()
            }
            lowerCommand.contains("read screen") || lowerCommand.contains("what's on screen") -> {
                service?.speak("Reading screen content")
                readCurrentScreen()
            }
            else -> {
                service?.speak("Command not recognized: $command")
            }
        }
    }
    
    private fun readCurrentScreen() {
        val service = ScreenReaderService.instance
        service?.rootInActiveWindow?.let { root ->
            val screenText = StringBuilder()
            extractTextFromNode(root, screenText)
            if (screenText.isNotEmpty()) {
                service.speak(screenText.toString())
            } else {
                service.speak("No text found on screen")
            }
            root.recycle()
        }
    }
    
    private fun extractTextFromNode(node: android.view.accessibility.AccessibilityNodeInfo, text: StringBuilder) {
        if (node.contentDescription != null) {
            text.append(node.contentDescription)
            text.append(". ")
        }
        if (node.text != null) {
            text.append(node.text)
            text.append(". ")
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                extractTextFromNode(child, text)
                child.recycle()
            }
        }
    }
}
