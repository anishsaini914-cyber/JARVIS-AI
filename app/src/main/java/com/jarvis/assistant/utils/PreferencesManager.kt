package com.jarvis.assistant.utils

import android.content.SharedPreferences
import com.jarvis.assistant.di.AppPreferences
import com.jarvis.assistant.di.SecurePreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @SecurePreferences private val securePrefs: SharedPreferences,
    @AppPreferences private val appPrefs: SharedPreferences
) {

    // === Secure (Encrypted) Preferences ===

    fun saveApiKey(key: String, value: String) {
        securePrefs.edit().putString(key, value).apply()
    }

    fun getApiKey(key: String): String? {
        return securePrefs.getString(key, null)
    }

    fun removeApiKey(key: String) {
        securePrefs.edit().remove(key).apply()
    }

    fun hasApiKey(key: String): Boolean {
        return securePrefs.contains(key)
    }

    // === App Preferences ===

    fun saveString(key: String, value: String) {
        appPrefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, default: String = ""): String {
        return appPrefs.getString(key, default) ?: default
    }

    fun saveBoolean(key: String, value: Boolean) {
        appPrefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return appPrefs.getBoolean(key, default)
    }

    fun saveInt(key: String, value: Int) {
        appPrefs.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, default: Int = 0): Int {
        return appPrefs.getInt(key, default)
    }

    fun saveFloat(key: String, value: Float) {
        appPrefs.edit().putFloat(key, value).apply()
    }

    fun getFloat(key: String, default: Float = 0f): Float {
        return appPrefs.getFloat(key, default)
    }

    fun saveLong(key: String, value: Long) {
        appPrefs.edit().putLong(key, value).apply()
    }

    fun getLong(key: String, default: Long = 0L): Long {
        return appPrefs.getLong(key, default)
    }

    fun remove(key: String) {
        appPrefs.edit().remove(key).apply()
    }

    fun clearAll() {
        appPrefs.edit().clear().apply()
    }

    fun clearSecure() {
        securePrefs.edit().clear().apply()
    }
}
