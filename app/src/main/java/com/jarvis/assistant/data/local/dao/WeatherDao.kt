package com.jarvis.assistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jarvis.assistant.data.local.entity.WeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather_cache WHERE locationKey = :key")
    suspend fun getWeatherByLocation(key: String): WeatherEntity?

    @Query("SELECT * FROM weather_cache WHERE locationKey = :key")
    fun getWeatherByLocationFlow(key: String): Flow<WeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("DELETE FROM weather_cache WHERE locationKey = :key")
    suspend fun deleteWeather(key: String)

    @Query("DELETE FROM weather_cache")
    suspend fun deleteAll()

    @Query("SELECT * FROM weather_cache ORDER BY lastUpdated DESC LIMIT 1")
    suspend fun getLatestWeather(): WeatherEntity?
}
