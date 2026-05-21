package com.jarvis.assistant.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jarvis.assistant.databinding.FragmentPermissionsBinding
import com.jarvis.assistant.utils.PermissionUtils

class PermissionsFragment : Fragment() {

    private var _binding: FragmentPermissionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        updatePermissionStatus()
    }

    private fun setupListeners() {
        binding.btnMicrophone.setOnClickListener {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 100)
        }

        binding.btnOverlay.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${requireContext().packageName}")
                )
                startActivity(intent)
            }
        }

        binding.btnNotifications.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        binding.btnNotificationListener.setOnClickListener {
            PermissionUtils.openNotificationListenerSettings(requireContext())
        }

        binding.btnPhone.setOnClickListener {
            requestPermissions(PermissionUtils.PHONE_PERMISSIONS, 102)
        }

        binding.btnLocation.setOnClickListener {
            requestPermissions(PermissionUtils.LOCATION_PERMISSIONS, 103)
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        binding.ivMicrophoneStatus.setImageResource(
            if (PermissionUtils.hasMicrophonePermission(requireContext()))
                android.R.drawable.presence_online
            else
                android.R.drawable.presence_offline
        )

        binding.ivOverlayStatus.setImageResource(
            if (PermissionUtils.hasOverlayPermission(requireContext()))
                android.R.drawable.presence_online
            else
                android.R.drawable.presence_offline
        )

        binding.ivNotificationsStatus.setImageResource(
            if (PermissionUtils.hasNotificationPermission(requireContext()))
                android.R.drawable.presence_online
            else
                android.R.drawable.presence_offline
        )

        binding.ivNotifListenerStatus.setImageResource(
            if (PermissionUtils.hasNotificationListenerPermission(requireContext()))
                android.R.drawable.presence_online
            else
                android.R.drawable.presence_offline
        )

        binding.ivPhoneStatus.setImageResource(
            if (PermissionUtils.hasPhonePermissions(requireContext()))
                android.R.drawable.presence_online
            else
                android.R.drawable.presence_offline
        )

        binding.ivLocationStatus.setImageResource(
            if (PermissionUtils.hasLocationPermissions(requireContext()))
                android.R.drawable.presence_online
            else
                android.R.drawable.presence_offline
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
