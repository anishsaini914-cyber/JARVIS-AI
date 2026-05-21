package com.jarvis.assistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_OPEN_CHAT -> {
                val chatIntent = Intent(context, com.jarvis.assistant.ui.MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra("open_chat", true)
                }
                context.startActivity(chatIntent)
            }
            ACTION_VOICE_INPUT -> {
                val voiceIntent = Intent(context, com.jarvis.assistant.ui.voice.VoiceActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(voiceIntent)
            }
        }
    }

    companion object {
        const val ACTION_OPEN_CHAT = "com.jarvis.assistant.action.OPEN_CHAT"
        const val ACTION_VOICE_INPUT = "com.jarvis.assistant.action.VOICE_INPUT"
    }
}
