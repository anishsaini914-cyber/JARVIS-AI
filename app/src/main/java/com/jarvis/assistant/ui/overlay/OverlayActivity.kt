package com.jarvis.assistant.ui.overlay

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.jarvis.assistant.databinding.ActivityOverlayBinding
import com.jarvis.assistant.ui.voice.VoiceActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OverlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOverlayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOverlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnActivateVoice.setOnClickListener {
            val intent = Intent(this, VoiceActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
            finish()
        }

        binding.btnOpenChat.setOnClickListener {
            val intent = Intent(this, com.jarvis.assistant.ui.MainActivity::class.java).apply {
                putExtra("open_chat", true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
            finish()
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }
}
