package com.jarvis.assistant.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jarvis.assistant.data.local.dao.ChatDao
import com.jarvis.assistant.data.local.dao.ConversationDao
import com.jarvis.assistant.data.local.dao.MessageDao
import com.jarvis.assistant.data.local.entity.ChatSessionEntity
import com.jarvis.assistant.data.local.entity.ConversationEntity
import com.jarvis.assistant.data.local.entity.MessageEntity

@Database(
    entities = [
        ChatSessionEntity::class,
        MessageEntity::class,
        ConversationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
}
