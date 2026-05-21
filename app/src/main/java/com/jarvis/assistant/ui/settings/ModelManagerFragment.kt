package com.jarvis.assistant.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.jarvis.assistant.ai.locallm.LocalModel
import com.jarvis.assistant.ai.locallm.LocalModelManager
import com.jarvis.assistant.databinding.FragmentModelManagerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ModelManagerFragment : Fragment() {

    @Inject
    lateinit var modelManager: LocalModelManager

    private var _binding: FragmentModelManagerBinding? = null
    private val binding get() = _binding!!

    private val importFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { importModel(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModelManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        loadModels()
    }

    private fun setupListeners() {
        binding.btnImportModel.setOnClickListener {
            importFileLauncher.launch("*/*")
        }

        binding.btnRefresh.setOnClickListener {
            loadModels()
        }
    }

    private fun loadModels() {
        lifecycleScope.launch {
            val models = modelManager.getModels()
            binding.tvModelCount.text = "${models.size} model(s) loaded"
            binding.tvStorageAvailable.text = formatSize(modelManager.getAvailableStorage())

            if (models.isEmpty()) {
                binding.tvNoModels.visibility = View.VISIBLE
                binding.modelList.visibility = View.GONE
            } else {
                binding.tvNoModels.visibility = View.GONE
                binding.modelList.visibility = View.VISIBLE
                displayModels(models)
            }
        }
    }

    private fun displayModels(models: List<LocalModel>) {
        binding.modelList.removeAllViews()
        models.forEach { model ->
            val card = createModelCard(model)
            binding.modelList.addView(card)
        }
    }

    private fun createModelCard(model: LocalModel): View {
        val card = com.google.android.material.card.MaterialCardView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 12, 16, 12)
            radius = 12f
            setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), com.jarvis.assistant.R.color.background_card)
            )
            strokeColor = ContextCompat.getColor(
                requireContext(),
                if (model.isLoaded) com.jarvis.assistant.R.color.accent else com.jarvis.assistant.R.color.divider
            )
            strokeWidth = if (model.isLoaded) 2 else 0
            setOnClickListener {
                modelManager.selectModel(model.id)
                loadModels()
                Snackbar.make(binding.root, "Selected ${model.name}", Snackbar.LENGTH_SHORT).show()
            }
        }

        val layout = androidx.constraintlayout.widget.ConstraintLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val nameText = android.widget.TextView(requireContext()).apply {
            text = model.name
            setTextColor(ContextCompat.getColor(requireContext(), com.jarvis.assistant.R.color.text_primary))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        nameText.id = View.generateViewId()
        layout.addView(nameText)

        val infoText = android.widget.TextView(requireContext()).apply {
            text = "${model.modelType.name} | ${model.quantization} | ${formatSize(model.fileSize)}"
            setTextColor(ContextCompat.getColor(requireContext(), com.jarvis.assistant.R.color.text_secondary))
            textSize = 12f
        }
        infoText.id = View.generateViewId()
        layout.addView(infoText)

        // Constraint layout params
        (nameText.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams).apply {
            startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            setMargins(0, 0, 0, 4)
        }

        (infoText.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams).apply {
            startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            topToBottom = nameText.id
            bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
        }

        card.addView(layout)
        return card
    }

    private fun importModel(uri: Uri) {
        lifecycleScope.launch {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val fileName = getFileName(uri) ?: "model.gguf"
                val outputFile = File(modelManager.getModelsDirectory(), fileName)

                inputStream?.use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                Snackbar.make(binding.root, "Model imported: $fileName", Snackbar.LENGTH_LONG).show()
                loadModels()
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Import failed: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) it.getString(nameIndex) else null
            } else null
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${String.format("%.1f", bytes / (1024.0 * 1024.0))} MB"
            else -> "${String.format("%.2f", bytes / (1024.0 * 1024.0 * 1024.0))} GB"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
