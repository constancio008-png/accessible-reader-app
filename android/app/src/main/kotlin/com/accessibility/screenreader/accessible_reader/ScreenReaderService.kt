package com.accessibility.screenreader.accessible_reader

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import java.util.*

class ScreenReaderService : AccessibilityService() {
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private var voiceControlHandler: VoiceControlHandler? = null
    private val TAG = "ScreenReaderService"
    
    companion object {
        var instance: ScreenReaderService? = null
        var isServiceEnabled = false
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isServiceEnabled = true
        
        Log.d(TAG, "Screen Reader Service Connected")
        
        // Initialize Text-to-Speech
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                } else {
                    isTtsInitialized = true
                    speak("Screen Reader Activated")
                }
            } else {
                Log.e(TAG, "TTS initialization failed")
            }
        }
        
        // Configure accessibility service
        val info = AccessibilityServiceInfo()
        info.apply {
            // Listen to all event types
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            
            // Provide spoken feedback
            feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
            
            // Set notification timeout
            notificationTimeout = 100
            
            // Retrieve window content
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE or
                    AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY
        }
        
        serviceInfo = info
        Log.d(TAG, "Service configured successfully")
        
        // Initialize voice control
        voiceControlHandler = VoiceControlHandler(this)
        voiceControlHandler?.initialize()
        voiceControlHandler?.setVoiceCommandListener(object : VoiceControlHandler.VoiceCommandListener {
            override fun onVoiceCommand(command: String) {
                Log.d(TAG, "Voice command received: $command")
            }
            
            override fun onVoiceError(error: String) {
                Log.e(TAG, "Voice error: $error")
            }
        })
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !isTtsInitialized) return
        
        val eventType = event.eventType
        val source = event.source
        
        Log.d(TAG, "Event: ${AccessibilityEvent.eventTypeToString(eventType)}")
        
        when (eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                handleViewClicked(event)
            }
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                handleViewFocused(event)
            }
            AccessibilityEvent.TYPE_VIEW_SELECTED -> {
                handleViewSelected(event)
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChanged(event)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                handleWindowContentChanged(event)
            }
            AccessibilityEvent.TYPE_VIEW_HOVER_ENTER -> {
                handleViewHoverEnter(event)
            }
            AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START -> {
                // User started exploring with touch
            }
            AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END -> {
                // User finished exploring with touch
            }
            AccessibilityEvent.TYPE_ANNOUNCEMENT -> {
                handleAnnouncement(event)
            }
        }
        
        source?.recycle()
    }

    private fun handleViewClicked(event: AccessibilityEvent) {
        val text = getEventText(event)
        if (text.isNotEmpty()) {
            speak("Clicked: $text")
        }
    }

    private fun handleViewFocused(event: AccessibilityEvent) {
        val text = getEventText(event)
        if (text.isNotEmpty()) {
            speak(text)
        }
    }

    private fun handleViewSelected(event: AccessibilityEvent) {
        val text = getEventText(event)
        if (text.isNotEmpty()) {
            speak("Selected: $text")
        }
    }

    private fun handleWindowStateChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: ""
        val className = event.className?.toString() ?: ""
        
        if (packageName.isNotEmpty()) {
            val appName = getApplicationName(packageName)
            speak("Opened $appName")
        }
    }

    private fun handleWindowContentChanged(event: AccessibilityEvent) {
        // Handle content changes if needed
        // Can be used to announce dynamic content updates
    }

    private fun handleViewHoverEnter(event: AccessibilityEvent) {
        val text = getEventText(event)
        if (text.isNotEmpty()) {
            speak(text)
        }
    }

    private fun handleAnnouncement(event: AccessibilityEvent) {
        val text = getEventText(event)
        if (text.isNotEmpty()) {
            speak(text)
        }
    }

    private fun getEventText(event: AccessibilityEvent): String {
        val sb = StringBuilder()
        
        // Get text from event
        if (event.text.isNotEmpty()) {
            for (s in event.text) {
                sb.append(s)
                sb.append(" ")
            }
        }
        
        // Get content description
        if (sb.isEmpty() && event.contentDescription != null) {
            sb.append(event.contentDescription)
        }
        
        // Get text from source node
        if (sb.isEmpty()) {
            val source = event.source
            if (source != null) {
                val nodeText = getNodeText(source)
                sb.append(nodeText)
                source.recycle()
            }
        }
        
        return sb.toString().trim()
    }

    private fun getNodeText(node: AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        
        // Get content description first
        if (node.contentDescription != null) {
            sb.append(node.contentDescription)
            sb.append(" ")
        }
        
        // Get text
        if (node.text != null) {
            sb.append(node.text)
            sb.append(" ")
        }
        
        // Get hint text
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (node.hintText != null) {
                sb.append(node.hintText)
                sb.append(" ")
            }
        }
        
        // Describe the type of view
        if (sb.isEmpty()) {
            val className = node.className?.toString() ?: ""
            when {
                className.contains("Button") -> sb.append("Button")
                className.contains("EditText") -> sb.append("Edit field")
                className.contains("TextView") -> sb.append("Text")
                className.contains("ImageView") -> sb.append("Image")
                className.contains("CheckBox") -> sb.append(if (node.isChecked) "Checked" else "Unchecked")
                className.contains("Switch") -> sb.append(if (node.isChecked) "On" else "Off")
            }
        }
        
        return sb.toString().trim()
    }

    private fun getApplicationName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    fun speak(text: String) {
        if (isTtsInitialized && text.isNotEmpty()) {
            Log.d(TAG, "Speaking: $text")
            textToSpeech?.speak(text, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString())
        }
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
        stopSpeaking()
    }

    fun startVoiceControl() {
        voiceControlHandler?.startListening()
    }
    
    fun stopVoiceControl() {
        voiceControlHandler?.stopListening()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        isServiceEnabled = false
        
        voiceControlHandler?.destroy()
        voiceControlHandler = null
        
        textToSpeech?.shutdown()
        textToSpeech = null
        
        Log.d(TAG, "Service destroyed")
    }

    // Gesture handling
    override fun onGesture(gestureId: Int): Boolean {
        Log.d(TAG, "Gesture detected: $gestureId")
        
        when (gestureId) {
            GESTURE_SWIPE_UP -> {
                speak("Swipe up")
                return true
            }
            GESTURE_SWIPE_DOWN -> {
                speak("Swipe down")
                return true
            }
            GESTURE_SWIPE_LEFT -> {
                speak("Swipe left")
                performGlobalAction(GLOBAL_ACTION_BACK)
                return true
            }
            GESTURE_SWIPE_RIGHT -> {
                speak("Swipe right")
                return true
            }
            GESTURE_SWIPE_UP_AND_DOWN -> {
                speak("Swipe up and down")
                return true
            }
            GESTURE_SWIPE_DOWN_AND_UP -> {
                speak("Swipe down and up")
                return true
            }
            GESTURE_SWIPE_LEFT_AND_RIGHT -> {
                speak("Swipe left and right")
                return true
            }
            GESTURE_SWIPE_RIGHT_AND_LEFT -> {
                speak("Swipe right and left")
                return true
            }
        }
        
        return super.onGesture(gestureId)
    }
}
