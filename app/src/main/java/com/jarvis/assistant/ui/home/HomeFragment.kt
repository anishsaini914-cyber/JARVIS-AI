package com.jarvis.assistant.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.jarvis.assistant.R
import com.jarvis.assistant.databinding.FragmentHomeBinding
import com.jarvis.assistant.service.BackgroundService
import com.jarvis.assistant.service.FloatingOverlayService
import com.jarvis.assistant.service.WakeWordService
import com.jarvis.assistant.ui.voice.VoiceActivity
import com.jarvis.assistant.utils.AppUtils
import com.jarvis.assistant.utils.Constants
import com.jarvis.assistant.utils.PermissionUtils
import com.jarvis.assistant.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var prefs: PreferencesManager

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        // Set greeting
        binding.tvGreeting.text = AppUtils.getGreeting()
        binding.tvTapToSpeak.text = getString(R.string.tap_to_speak)

        // Animate elements
        binding.ivJarvisLogo.startAnimation(
            AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        )
        binding.tvGreeting.startAnimation(
            AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        )

        // Quick action cards
        binding.cardChat.setOnClickListener {
            navigateTo(R.id.navigation_chat)
        }

        binding.cardVoice.setOnClickListener {
            launchVoiceActivity()
        }

        binding.cardSettings.setOnClickListener {
            navigateTo(R.id.navigation_settings)
        }

        binding.micButton.setOnClickListener {
            if (PermissionUtils.hasMicrophonePermission(requireContext())) {
                launchVoiceActivity()
            } else {
                requestPermissions()
            }
        }

        // Quick actions
        binding.quickActionWeather.setOnClickListener {
            navigateTo(R.id.navigation_chat)
        }

        binding.quickActionFlashlight.setOnClickListener {
            AppUtils.toggleFlashlight(requireContext())
        }

        binding.quickActionBattery.setOnClickListener {
            val batteryInfo = AppUtils.getBatteryInfo(requireContext())
            binding.tvStatus.text = batteryInfo
        }

        binding.quickActionSearch.setOnClickListener {
            navigateTo(R.id.navigation_chat)
        }
    }

    private fun navigateTo(destination: Int) {
        try {
            requireActivity().supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment)?.let { navHost ->
                    val navController = navHost.childFragmentManager.fragments
                        .firstOrNull()?.let { (it as? androidx.navigation.fragment.NavHostFragment) }
                    navController?.navController?.navigate(destination)
                }
        } catch (e: Exception) {
            // Fallback
        }
    }

    private fun launchVoiceActivity() {
        val intent = Intent(requireContext(), VoiceActivity::class.java)
        startActivity(intent)
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissions(permissions.toTypedArray(), 100)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
