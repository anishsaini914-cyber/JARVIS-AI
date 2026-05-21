package com.jarvis.assistant.utils

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.jarvis.assistant.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppUtils {

    private var flashLightEnabled = false
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null

    fun getGreeting(): String {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            else -> SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestamp))
        }
    }

    fun toggleFlashlight(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (cameraManager == null) {
                    cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    cameraId = cameraManager?.cameraIdList?.firstOrNull()
                }
                cameraId?.let { id ->
                    if (flashLightEnabled) {
                        cameraManager?.setTorchMode(id, false)
                        flashLightEnabled = false
                    } else {
                        cameraManager?.setTorchMode(id, true)
                        flashLightEnabled = true
                    }
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    fun isFlashlightOn(): Boolean = flashLightEnabled

    fun getBatteryInfo(context: Context): String {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val level = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        val isCharging = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            batteryManager?.isCharging ?: false
        } else {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isPowerSaveMode
        }
        return if (level >= 0) {
            "Battery is at $level%${if (isCharging) " and charging" else ""}"
        } else {
            "Unable to read battery status"
        }
    }

    fun openApp(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                context.startActivity(intent)
                true
            } else {
                // Try searching for the app
                val searchIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://search?q=$packageName")
                }
                context.startActivity(searchIntent)
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus
        view?.let { v ->
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    fun showKeyboard(activity: Activity) {
        val view = activity.currentFocus
        view?.let { v ->
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as? android.net.ConnectivityManager
        val network = connectivityManager?.activeNetwork
        val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }
        return capabilities != null
    }

    fun getWeatherEmoji(code: Int, isDay: Boolean): String {
        return when {
            code == 0 -> if (isDay) "\u2600\uFE0F" else "\uD83C\uDF19" // Clear
            code <= 3 -> if (isDay) "\u26C5" else "\uD83C\uDF24\uFE0F" // Partly cloudy
            code <= 20 -> "\uD83C\uDF27\uFE0F" // Foggy
            code <= 30 -> "\uD83C\uDF25\uFE0F" // Drizzle
            code <= 50 -> "\uD83C\uDF26\uFE0F" // Rainy
            code <= 60 -> "\u2744\uFE0F" // Snowy
            code <= 70 -> "\u26C8\uFE0F" // Thunderstorm
            code <= 80 -> "\uD83C\uDF2A\uFE0F" // Heavy rain
            code <= 90 -> "\uD83D\uDCA8" // Windy
            else -> if (isDay) "\u2600\uFE0F" else "\uD83C\uDF19"
        }
    }

    fun getWeatherDescription(code: Int): String {
        return when {
            code == 0 -> "Clear sky"
            code == 1 -> "Mainly clear"
            code == 2 -> "Partly cloudy"
            code == 3 -> "Overcast"
            code in 4..10 -> "Foggy"
            code in 11..20 -> "Drizzle"
            code in 21..30 -> "Rainy"
            code in 31..40 -> "Snowy"
            code in 41..50 -> "Thunderstorm"
            code in 51..60 -> "Heavy rain"
            code in 61..70 -> "Stormy"
            code in 71..80 -> "Windy"
            else -> "Unknown"
        }
    }
}
