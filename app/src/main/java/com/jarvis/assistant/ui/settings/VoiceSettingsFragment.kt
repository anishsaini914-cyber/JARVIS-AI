package com.jarvis.assistant.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jarvis.assistant.databinding.FragmentVoiceSettingsBinding
import com.jarvis.assistant.utils.Constants
import com.jarvis.assistant.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VoiceSettingsFragment : Fragment() {

    @Inject
    lateinit var prefs: PreferencesManager

    private var _binding: FragmentVoiceSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoiceSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        binding.switchTts.isChecked = prefs.getBoolean(Constants.PREF_TTS_ENABLED, true)
        binding.switchCallHandling.isChecked = prefs.getBoolean(Constants.PREF_CALL_HANDLING_ENABLED, false)

        val voice = prefs.getString(Constants.PREF_TTS_VOICE, "default")
        when (voice) {
            "default" -> binding.radioDefaultVoice.isChecked = true
            "assistant1" -> binding.radioAssistant1.isChecked = true
            "assistant2" -> binding.radioAssistant2.isChecked = true
        }
    }

    private fun setupListeners() {
        binding.switchTts.setOnCheckedChangeListener { _, isChecked ->
            prefs.saveBoolean(Constants.PREF_TTS_ENABLED, isChecked)
        }

        binding.switchCallHandling.setOnCheckedChangeListener { _, isChecked ->
            prefs.saveBoolean(Constants.PREF_CALL_HANDLING_ENABLED, isChecked)
        }

        binding.radioGroupVoice.setOnCheckedChangeListener { _, checkedId ->
            val voice = when (checkedId) {
                binding.radioDefaultVoice.id -> "default"
                binding.radioAssistant1.id -> "assistant1"
                binding.radioAssistant2.id -> "assistant2"
                else -> "default"
            }
            prefs.saveString(Constants.PREF_TTS_VOICE, voice)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
