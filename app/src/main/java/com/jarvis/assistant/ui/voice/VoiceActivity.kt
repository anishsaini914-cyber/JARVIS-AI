package com.jarvis.assistant.ui.voice

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jarvis.assistant.R
import com.jarvis.assistant.ai.AiResult
import com.jarvis.assistant.data.repository.VoiceRepository
import com.jarvis.assistant.databinding.ActivityVoiceBinding
import com.jarvis.assistant.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VoiceActivity : AppCompatActivity() {

    @Inject
    lateinit var voiceRepository: VoiceRepository

    @Inject
    lateinit var prefs: PreferencesManager

    private lateinit var binding: ActivityVoiceBinding
    private var speechRecognizer: SpeechRecognizer? = null
    private var speechUtils: SpeechUtils? = null
    private var isListening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )

        setupSpeech()
        setupListeners()
    }

    private fun setupSpeech() {
        speechUtils = SpeechUtils(this)

        speechUtils?.initTts { ready ->
            if (ready && prefs.getBoolean(Constants.PREF_TTS_ENABLED, true)) {
                speechUtils?.speak("I'm listening")
            }
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                binding.tvStatus.text = getString(R.string.listening)
                binding.ivWave.animate().scaleX(1.2f).scaleY(1.2f).setDuration(500).start()
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                binding.tvStatus.text = getString(R.string.processing)
            }

            override fun onError(error: Int) {
                isListening = false
                val message = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "I didn't catch that"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                    else -> "Try again"
                }
                binding.tvStatus.text = message
                binding.ivWave.animate().scaleX(1f).scaleY(1f).setDuration(500).start()
                delayAndFinish()
            }

            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (text != null) {
                    processVoiceInput(text)
                } else {
                    binding.tvStatus.text = getString(R.string.voice_not_recognized)
                    delayAndFinish()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                binding.tvVoiceText.text = text ?: ""
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnMic.setOnClickListener {
            if (isListening) {
                stopListening()
            } else {
                startListening()
            }
        }

        binding.btnSwitchToChat.setOnClickListener {
            val intent = Intent(this, com.jarvis.assistant.ui.MainActivity::class.java).apply {
                putExtra("open_chat", true)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun startListening() {
        if (!PermissionUtils.hasMicrophonePermission(this)) {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 100)
            return
        }

        val intent = android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        val bundle = android.os.Bundle().apply {
            putString(
                android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putString(
                android.speech.RecognizerIntent.EXTRA_LANGUAGE,
                java.util.Locale.getDefault().toLanguageTag()
            )
            putInt(android.speech.RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putBoolean(android.speech.RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(bundle)
        binding.btnMic.setImageResource(android.R.drawable.ic_media_pause)
    }

    private fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        binding.btnMic.setImageResource(android.R.drawable.ic_btn_speak_now)
    }

    private fun processVoiceInput(text: String) {
        binding.tvVoiceText.text = text
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.tvStatus.text = getString(R.string.processing)

        lifecycleScope.launch {
            val result = voiceRepository.processVoiceCommand(text)

            binding.progressBar.visibility = android.view.View.GONE

            when (result) {
                is AiResult.Success -> {
                    binding.tvResponse.text = result.response
                    if (prefs.getBoolean(Constants.PREF_TTS_ENABLED, true)) {
                        speechUtils?.speak(result.response) {
                            delayAndFinish()
                        }
                    } else {
                        delayAndFinish()
                    }
                }
                is AiResult.Error -> {
                    binding.tvResponse.text = "Error: ${result.message}"
                    binding.tvStatus.text = "Error occurred"
                    delayAndFinish()
                }
            }
        }
    }

    private fun delayAndFinish() {
        lifecycleScope.launch {
            delay(2000)
            if (!isFinishing) {
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        speechRecognizer?.stopListening()
        speechUtils?.stopSpeaking()
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        speechUtils?.destroy()
        super.onDestroy()
    }
}
