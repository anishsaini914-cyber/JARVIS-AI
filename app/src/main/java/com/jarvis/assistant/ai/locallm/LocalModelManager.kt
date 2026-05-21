package com.jarvis.assistant.ai.locallm

import android.content.Context
import com.jarvis.assistant.utils.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileFilter
import javax.inject.Inject
import javax.inject.Singleton

data class LocalModel(
    val id: String,
    val name: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val modelType: ModelType,
    val quantization: String = "unknown",
    val contextSize: Int = 2048,
    val isLoaded: Boolean = false
)

enum class ModelType {
    GGUF, GGML, UNKNOWN
}

@Singleton
class LocalModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: PreferencesManager
) {

    private val modelsDir: File
        get() = File(context.filesDir, "models").also { it.mkdirs() }

    fun getModels(): List<LocalModel> {
        val models = mutableListOf<LocalModel>()
        if (!modelsDir.exists()) return models

        modelsDir.listFiles(FileFilter { file ->
            file.extension.lowercase() in listOf("gguf", "ggml", "bin")
        })?.forEach { file ->
            models.add(
                LocalModel(
                    id = file.nameWithoutExtension,
                    name = file.nameWithoutExtension.replace("_", " ").replace("-", " "),
                    fileName = file.name,
                    filePath = file.absolutePath,
                    fileSize = file.length(),
                    modelType = getModelType(file),
                    quantization = detectQuantization(file.name),
                    isLoaded = file.name == prefs.getString("active_local_model")
                )
            )
        }
        return models.sortedByDescending { it.fileSize }
    }

    fun selectModel(modelId: String) {
        val model = getModels().find { it.id == modelId }
        model?.let {
            prefs.saveString("active_local_model", it.fileName)
        }
    }

    fun getActiveModel(): LocalModel? {
        val fileName = prefs.getString("active_local_model")
        if (fileName.isBlank()) return null
        return getModels().find { it.fileName == fileName }
    }

    fun deleteModel(modelId: String): Boolean {
        val model = getModels().find { it.id == modelId } ?: return false
        val file = File(model.filePath)
        return if (file.exists()) {
            file.delete()
        } else false
    }

    fun getModelsDirectory(): File = modelsDir

    fun getAvailableStorage(): Long {
        val stat = android.os.StatFs(modelsDir.path)
        return stat.availableBlocksLong * stat.blockSizeLong
    }

    private fun getModelType(file: File): ModelType {
        return when (file.extension.lowercase()) {
            "gguf" -> ModelType.GGUF
            "ggml" -> ModelType.GGML
            else -> ModelType.UNKNOWN
        }
    }

    private fun detectQuantization(fileName: String): String {
        return when {
            fileName.contains("Q2_") || fileName.contains("q2_") -> "Q2_K"
            fileName.contains("Q3_") || fileName.contains("q3_") -> "Q3_K"
            fileName.contains("Q4_") || fileName.contains("q4_") -> "Q4_K"
            fileName.contains("Q5_") || fileName.contains("q5_") -> "Q5_K"
            fileName.contains("Q6_") || fileName.contains("q6_") -> "Q6_K"
            fileName.contains("Q8_") || fileName.contains("q8_") -> "Q8_0"
            fileName.contains("F16") || fileName.contains("fp16") -> "F16"
            fileName.contains("F32") || fileName.contains("fp32") -> "F32"
            else -> "unknown"
        }
    }
}
