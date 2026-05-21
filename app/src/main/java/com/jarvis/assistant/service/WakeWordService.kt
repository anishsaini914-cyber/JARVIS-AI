package com.jarvis.assistant.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.jarvis.assistant.JarvisApplication
import com.jarvis.assistant.R
import com.jarvis.assistant.ui.voice.VoiceActivity
import com.jarvis.assistant.utils.Constants
import com.jarvis.assistant.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class WakeWordService : Service() {

    @Inject
    lateinit var prefs: PreferencesManager

    private var isListening = false
    private var audioRecord: AudioRecord? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var wakeWordJob: Job? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startWakeWordDetection()
            ACTION_STOP -> stopWakeWordDetection()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startWakeWordDetection() {
        if (isListening) return

        val notification = createNotification()
        startForeground(JarvisApplication.WAKE_WORD_NOTIFICATION_ID, notification)
        isListening = true

        wakeWordJob = serviceScope.launch {
            startAudioCapture()
        }
    }

    private fun startAudioCapture() {
        try {
            val sampleRate = Constants.WAKE_WORD_SAMPLE_RATE
            val bufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                stopWakeWordDetection()
                return
            }

            audioRecord?.startRecording()
            val buffer = ShortArray(bufferSize)

            while (isListening && !Thread.currentThread().isInterrupted) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    processAudioBuffer(buffer, read)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopWakeWordDetection()
        }
    }

    private fun processAudioBuffer(buffer: ShortArray, size: Int) {
        // Audio level detection for wake word
        // In production, this would use a proper wake word detection model
        val amplitude = buffer.take(size).map { kotlin.math.abs(it.toInt()) }.average()
        if (amplitude > 5000) {
            // Potential wake word detected - launch voice activity
            launchVoiceActivity()
        }
    }

    private fun launchVoiceActivity() {
        val intent = Intent(this, VoiceActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

    private fun stopWakeWordDetection() {
        isListening = false
        wakeWordJob?.cancel()
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioRecord = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            stopForeground(true)
        }
        stopSelf()
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, VoiceActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, JarvisApplication.CHANNEL_WAKE_WORD)
            .setContentTitle("JARVIS")
            .setContentText("Listening for wake word...")
            .setSmallIcon(R.drawable.ic_mic)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        stopWakeWordDetection()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.jarvis.assistant.action.START_WAKE_WORD"
        const val ACTION_STOP = "com.jarvis.assistant.action.STOP_WAKE_WORD"
    }
}
