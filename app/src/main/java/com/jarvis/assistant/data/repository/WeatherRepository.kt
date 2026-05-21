package com.jarvis.assistant.data.repository

import com.jarvis.assistant.data.local.dao.WeatherDao
import com.jarvis.assistant.data.local.entity.WeatherEntity
import com.jarvis.assistant.data.remote.api.WeatherApi
import com.jarvis.assistant.utils.Constants
import com.jarvis.assistant.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class WeatherData(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int,
    val description: String,
    val location: String,
    val isDay: Boolean
)

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherApi,
    private val weatherDao: WeatherDao,
    private val prefs: PreferencesManager
) {

    suspend fun getWeather(latitude: Double? = null, longitude: Double? = null): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                val lat = latitude ?: prefs.getFloat(Constants.PREF_WEATHER_LAT, 0f).toDouble()
                val lon = longitude ?: prefs.getFloat(Constants.PREF_WEATHER_LON, 0f).toDouble()

                if (lat == 0.0 && lon == 0.0) return@withContext null

                // Check cache (10 minute TTL)
                val cacheKey = "${lat},${lon}"
                val cached = weatherDao.getWeatherByLocation(cacheKey)
                if (cached != null && System.currentTimeMillis() - cached.lastUpdated < 600_000) {
                    return@withContext cached.toWeatherData("")
                }

                val response = weatherApi.getForecast(lat, lon)
                if (response.isSuccessful) {
                    val body = response.body()
                    val current = body?.current
                    if (current != null) {
                        val entity = WeatherEntity(
                            locationKey = cacheKey,
                            temperature = current.temperature ?: 0.0,
                            feelsLike = current.apparentTemperature ?: 0.0,
                            humidity = current.humidity ?: 0,
                            windSpeed = current.windSpeed ?: 0.0,
                            weatherCode = current.weatherCode ?: 0,
                            description = getWeatherDescription(current.weatherCode ?: 0),
                            isDay = current.isDay == 1
                        )
                        weatherDao.insertWeather(entity)
                        return@withContext entity.toWeatherData("")
                    }
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun WeatherEntity.toWeatherData(location: String): WeatherData {
        return WeatherData(
            temperature = temperature,
            feelsLike = feelsLike,
            humidity = humidity,
            windSpeed = windSpeed,
            weatherCode = weatherCode,
            description = description,
            location = location,
            isDay = isDay
        )
    }

    private fun getWeatherDescription(code: Int): String {
        return com.jarvis.assistant.utils.AppUtils.getWeatherDescription(code)
    }
}
