package com.jarvis.assistant.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.jarvis.assistant.databinding.FragmentAiProvidersBinding
import com.jarvis.assistant.utils.Constants
import com.jarvis.assistant.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AiProvidersFragment : Fragment() {

    @Inject
    lateinit var prefs: PreferencesManager

    private var _binding: FragmentAiProvidersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiProvidersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        val activeProvider = prefs.getString(Constants.PREF_ACTIVE_PROVIDER, "openai")

        when (activeProvider) {
            "openai" -> binding.radioOpenAi.isChecked = true
            "gemini" -> binding.radioGemini.isChecked = true
            "agentrouter" -> binding.radioAgentRouter.isChecked = true
            "local" -> binding.radioLocal.isChecked = true
        }

        binding.etOpenAiKey.setText(prefs.getApiKey(Constants.PREF_OPENAI_KEY) ?: "")
        binding.etOpenAiModel.setText(
            prefs.getString(Constants.PREF_OPENAI_MODEL, Constants.DEFAULT_OPENAI_MODEL)
        )

        binding.etGeminiKey.setText(prefs.getApiKey(Constants.PREF_GEMINI_KEY) ?: "")
        binding.etGeminiModel.setText(
            prefs.getString(Constants.PREF_GEMINI_MODEL, Constants.DEFAULT_GEMINI_MODEL)
        )

        binding.etAgentRouterKey.setText(prefs.getApiKey(Constants.PREF_AGENT_ROUTER_KEY) ?: "")
        binding.etAgentRouterModel.setText(
            prefs.getString(Constants.PREF_AGENT_ROUTER_MODEL, Constants.DEFAULT_AGENT_ROUTER_MODEL)
        )
        binding.etAgentRouterEndpoint.setText(
            prefs.getString(Constants.PREF_AGENT_ROUTER_ENDPOINT, Constants.AGENT_ROUTER_BASE_URL)
        )

        updateProviderVisibility(activeProvider)
    }

    private fun setupListeners() {
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val provider = when (checkedId) {
                binding.radioOpenAi.id -> "openai"
                binding.radioGemini.id -> "gemini"
                binding.radioAgentRouter.id -> "agentrouter"
                binding.radioLocal.id -> "local"
                else -> "openai"
            }
            prefs.saveString(Constants.PREF_ACTIVE_PROVIDER, provider)
            updateProviderVisibility(provider)
        }

        binding.btnSaveOpenAi.setOnClickListener { saveOpenAiSettings() }
        binding.btnSaveGemini.setOnClickListener { saveGeminiSettings() }
        binding.btnSaveAgentRouter.setOnClickListener { saveAgentRouterSettings() }

        binding.btnShowOpenAiKey.setOnClickListener {
            toggleKeyVisibility(binding.etOpenAiKey)
        }
        binding.btnShowGeminiKey.setOnClickListener {
            toggleKeyVisibility(binding.etGeminiKey)
        }
        binding.btnShowAgentRouterKey.setOnClickListener {
            toggleKeyVisibility(binding.etAgentRouterKey)
        }
    }

    private fun updateProviderVisibility(provider: String) {
        binding.layoutOpenAi.visibility = if (provider == "openai") View.VISIBLE else View.GONE
        binding.layoutGemini.visibility = if (provider == "gemini") View.VISIBLE else View.GONE
        binding.layoutAgentRouter.visibility = if (provider == "agentrouter") View.VISIBLE else View.GONE
        binding.layoutLocal.visibility = if (provider == "local") View.VISIBLE else View.GONE
    }

    private fun saveOpenAiSettings() {
        val key = binding.etOpenAiKey.text.toString().trim()
        if (key.isNotEmpty()) {
            prefs.saveApiKey(Constants.PREF_OPENAI_KEY, key)
        }
        prefs.saveString(
            Constants.PREF_OPENAI_MODEL,
            binding.etOpenAiModel.text.toString().trim().ifEmpty { Constants.DEFAULT_OPENAI_MODEL }
        )
        showSaved()
    }

    private fun saveGeminiSettings() {
        val key = binding.etGeminiKey.text.toString().trim()
        if (key.isNotEmpty()) {
            prefs.saveApiKey(Constants.PREF_GEMINI_KEY, key)
        }
        prefs.saveString(
            Constants.PREF_GEMINI_MODEL,
            binding.etGeminiModel.text.toString().trim().ifEmpty { Constants.DEFAULT_GEMINI_MODEL }
        )
        showSaved()
    }

    private fun saveAgentRouterSettings() {
        val key = binding.etAgentRouterKey.text.toString().trim()
        if (key.isNotEmpty()) {
            prefs.saveApiKey(Constants.PREF_AGENT_ROUTER_KEY, key)
        }
        prefs.saveString(
            Constants.PREF_AGENT_ROUTER_MODEL,
            binding.etAgentRouterModel.text.toString().trim()
                .ifEmpty { Constants.DEFAULT_AGENT_ROUTER_MODEL }
        )
        prefs.saveString(
            Constants.PREF_AGENT_ROUTER_ENDPOINT,
            binding.etAgentRouterEndpoint.text.toString().trim()
                .ifEmpty { Constants.AGENT_ROUTER_BASE_URL }
        )
        showSaved()
    }

    private fun toggleKeyVisibility(editText: android.widget.EditText) {
        val inputType = editText.inputType
        if (inputType == android.text.InputType.TYPE_CLASS_TEXT or
            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        ) {
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        editText.setSelection(editText.text.length)
    }

    private fun showSaved() {
        Snackbar.make(binding.root, "Settings saved", Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
