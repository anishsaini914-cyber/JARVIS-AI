package com.jarvis.assistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val prefs = context.getSharedPreferences("jarvis_prefs", Context.MODE_PRIVATE)
            startServiceIfEnabled(context, prefs)
        }
    }

    private fun startServiceIfEnabled(context: Context, prefs: SharedPreferences) {
        if (prefs.getBoolean("wake_word_enabled", false)) {
            val wakeIntent = Intent(context, WakeWordService::class.java).apply {
                action = WakeWordService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(wakeIntent)
            } else {
                context.startService(wakeIntent)
            }
        }

        if (prefs.getBoolean("overlay_enabled", false)) {
            val overlayIntent = Intent(context, FloatingOverlayService::class.java).apply {
                action = FloatingOverlayService.ACTION_SHOW
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(overlayIntent)
            } else {
                context.startService(overlayIntent)
            }
        }

        val bgIntent = Intent(context, BackgroundService::class.java).apply {
            action = BackgroundService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(bgIntent)
        } else {
            context.startService(bgIntent)
        }
    }
}
