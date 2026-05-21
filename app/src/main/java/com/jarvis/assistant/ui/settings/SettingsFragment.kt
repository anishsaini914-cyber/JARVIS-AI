package com.jarvis.assistant.ui.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.jarvis.assistant.R
import com.jarvis.assistant.databinding.FragmentSettingsBinding
import com.jarvis.assistant.service.BackgroundService
import com.jarvis.assistant.service.FloatingOverlayService
import com.jarvis.assistant.service.WakeWordService
import com.jarvis.assistant.utils.Constants
import com.jarvis.assistant.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var prefs: PreferencesManager

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        loadSettings()
    }

    private fun setupListeners() {
        binding.btnAiProviders.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_ai_providers)
        }

        binding.btnWakeWord.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_wake_word)
        }

        binding.btnVoice.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_voice_settings)
        }

        binding.btnOverlay.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_overlay_settings)
        }

        binding.btnAbout.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_about)
        }

        binding.btnPermissions.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_permissions)
        }

        binding.btnModelManager.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_model_manager)
        }

        binding.btnWeather.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_weather_settings)
        }

        binding.switchWakeWord.setOnCheckedChangeListener { _, isChecked ->
            prefs.saveBoolean(Constants.PREF_WAKE_WORD_ENABLED, isChecked)
            if (isChecked) {
                startService(Intent(requireContext(), WakeWordService::class.java).apply {
                    action = WakeWordService.ACTION_START
                })
            } else {
                startService(Intent(requireContext(), WakeWordService::class.java).apply {
                    action = WakeWordService.ACTION_STOP
                })
            }
        }

        binding.switchOverlay.setOnCheckedChangeListener { _, isChecked ->
            prefs.saveBoolean(Constants.PREF_OVERLAY_ENABLED, isChecked)
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !Settings.canDrawOverlays(requireContext())
                ) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:${requireContext().packageName}")
                    )
                    startActivity(intent)
                    binding.switchOverlay.isChecked = false
                } else {
                    startService(Intent(requireContext(), FloatingOverlayService::class.java).apply {
                        action = FloatingOverlayService.ACTION_SHOW
                    })
                }
            } else {
                startService(Intent(requireContext(), FloatingOverlayService::class.java).apply {
                    action = FloatingOverlayService.ACTION_HIDE
                })
            }
        }

        binding.switchTts.setOnCheckedChangeListener { _, isChecked ->
            prefs.saveBoolean(Constants.PREF_TTS_ENABLED, isChecked)
        }
    }

    private fun loadSettings() {
        binding.switchWakeWord.isChecked = prefs.getBoolean(Constants.PREF_WAKE_WORD_ENABLED, false)
        binding.switchOverlay.isChecked = prefs.getBoolean(Constants.PREF_OVERLAY_ENABLED, false)
        binding.switchTts.isChecked = prefs.getBoolean(Constants.PREF_TTS_ENABLED, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
