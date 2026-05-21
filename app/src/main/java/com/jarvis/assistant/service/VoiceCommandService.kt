package com.jarvis.assistant.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import com.jarvis.assistant.utils.AppUtils
import com.jarvis.assistant.utils.Constants
import com.jarvis.assistant.utils.ParsedCommand
import com.jarvis.assistant.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VoiceCommandService : Service() {

    @Inject
    lateinit var prefs: PreferencesManager

    override fun onBind(intent: Intent?): IBinder? = null

    fun executeCommand(command: ParsedCommand): String {
        return when (command.type) {
            Constants.CMD_FLASHLIGHT -> executeFlashlight(command)
            Constants.CMD_BATTERY -> getBatteryStatus()
            Constants.CMD_OPEN_APP -> openApp(command)
            Constants.CMD_OPEN_SETTINGS -> openSettings(command)
            Constants.CMD_SEARCH -> performSearch(command)
            Constants.CMD_MUSIC -> playMusic(command)
            else -> "Command type ${command.type} not supported"
        }
    }

    private fun executeFlashlight(command: ParsedCommand): String {
        return when (command.action) {
            "on" -> {
                AppUtils.toggleFlashlight(this)
                "Flashlight turned on"
            }
            "off" -> {
                if (AppUtils.isFlashlightOn()) {
                    AppUtils.toggleFlashlight(this)
                    "Flashlight turned off"
                } else {
                    AppUtils.toggleFlashlight(this)
                    "Flashlight turned on"
                }
            }
            else -> {
                AppUtils.toggleFlashlight(this)
                if (AppUtils.isFlashlightOn()) "Flashlight turned on" else "Flashlight turned off"
            }
        }
    }

    private fun getBatteryStatus(): String {
        return AppUtils.getBatteryInfo(this)
    }

    private fun openApp(command: ParsedCommand): String {
        val appName = command.params["app"] ?: command.params["param0"] ?: return "What app should I open?"
        // Try to find and open the app
        val packageName = resolveAppPackage(appName)
        return if (packageName != null && AppUtils.openApp(this, packageName)) {
            "Opening $appName"
        } else {
            "I couldn't find the app $appName"
        }
    }

    private fun openSettings(command: ParsedCommand): String {
        val setting = command.params["setting"] ?: command.params["param0"] ?: "settings"
        return try {
            val intent = when (setting) {
                "wifi" -> Intent(Settings.ACTION_WIFI_SETTINGS)
                "bluetooth" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                "display" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
                "sound" -> Intent(Settings.ACTION_SOUND_SETTINGS)
                else -> Intent(Settings.ACTION_SETTINGS)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            "Opening $setting settings"
        } catch (e: Exception) {
            "Unable to open settings"
        }
    }

    private fun performSearch(command: ParsedCommand): String {
        val query = command.params["query"] ?: command.params["param0"] ?: return "What should I search for?"
        AppUtils.openUrl(this, "https://google.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}")
        return "Searching for $query"
    }

    private fun playMusic(command: ParsedCommand): String {
        val query = command.params["query"] ?: command.params["param0"]
        return if (query != null) {
            AppUtils.openUrl(this, "https://music.youtube.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}")
            "Playing $query"
        } else {
            AppUtils.openApp(this, "com.spotify.music")
                ?: AppUtils.openApp(this, "com.google.android.apps.youtube.music")
            "Opening music player"
        }
    }

    private fun resolveAppPackage(appName: String): String? {
        val appMap = mapOf(
            "whatsapp" to "com.whatsapp",
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "gmail" to "com.google.android.gm",
            "maps" to "com.google.android.apps.maps",
            "photos" to "com.google.android.apps.photos",
            "settings" to "com.android.settings",
            "camera" to "com.android.camera",
            "phone" to "com.android.dialer",
            "messages" to "com.android.messaging",
            "play store" to "com.android.vending",
            "spotify" to "com.spotify.music",
            "instagram" to "com.instagram.android",
            "twitter" to "com.twitter.android",
            "facebook" to "com.facebook.katana",
            "telegram" to "org.telegram.messenger",
            "netflix" to "com.netflix.mediaclient",
            "calculator" to "com.google.android.calculator",
            "clock" to "com.google.android.deskclock",
            "calendar" to "com.google.android.calendar"
        )
        return appMap[appName.lowercase()]
    }
}
