package com.jarvis.assistant.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jarvis.assistant.BuildConfig
import com.jarvis.assistant.R
import com.jarvis.assistant.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding.tvVersion.text = "Version ${BuildConfig.VERSION_NAME}"
        binding.tvDeveloperName.text = getString(R.string.about_developer_name)
        binding.tvEmail.text = getString(R.string.about_email)

        binding.btnEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:anishsaini939@gmail.com")
            }
            startActivity(Intent.createChooser(intent, "Send Email"))
        }

        binding.btnPrivacy.setOnClickListener {
            // Open privacy page
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
