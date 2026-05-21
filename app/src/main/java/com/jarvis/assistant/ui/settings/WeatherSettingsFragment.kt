package com.jarvis.assistant.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.jarvis.assistant.databinding.FragmentWeatherSettingsBinding
import com.jarvis.assistant.utils.Constants
import com.jarvis.assistant.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WeatherSettingsFragment : Fragment() {

    @Inject
    lateinit var prefs: PreferencesManager

    private var _binding: FragmentWeatherSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        binding.switchWeather.isChecked = prefs.getBoolean(Constants.PREF_WEATHER_ENABLED, false)

        val unit = prefs.getString(Constants.PREF_WEATHER_UNIT, "celsius")
        binding.radioCelsius.isChecked = unit == "celsius"
        binding.radioFahrenheit.isChecked = unit == "fahrenheit"

        val lat = prefs.getFloat(Constants.PREF_WEATHER_LAT, 0f)
        val lon = prefs.getFloat(Constants.PREF_WEATHER_LON, 0f)
        if (lat != 0f || lon != 0f) {
            binding.etLatitude.setText(lat.toString())
            binding.etLongitude.setText(lon.toString())
        }
    }

    private fun setupListeners() {
        binding.switchWeather.setOnCheckedChangeListener { _, isChecked ->
            prefs.saveBoolean(Constants.PREF_WEATHER_ENABLED, isChecked)
        }

        binding.radioGroupUnit.setOnCheckedChangeListener { _, checkedId ->
            val unit = if (checkedId == binding.radioCelsius.id) "celsius" else "fahrenheit"
            prefs.saveString(Constants.PREF_WEATHER_UNIT, unit)
        }

        binding.btnSaveLocation.setOnClickListener {
            val lat = binding.etLatitude.text.toString().toFloatOrNull()
            val lon = binding.etLongitude.text.toString().toFloatOrNull()
            if (lat != null && lon != null) {
                prefs.saveFloat(Constants.PREF_WEATHER_LAT, lat)
                prefs.saveFloat(Constants.PREF_WEATHER_LON, lon)
                Snackbar.make(binding.root, "Location saved", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.root, "Enter valid coordinates", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.btnUseCurrentLocation.setOnClickListener {
            // Would use FusedLocationProviderClient in production
            Snackbar.make(binding.root, "Location service not available", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
