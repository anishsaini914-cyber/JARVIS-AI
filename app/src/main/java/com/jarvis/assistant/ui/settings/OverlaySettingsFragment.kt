package com.jarvis.assistant.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jarvis.assistant.databinding.FragmentOverlaySettingsBinding
import com.jarvis.assistant.utils.Constants
import com.jarvis.assistant.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OverlaySettingsFragment : Fragment() {

    @Inject
    lateinit var prefs: PreferencesManager

    private var _binding: FragmentOverlaySettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOverlaySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        binding.switchEnable.isChecked = prefs.getBoolean(Constants.PREF_OVERLAY_ENABLED, false)
        binding.sliderOpacity.setProgress((prefs.getFloat("overlay_opacity", 0.8f) * 100).toInt())
        binding.sliderSize.setProgress((prefs.getFloat("overlay_size", 1.0f) * 50).toInt())
    }

    private fun setupListeners() {
        binding.switchEnable.setOnCheckedChangeListener { _, isChecked ->
            prefs.saveBoolean(Constants.PREF_OVERLAY_ENABLED, isChecked)
        }

        binding.sliderOpacity.addOnChangeListener { _, value, _ ->
            binding.tvOpacityValue.text = "${value.toInt()}%"
            prefs.saveFloat("overlay_opacity", value / 100f)
        }

        binding.sliderSize.addOnChangeListener { _, value, _ ->
            binding.tvSizeValue.text = "${value.toInt() + 50}%"
            prefs.saveFloat("overlay_size", (value + 50) / 100f)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
