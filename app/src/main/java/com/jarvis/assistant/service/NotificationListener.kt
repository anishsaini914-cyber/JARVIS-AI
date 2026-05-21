package com.jarvis.assistant.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import com.jarvis.assistant.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class NotificationListener : NotificationListenerService() {

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

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString(android.app.Notification.EXTRA_TITLE) ?: ""
        val text = extras.getString(android.app.Notification.EXTRA_TEXT) ?: ""
        val bigText = extras.getString(android.app.Notification.EXTRA_BIG_TEXT) ?: ""

        // Don't announce our own notifications
        if (packageName == packageName) return

        // Extract readable content
        val content = buildString {
            if (title.isNotBlank()) append("$title. ")
            if (bigText.isNotBlank()) append(bigText)
            else if (text.isNotBlank()) append(text)
        }

        if (content.isNotBlank()) {
            onNotificationReceived(packageName, content)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {}

    private fun onNotificationReceived(packageName: String, content: String) {
        // Store in a buffer for reading later
        recentNotifications.add(
            NotificationData(
                packageName = packageName,
                content = content,
                timestamp = System.currentTimeMillis()
            )
        )

        // Keep only last 50 notifications
        if (recentNotifications.size > 50) {
            recentNotifications.removeAt(0)
        }
    }

    fun getRecentNotifications(): List<NotificationData> {
        return recentNotifications.toList().reversed()
    }

    fun getFormattedNotifications(): String {
        val notifications = getRecentNotifications().take(10)
        if (notifications.isEmpty()) return "No recent notifications"

        return notifications.joinToString("\n") { notification ->
            val appName = getAppName(notification.packageName)
            "[$appName] ${notification.content}"
        }
    }

    fun speakNotifications() {
        val text = getFormattedNotifications()
        if (text.isNotBlank()) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "notifications")
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    override fun onDestroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onDestroy()
    }

    data class NotificationData(
        val packageName: String,
        val content: String,
        val timestamp: Long
    )

    companion object {
        private val recentNotifications = mutableListOf<NotificationData>()
    }
}
