package com.jarvis.assistant.utils

object Constants {
    const val OPENAI_BASE_URL = "https://api.openai.com/"
    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"
    const val AGENT_ROUTER_BASE_URL = "https://agentrouter.org/"
    const val WEATHER_BASE_URL = "https://api.open-meteo.com/"
    const val BASE_URL = OPENAI_BASE_URL // Backward compatibility

    // Default Models
    const val DEFAULT_OPENAI_MODEL = "gpt-3.5-turbo"
    const val DEFAULT_GEMINI_MODEL = "gemini-1.5-flash"
    const val DEFAULT_AGENT_ROUTER_MODEL = "default"

    // Wake Word
    const val DEFAULT_WAKE_WORD = "hey jarvis"
    const val WAKE_WORD_SAMPLE_RATE = 16000

    // Timeouts
    const val VOICE_TIMEOUT_MS = 5000L
    const val LISTENING_TIMEOUT_MS = 10000L
    const val INFERENCE_TIMEOUT_MS = 30000L

    // Preferences Keys
    const val PREF_ACTIVE_PROVIDER = "active_provider"
    const val PREF_OPENAI_KEY = "openai_api_key"
    const val PREF_OPENAI_MODEL = "openai_model"
    const val PREF_GEMINI_KEY = "gemini_api_key"
    const val PREF_GEMINI_MODEL = "gemini_model"
    const val PREF_AGENT_ROUTER_KEY = "agent_router_api_key"
    const val PREF_AGENT_ROUTER_ENDPOINT = "agent_router_endpoint"
    const val PREF_AGENT_ROUTER_MODEL = "agent_router_model"
    const val PREF_WAKE_WORD = "wake_word"
    const val PREF_WAKE_WORD_ENABLED = "wake_word_enabled"
    const val PREF_OVERLAY_ENABLED = "overlay_enabled"
    const val PREF_TTS_ENABLED = "tts_enabled"
    const val PREF_TTS_VOICE = "tts_voice"
    const val PREF_WEATHER_ENABLED = "weather_enabled"
    const val PREF_WEATHER_UNIT = "weather_unit"
    const val PREF_WEATHER_LAT = "weather_lat"
    const val PREF_WEATHER_LON = "weather_lon"
    const val PREF_ONBOARDING_COMPLETED = "onboarding_completed"
    const val PREF_DARK_MODE = "dark_mode"
    const val PREF_CALL_HANDLING_ENABLED = "call_handling_enabled"
    const val PREF_THEME = "theme"

    // Notification IDs
    const val NOTIFICATION_ID_FOREGROUND = 1001
    const val NOTIFICATION_ID_OVERLAY = 1002
    const val NOTIFICATION_ID_WAKE_WORD = 1003
    const val NOTIFICATION_ID_CALL = 1004

    // Commands
    const val CMD_OPEN_APP = "open_app"
    const val CMD_OPEN_SETTINGS = "open_settings"
    const val CMD_FLASHLIGHT = "flashlight"
    const val CMD_ALARM = "alarm"
    const val CMD_CALL = "call"
    const val CMD_WEATHER = "weather"
    const val CMD_MUSIC = "music"
    const val CMD_SEARCH = "search"
    const val CMD_BATTERY = "battery"
    const val CMD_WHO_IS_CALLING = "who_is_calling"
    const val CMD_READ_NOTIFICATIONS = "read_notifications"
}
