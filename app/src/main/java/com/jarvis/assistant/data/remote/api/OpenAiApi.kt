package com.jarvis.assistant.data.remote.api

import com.jarvis.assistant.data.remote.model.ChatCompletionRequest
import com.jarvis.assistant.data.remote.model.ChatCompletionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAiApi {

    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>
}
