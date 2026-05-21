package com.jarvis.assistant.data.remote.api

import com.jarvis.assistant.data.remote.model.AgentRouterRequest
import com.jarvis.assistant.data.remote.model.AgentRouterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AgentRouterApi {

    @POST("api/chat/completions")
    suspend fun createChatCompletion(
        @Body request: AgentRouterRequest
    ): Response<AgentRouterResponse>
}
