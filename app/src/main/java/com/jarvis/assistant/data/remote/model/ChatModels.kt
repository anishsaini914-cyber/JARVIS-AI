package com.jarvis.assistant.data.remote.model

import com.google.gson.annotations.SerializedName

// === OpenAI Models ===

data class ChatCompletionRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val maxTokens: Int = 2000,
    @SerializedName("max_tokens") val max_tokens: Int = 2000,
    val stream: Boolean = false
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatCompletionResponse(
    val id: String?,
    val `object`: String?,
    val created: Long?,
    val model: String?,
    val choices: List<Choice>?,
    val usage: Usage?,
    val error: ErrorResponse? = null
)

data class Choice(
    val index: Int?,
    val message: ChatMessage?,
    val delta: ChatMessage?,
    @SerializedName("finish_reason") val finishReason: String?
)

data class Usage(
    @SerializedName("prompt_tokens") val promptTokens: Int?,
    @SerializedName("completion_tokens") val completionTokens: Int?,
    @SerializedName("total_tokens") val totalTokens: Int?
)

data class ErrorResponse(
    val message: String?,
    val type: String?,
    val code: String?
)

// === Gemini Models ===

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiConfig = GeminiConfig()
)

data class GeminiContent(
    val role: String = "user",
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiConfig(
    val temperature: Double = 0.7,
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int = 2000
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?,
    val error: GeminiError? = null
)

data class GeminiCandidate(
    val content: GeminiContent?,
    @SerializedName("finishReason") val finishReason: String?
)

data class GeminiError(
    val code: Int?,
    val message: String?,
    val status: String?
)

// === AgentRouter Models ===

data class AgentRouterRequest(
    val messages: List<RouterMessage>,
    val model: String = "default",
    val temperature: Double = 0.7
)

data class RouterMessage(
    val role: String,
    val content: String
)

data class AgentRouterResponse(
    val choices: List<RouterChoice>?,
    val error: String? = null
)

data class RouterChoice(
    val message: RouterMessage?,
    @SerializedName("finish_reason") val finishReason: String?
)

// === Weather Models ===

data class WeatherResponse(
    val latitude: Double?,
    val longitude: Double?,
    val current: WeatherCurrent?,
    val error: Boolean? = null,
    val reason: String? = null
)

data class WeatherCurrent(
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("relative_humidity_2m") val humidity: Int?,
    @SerializedName("apparent_temperature") val apparentTemperature: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("wind_speed_10m") val windSpeed: Double?,
    @SerializedName("is_day") val isDay: Int?
)

// === Local LLM Models ===

data class LocalInferenceRequest(
    val prompt: String,
    val maxTokens: Int = 256,
    val temperature: Double = 0.7,
    val topP: Double = 0.9,
    val topK: Int = 40,
    val repeatPenalty: Double = 1.1
)

data class LocalInferenceResponse(
    val text: String?,
    val tokensGenerated: Int?,
    val inferenceTime: Long?,
    val error: String? = null
)
