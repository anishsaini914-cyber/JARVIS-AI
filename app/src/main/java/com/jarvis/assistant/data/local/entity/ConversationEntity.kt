package com.jarvis.assistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val response: String,
    val provider: String = "openai",
    val timestamp: Long = System.currentTimeMillis(),
    val isVoiceQuery: Boolean = false,
    val commandType: String? = null,
    val wasSuccessful: Boolean = true
)
