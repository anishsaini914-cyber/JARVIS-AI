package com.jarvis.assistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class JarvisApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val voiceChannel = NotificationChannel(
                CHANNEL_VOICE,
                "Voice Assistant",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Voice assistant notifications"
                setShowBadge(false)
                enableVibration(true)
            }

            val overlayChannel = NotificationChannel(
                CHANNEL_OVERLAY,
                "Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Floating overlay service"
                setShowBadge(false)
            }

            val wakeWordChannel = NotificationChannel(
                CHANNEL_WAKE_WORD,
                "Wake Word Detection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Wake word detection service"
                setShowBadge(false)
            }

            val callChannel = NotificationChannel(
                CHANNEL_CALL,
                "Call Handling",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Call handling notifications"
                enableVibration(true)
                setSound(null, null)
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "JARVIS",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications"
            }

            manager.createNotificationChannel(voiceChannel)
            manager.createNotificationChannel(overlayChannel)
            manager.createNotificationChannel(wakeWordChannel)
            manager.createNotificationChannel(callChannel)
            manager.createNotificationChannel(generalChannel)
        }
    }

    companion object {
        const val CHANNEL_VOICE = "jarvis_channel_voice"
        const val CHANNEL_OVERLAY = "jarvis_channel_overlay"
        const val CHANNEL_WAKE_WORD = "jarvis_channel_wake_word"
        const val CHANNEL_CALL = "jarvis_channel_call"
        const val CHANNEL_GENERAL = "jarvis_channel_general"

        const val FOREGROUND_NOTIFICATION_ID = 1001
        const val OVERLAY_NOTIFICATION_ID = 1002
        const val WAKE_WORD_NOTIFICATION_ID = 1003
        const val CALL_NOTIFICATION_ID = 1004
    }
}
