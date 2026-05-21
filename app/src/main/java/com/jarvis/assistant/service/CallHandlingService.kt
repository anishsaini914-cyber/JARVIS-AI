package com.jarvis.assistant.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.jarvis.assistant.utils.Constants
import com.jarvis.assistant.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CallHandlingService : AccessibilityService() {

    @Inject
    lateinit var prefs: PreferencesManager

    private var textToSpeech: TextToSpeech? = null

    override fun onCreate() {
        super.onCreate()
        initTts()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!prefs.getBoolean(Constants.PREF_CALL_HANDLING_ENABLED, false)) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowChange(event)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                handleContentChange(event)
            }
        }
    }

    private fun handleWindowChange(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: return

        Log.d("CallHandling", "Window: $packageName / $className")

        // Detect incoming call screen
        if (packageName == "com.android.dialer" || packageName == "com.android.incallui") {
            if (className.contains("InCallActivity") || className.contains("CallActivity")) {
                announceCaller()
            }
        }
    }

    private fun handleContentChange(event: AccessibilityEvent) {
        // Check for incoming call notification
        if (event.text?.any { it.contains("incoming call", ignoreCase = true) } == true) {
            announceCaller()
        }
    }

    private fun announceCaller() {
        try {
            val root = rootInActiveWindow ?: return
            val callerName = findCallerName(root)
            val announcement = if (callerName != null) {
                "Incoming call from $callerName"
            } else {
                "Incoming call"
            }

            // Vibrate
            vibrate()

            // Announce via TTS
            textToSpeech?.speak(announcement, TextToSpeech.QUEUE_FLUSH, null, "call_announcement")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun findCallerName(node: AccessibilityNodeInfo): String? {
        // Search for caller name in the active window
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(node)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val text = current.text?.toString()
            if (text != null && text.length in 2..50 && !text.contains(Regex("[0-9]{3,}"))) {
                // Potential caller name
                if (!text.contains("phone", ignoreCase = true) &&
                    !text.contains("call", ignoreCase = true) &&
                    !text.contains("answer", ignoreCase = true) &&
                    !text.contains("decline", ignoreCase = true)
                ) {
                    return text
                }
            }

            for (i in 0 until current.childCount) {
                current.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    fun answerCall() {
        // Try to find and click the answer button
        try {
            val root = rootInActiveWindow ?: return
            val answerButton = findButton(root, listOf("answer", "accept", "pick up", "uthao"))
            if (answerButton != null) {
                performActionOnNode(answerButton)
            } else {
                // Fallback: perform swipe gesture
                performSwipeGesture(500, 1500, 500, 500)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun endCall() {
        try {
            val root = rootInActiveWindow ?: return
            val endButton = findButton(root, listOf("end", "hang up", "decline", "reject", "cut"))
            if (endButton != null) {
                performActionOnNode(endButton)
            } else {
                // Fallback: perform swipe gesture
                performSwipeGesture(500, 500, 500, 1500)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleSpeaker() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        if (audioManager.isSpeakerphoneOn) {
            audioManager.isSpeakerphoneOn = false
            audioManager.mode = AudioManager.MODE_IN_CALL
        } else {
            audioManager.isSpeakerphoneOn = true
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        }
    }

    fun muteCall() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.isMicrophoneMute = true
    }

    fun unmuteCall() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.isMicrophoneMute = false
    }

    private fun findButton(node: AccessibilityNodeInfo, labels: List<String>): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(node)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val text = current.text?.toString()?.lowercase() ?: ""
            val contentDesc = current.contentDescription?.toString()?.lowercase() ?: ""

            if (labels.any { label -> text.contains(label) || contentDesc.contains(label) }) {
                if (current.isClickable) {
                    return current
                }
            }

            for (i in 0 until current.childCount) {
                current.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    private fun performActionOnNode(node: AccessibilityNodeInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            val clickPath = Path().apply {
                moveTo(bounds.centerX().toFloat(), bounds.centerY().toFloat())
                lineTo(bounds.centerX().toFloat(), bounds.centerY().toFloat())
            }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(clickPath, 0, 100))
                .build()
            dispatchGesture(gesture, null, null)
        } else {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }

    private fun performSwipeGesture(startX: Float, startY: Float, endX: Float, endY: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 500))
                .build()
            dispatchGesture(gesture, null, null)
        }
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onDestroy()
    }
}
