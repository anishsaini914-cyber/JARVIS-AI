package com.jarvis.assistant.utils

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.os.Build
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class SpeechUtils(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var ttsInitialized = false
    private var sttInitialized = false

    // TextToSpeech
    fun initTts(callback: (Boolean) -> Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            ttsInitialized = status == TextToSpeech.SUCCESS
            if (ttsInitialized) {
                textToSpeech?.language = Locale.getDefault()
            }
            callback(ttsInitialized)
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!ttsInitialized) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val utteranceId = "jarvis_${System.currentTimeMillis()}"
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    if (utteranceId == utteranceId) {
                        onDone?.invoke()
                    }
                }
                override fun onError(utteranceId: String?) {}
            })
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
            onDone?.invoke()
        }
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
    }

    fun isTtsSpeaking(): Boolean {
        return textToSpeech?.isSpeaking ?: false
    }

    // SpeechRecognizer
    fun initStt(listener: RecognitionListener) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(listener)
            sttInitialized = true
        }
    }

    fun startListening(language: String = "en-US") {
        if (!sttInitialized) return
        val intent = RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        RecognizerIntent.EXTRA_LANGUAGE_MODEL?.let {
            val intentObj = RecognizerIntent()
            // Using bundle approach
            val bundle = Bundle().apply {
                putString(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putString(RecognizerIntent.EXTRA_LANGUAGE, language)
                putString(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putInt(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putBoolean(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(bundle)
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun cancelListening() {
        speechRecognizer?.cancel()
    }

    fun destroy() {
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        speechRecognizer = null
        textToSpeech = null
    }
}
