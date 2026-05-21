package com.jarvis.assistant.ai

import com.jarvis.assistant.data.remote.api.AgentRouterApi
import com.jarvis.assistant.data.remote.api.GeminiApi
import com.jarvis.assistant.data.remote.api.OpenAiApi
import com.jarvis.assistant.data.remote.model.ChatCompletionRequest
import com.jarvis.assistant.data.remote.model.ChatMessage
import com.jarvis.assistant.data.remote.model.GeminiContent
import com.jarvis.assistant.data.remote.model.GeminiPart
import com.jarvis.assistant.data.remote.model.GeminiRequest
import com.jarvis.assistant.data.remote.model.AgentRouterRequest
import com.jarvis.assistant.data.remote.model.AgentRouterMessage
import com.jarvis.assistant.utils.Constants
import com.jarvis.assistant.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class AiResult {
    data class Success(val response: String, val provider: String, val model: String) : AiResult()
    data class Error(val message: String) : AiResult()
}

interface AiProvider {
    suspend fun generateResponse(messages: List<ChatMessage>, model: String?): AiResult
    fun getProviderName(): String
}

@Singleton
class OpenAiProvider @Inject constructor(
    private val api: OpenAiApi,
    private val prefs: PreferencesManager
) : AiProvider {

    override suspend fun generateResponse(messages: List<ChatMessage>, model: String?): AiResult {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = prefs.getApiKey(Constants.PREF_OPENAI_KEY)
                    ?: return@withContext AiResult.Error("OpenAI API key not configured")

                val selectedModel = model ?: prefs.getString(
                    Constants.PREF_OPENAI_MODEL,
                    Constants.DEFAULT_OPENAI_MODEL
                )

                val request = ChatCompletionRequest(
                    model = selectedModel,
                    messages = messages
                )

                val response = api.createChatCompletion(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    val content = body?.choices?.firstOrNull()?.message?.content
                    if (content != null) {
                        AiResult.Success(content, "openai", selectedModel)
                    } else {
                        AiResult.Error("Empty response from OpenAI")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    AiResult.Error(errorBody ?: "Unknown error from OpenAI")
                }
            } catch (e: Exception) {
                AiResult.Error("Network error: ${e.message}")
            }
        }
    }

    override fun getProviderName() = "openai"
}

@Singleton
class GeminiProvider @Inject constructor(
    private val api: GeminiApi,
    private val prefs: PreferencesManager
) : AiProvider {

    override suspend fun generateResponse(messages: List<ChatMessage>, model: String?): AiResult {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = prefs.getApiKey(Constants.PREF_GEMINI_KEY)
                    ?: return@withContext AiResult.Error("Gemini API key not configured")

                val selectedModel = model ?: prefs.getString(
                    Constants.PREF_GEMINI_MODEL,
                    Constants.DEFAULT_GEMINI_MODEL
                )

                // Convert messages to Gemini format - take the last user message
                val lastUserMessage = messages.lastOrNull { it.role == "user" }
                    ?: return@withContext AiResult.Error("No user message found")

                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = lastUserMessage.content))
                        )
                    )
                )

                val response = api.generateContent(selectedModel, request, apiKey)
                if (response.isSuccessful) {
                    val body = response.body()
                    val text = body?.candidates?.firstOrNull()
                        ?.content?.parts?.firstOrNull()?.text
                    if (text != null) {
                        AiResult.Success(text, "gemini", selectedModel)
                    } else {
                        AiResult.Error("Empty response from Gemini")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    AiResult.Error(errorBody ?: "Unknown error from Gemini")
                }
            } catch (e: Exception) {
                AiResult.Error("Network error: ${e.message}")
            }
        }
    }

    override fun getProviderName() = "gemini"
}

@Singleton
class AgentRouterProvider @Inject constructor(
    private val api: AgentRouterApi,
    private val prefs: PreferencesManager
) : AiProvider {

    override suspend fun generateResponse(messages: List<ChatMessage>, model: String?): AiResult {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = prefs.getApiKey(Constants.PREF_AGENT_ROUTER_KEY)
                    ?: return@withContext AiResult.Error("AgentRouter API key not configured")

                val selectedModel = model ?: prefs.getString(
                    Constants.PREF_AGENT_ROUTER_MODEL,
                    Constants.DEFAULT_AGENT_ROUTER_MODEL
                )

                val routerMessages = messages.map { msg ->
                    AgentRouterMessage(role = msg.role, content = msg.content)
                }

                val request = AgentRouterRequest(
                    messages = routerMessages,
                    model = selectedModel
                )

                val response = api.createChatCompletion(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    val content = body?.choices?.firstOrNull()?.message?.content
                    if (content != null) {
                        AiResult.Success(content, "agentrouter", selectedModel)
                    } else {
                        AiResult.Error(body?.error ?: "Empty response from AgentRouter")
                    }
                } else {
                    AiResult.Error("AgentRouter error: ${response.code()}")
                }
            } catch (e: Exception) {
                AiResult.Error("Network error: ${e.message}")
            }
        }
    }

    override fun getProviderName() = "agentrouter"
}

@Singleton
class LocalAiProvider @Inject constructor(
    private val prefs: PreferencesManager
) : AiProvider {

    override suspend fun generateResponse(messages: List<ChatMessage>, model: String?): AiResult {
        return withContext(Dispatchers.IO) {
            try {
                val lastUserMessage = messages.lastOrNull { it.role == "user" }
                    ?: return@withContext AiResult.Error("No user message found")

                // Local LLM inference - placeholder for llama.cpp integration
                // In production, this would call the native library
                val response = performLocalInference(lastUserMessage.content)
                AiResult.Success(response, "local", "local-model")
            } catch (e: Exception) {
                AiResult.Error("Local inference error: ${e.message}")
            }
        }
    }

    override fun getProviderName() = "local"

    private suspend fun performLocalInference(prompt: String): String {
        // Placeholder for local model inference
        // This would integrate with llama.cpp via JNI
        return "Local inference not yet implemented. Please configure an AI provider in Settings."
    }
}

@Singleton
class AiRouter @Inject constructor(
    private val openAiProvider: OpenAiProvider,
    private val geminiProvider: GeminiProvider,
    private val agentRouterProvider: AgentRouterProvider,
    private val localAiProvider: LocalAiProvider,
    private val prefs: PreferencesManager
) {
    fun getProvider(providerName: String? = null): AiProvider {
        return when (providerName ?: prefs.getString(Constants.PREF_ACTIVE_PROVIDER, "openai")) {
            "openai" -> openAiProvider
            "gemini" -> geminiProvider
            "agentrouter" -> agentRouterProvider
            "local" -> localAiProvider
            else -> openAiProvider
        }
    }

    suspend fun generateResponse(
        messages: List<ChatMessage>,
        provider: String? = null,
        model: String? = null
    ): AiResult {
        return getProvider(provider).generateResponse(messages, model)
    }
}

data class ChatMessage(
    val role: String,
    val content: String
)
