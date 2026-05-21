package com.jarvis.assistant.data.repository

import com.jarvis.assistant.ai.AiResult
import com.jarvis.assistant.ai.AiRouter
import com.jarvis.assistant.data.local.dao.ChatDao
import com.jarvis.assistant.data.local.dao.MessageDao
import com.jarvis.assistant.data.local.entity.ChatSessionEntity
import com.jarvis.assistant.data.local.entity.MessageEntity
import com.jarvis.assistant.data.remote.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val aiRouter: AiRouter
) {

    // Session operations
    fun getAllSessions(): Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getSessionById(id: Long): Flow<ChatSessionEntity?> = chatDao.getSessionByIdFlow(id)

    suspend fun getSessionByIdSync(id: Long): ChatSessionEntity? = chatDao.getSessionById(id)

    suspend fun createSession(provider: String = "openai", model: String = "gpt-3.5-turbo"): Long {
        val session = ChatSessionEntity(
            title = "New Chat",
            provider = provider,
            model = model
        )
        return chatDao.insertSession(session)
    }

    suspend fun updateSession(session: ChatSessionEntity) = chatDao.updateSession(session)

    suspend fun deleteSession(id: Long) {
        messageDao.deleteMessagesBySession(id)
        chatDao.deleteSessionById(id)
    }

    suspend fun updateSessionTitle(id: Long, title: String) {
        chatDao.updateSessionTitle(id, title)
    }

    // Message operations
    fun getMessages(sessionId: Long): Flow<List<MessageEntity>> =
        messageDao.getMessagesBySession(sessionId)

    suspend fun sendMessage(sessionId: Long, content: String, provider: String? = null): AiResult {
        // Save user message
        val userMessage = MessageEntity(
            sessionId = sessionId,
            role = "user",
            content = content
        )
        messageDao.insertMessage(userMessage)

        // Get conversation history from database
        val session = chatDao.getSessionById(sessionId)
        val recentMessages = messageDao.getMessagesBySession(sessionId)
            .let { flow -> flow.first() }
            .takeLast(10) // Keep context window reasonable

        // Build context from recent messages
        val contextMessages = mutableListOf<ChatMessage>().apply {
            add(ChatMessage("system", "You are JARVIS, a helpful AI assistant. Respond concisely and helpfully."))
            // Add conversation history for context
            recentMessages.forEach { msg ->
                add(ChatMessage(msg.role, msg.content))
            }
        }

        // Get AI response
        val result = aiRouter.generateResponse(
            messages = contextMessages,
            provider = provider ?: session?.provider
        )

        // Save AI response on success
        if (result is AiResult.Success) {
            val aiMessage = MessageEntity(
                sessionId = sessionId,
                role = "assistant",
                content = result.response,
                providerUsed = result.provider
            )
            messageDao.insertMessage(aiMessage)

            // Update session title if first message
            val msgCount = messageDao.getMessageCount(sessionId)
            if (msgCount <= 2) {
                val title = content.take(50).let {
                    if (it.length >= 50) "$it..." else it
                }
                chatDao.updateSessionTitle(sessionId, title)
            }
        }

        return result
    }

    suspend fun deleteAllSessions() {
        messageDao.deleteAllMessages()
        chatDao.deleteAllSessions()
    }

    suspend fun deleteMessage(id: Long) {
        messageDao.deleteMessage(MessageEntity(id = id, sessionId = 0, role = "", content = ""))
    }
}
