package com.jarvis.assistant.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jarvis.assistant.data.local.dao.WeatherDao
import com.jarvis.assistant.data.local.entity.WeatherEntity

@Database(
    entities = [WeatherEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}
