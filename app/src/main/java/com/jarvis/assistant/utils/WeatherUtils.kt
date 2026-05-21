package com.jarvis.assistant.utils

import android.content.Context
import android.location.Geocoder
import java.util.Locale

object WeatherUtils {

    fun getCityName(context: Context, latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].locality ?: addresses[0].subAdminArea ?: "Unknown"
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun formatTemperature(temp: Double, unit: String): String {
        return when (unit) {
            "fahrenheit" -> "${String.format("%.1f", temp * 9 / 5 + 32)}°F"
            else -> "${String.format("%.1f", temp)}°C"
        }
    }

    fun getWindDescription(speed: Double): String {
        return when {
            speed < 1 -> "Calm"
            speed < 5 -> "Light breeze"
            speed < 12 -> "Moderate"
            speed < 20 -> "Strong"
            speed < 30 -> "Very strong"
            else -> "Storm force"
        }
    }

    fun getHumidityLevel(humidity: Int): String {
        return when {
            humidity < 30 -> "Very dry"
            humidity < 50 -> "Dry"
            humidity < 70 -> "Comfortable"
            humidity < 85 -> "Humid"
            else -> "Very humid"
        }
    }
}
