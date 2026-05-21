package com.jarvis.assistant.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.core.app.NotificationCompat
import com.jarvis.assistant.JarvisApplication
import com.jarvis.assistant.R
import com.jarvis.assistant.ui.voice.VoiceActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var isExpanded = false

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> showOverlay()
            ACTION_HIDE -> hideOverlay()
            ACTION_TOGGLE -> toggleOverlay()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showOverlay() {
        if (::overlayView.isInitialized) return

        val notification = createNotification()
        startForeground(JarvisApplication.OVERLAY_NOTIFICATION_ID, notification)

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_floating_bubble, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        windowManager.addView(overlayView, params)

        setupOverlayListeners(params)
    }

    private fun setupOverlayListeners(params: WindowManager.LayoutParams) {
        val bubbleView = overlayView.findViewById<View>(R.id.overlayBubble)
        val expandedPanel = overlayView.findViewById<View>(R.id.overlayExpandedPanel)

        bubbleView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (Math.abs(dx) < 10 && Math.abs(dy) < 10) {
                        toggleExpanded()
                    }
                    true
                }
                else -> false
            }
        }

        overlayView.findViewById<View>(R.id.btnOverlayMic)?.setOnClickListener {
            launchVoiceActivity()
        }

        overlayView.findViewById<View>(R.id.btnOverlayClose)?.setOnClickListener {
            hideOverlay()
        }

        overlayView.findViewById<View>(R.id.btnOverlayChat)?.setOnClickListener {
            launchMainActivity()
        }
    }

    private fun toggleExpanded() {
        isExpanded = !isExpanded
        val expandedPanel = overlayView.findViewById<View>(R.id.overlayExpandedPanel)
        expandedPanel?.visibility = if (isExpanded) View.VISIBLE else View.GONE
        if (isExpanded) {
            expandedPanel?.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.fade_in)
            )
        }
    }

    private fun launchVoiceActivity() {
        val intent = Intent(this, VoiceActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun launchMainActivity() {
        val intent = Intent(this, com.jarvis.assistant.ui.MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun hideOverlay() {
        if (::overlayView.isInitialized) {
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            stopForeground(true)
        }
        stopSelf()
    }

    private fun toggleOverlay() {
        if (::overlayView.isInitialized) {
            hideOverlay()
        } else {
            showOverlay()
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, com.jarvis.assistant.ui.MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, JarvisApplication.CHANNEL_OVERLAY)
            .setContentTitle("JARVIS Assistant")
            .setContentText("Floating assistant active")
            .setSmallIcon(R.drawable.ic_jarvis_ai)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        if (::overlayView.isInitialized) {
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onDestroy()
    }

    companion object {
        const val ACTION_SHOW = "com.jarvis.assistant.action.SHOW_OVERLAY"
        const val ACTION_HIDE = "com.jarvis.assistant.action.HIDE_OVERLAY"
        const val ACTION_TOGGLE = "com.jarvis.assistant.action.TOGGLE_OVERLAY"
    }
}
