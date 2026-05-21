package com.jarvis.assistant.data.repository

import com.jarvis.assistant.ai.AiResult
import com.jarvis.assistant.ai.AiRouter
import com.jarvis.assistant.data.local.dao.ConversationDao
import com.jarvis.assistant.data.local.entity.ConversationEntity
import com.jarvis.assistant.data.remote.model.ChatMessage
import com.jarvis.assistant.utils.CommandParser
import com.jarvis.assistant.utils.Constants
import com.jarvis.assistant.utils.PreferencesManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRepository @Inject constructor(
    private val conversationDao: ConversationDao,
    private val aiRouter: AiRouter,
    private val prefs: PreferencesManager
) {

    fun getAllConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getAllConversations()

    suspend fun processVoiceCommand(text: String, commandType: String? = null): AiResult {
        val parsedCommand = CommandParser.parse(text)

        // Check if it's a system command
        if (parsedCommand.type != "unknown") {
            // System commands are handled by the service layer
            // For now, pass to AI for processing
            return processWithAi(text, commandType)
        }

        return processWithAi(text, commandType)
    }

    private suspend fun processWithAi(text: String, commandType: String? = null): AiResult {
        val messages = listOf(
            ChatMessage("system", "You are JARVIS, an AI assistant. Respond concisely and helpfully."),
            ChatMessage("user", text)
        )

        val result = aiRouter.generateResponse(messages)

        // Save conversation
        if (result is AiResult.Success) {
            conversationDao.insertConversation(
                ConversationEntity(
                    query = text,
                    response = result.response,
                    provider = result.provider,
                    isVoiceQuery = true,
                    commandType = commandType,
                    wasSuccessful = true
                )
            )
        } else if (result is AiResult.Error) {
            conversationDao.insertConversation(
                ConversationEntity(
                    query = text,
                    response = "Error: ${result.message}",
                    provider = prefs.getString(Constants.PREF_ACTIVE_PROVIDER, "openai"),
                    isVoiceQuery = true,
                    commandType = commandType,
                    wasSuccessful = false
                )
            )
        }

        return result
    }

    suspend fun deleteAllConversations() = conversationDao.deleteAllConversations()

    suspend fun getConversationCount() = conversationDao.getConversationCount()
}
