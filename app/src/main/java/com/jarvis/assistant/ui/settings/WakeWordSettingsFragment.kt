package com.jarvis.assistant.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.jarvis.assistant.databinding.FragmentWakeWordSettingsBinding
import com.jarvis.assistant.utils.Constants
import com.jarvis.assistant.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WakeWordSettingsFragment : Fragment() {

    @Inject
    lateinit var prefs: PreferencesManager

    private var _binding: FragmentWakeWordSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWakeWordSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        binding.switchWakeWord.isChecked = prefs.getBoolean(Constants.PREF_WAKE_WORD_ENABLED, false)

        val wakeWord = prefs.getString(Constants.PREF_WAKE_WORD, Constants.DEFAULT_WAKE_WORD)
        when (wakeWord) {
            "hey_jarvis" -> binding.radioHeyJarvis.isChecked = true
            "hello_jarvis" -> binding.radioHelloJarvis.isChecked = true
            "custom" -> {
                binding.radioCustom.isChecked = true
                binding.etCustomWakeWord.visibility = View.VISIBLE
                binding.etCustomWakeWord.setText(prefs.getString("custom_wake_word_text", ""))
            }
        }
    }

    private fun setupListeners() {
        binding.switchWakeWord.setOnCheckedChangeListener { _, isChecked ->
            prefs.saveBoolean(Constants.PREF_WAKE_WORD_ENABLED, isChecked)
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val wakeWord = when (checkedId) {
                binding.radioHeyJarvis.id -> {
                    binding.etCustomWakeWord.visibility = View.GONE
                    "hey_jarvis"
                }
                binding.radioHelloJarvis.id -> {
                    binding.etCustomWakeWord.visibility = View.GONE
                    "hello_jarvis"
                }
                binding.radioCustom.id -> {
                    binding.etCustomWakeWord.visibility = View.VISIBLE
                    "custom"
                }
                else -> "hey_jarvis"
            }
            prefs.saveString(Constants.PREF_WAKE_WORD, wakeWord)
        }

        binding.btnSaveCustom.setOnClickListener {
            val customText = binding.etCustomWakeWord.text.toString().trim()
            if (customText.isNotEmpty()) {
                prefs.saveString("custom_wake_word_text", customText)
                Snackbar.make(binding.root, "Custom wake word saved", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
