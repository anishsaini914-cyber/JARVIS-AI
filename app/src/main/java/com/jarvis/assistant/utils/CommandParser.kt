package com.jarvis.assistant.utils

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

data class ParsedCommand(
    val type: String,
    val action: String? = null,
    val params: Map<String, String> = emptyMap(),
    val originalText: String = ""
)

object CommandParser {

    private var commands: Map<String, CommandDefinition> = emptyMap()

    data class CommandDefinition(
        val patterns: List<String>,
        val description: String
    )

    fun loadCommands(context: Context) {
        try {
            val inputStream = context.resources.openRawResource(
                context.resources.getIdentifier(
                    "jarvis_wake_word_commands",
                    "raw",
                    context.packageName
                )
            )
            val reader = BufferedReader(InputStreamReader(inputStream))
            val json = reader.readText()
            val jsonObject = JSONObject(json)
            val commandsObj = jsonObject.getJSONObject("commands")
            val map = mutableMapOf<String, CommandDefinition>()

            commandsObj.keys().forEach { key ->
                val cmd = commandsObj.getJSONObject(key)
                val patterns = mutableListOf<String>()
                val patternsArray = cmd.getJSONArray("patterns")
                for (i in 0 until patternsArray.length()) {
                    patterns.add(patternsArray.getString(i))
                }
                map[key] = CommandDefinition(
                    patterns = patterns,
                    description = cmd.getString("description")
                )
            }
            commands = map
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun parse(text: String): ParsedCommand {
        val lowerText = text.lowercase().trim()

        for ((type, definition) in commands) {
            for (pattern in definition.patterns) {
                val lowerPattern = pattern.lowercase()

                // Exact match
                if (lowerText == lowerPattern) {
                    return ParsedCommand(
                        type = type,
                        action = type,
                        originalText = text
                    )
                }

                // Pattern with wildcard
                if (lowerPattern.contains("*")) {
                    val regex = lowerPattern
                        .replace("*", "(.+)")
                        .toRegex()
                    val match = regex.find(lowerText)
                    if (match != null) {
                        val params = mutableMapOf<String, String>()
                        match.groupValues.drop(1).forEachIndexed { index, value ->
                            params["param$index"] = value.trim()
                        }
                        return ParsedCommand(
                            type = type,
                            action = type,
                            params = params,
                            originalText = text
                        )
                    }
                }
            }
        }

        // Check for simple patterns
        return when {
            lowerText.startsWith("open ") -> {
                val appName = lowerText.removePrefix("open ").trim()
                ParsedCommand(Constants.CMD_OPEN_APP, "open", mapOf("app" to appName), text)
            }

            lowerText.startsWith("play ") -> {
                val song = lowerText.removePrefix("play ").trim()
                ParsedCommand(Constants.CMD_MUSIC, "play", mapOf("query" to song), text)
            }

            lowerText.startsWith("search ") || lowerText.startsWith("google ") -> {
                val query = lowerText.removePrefix("search ")
                    .removePrefix("google ")
                    .trim()
                ParsedCommand(Constants.CMD_SEARCH, "search", mapOf("query" to query), text)
            }

            lowerText.contains("flashlight") || lowerText.contains("torch") || lowerText.contains("light") -> {
                val state = when {
                    lowerText.contains("on") -> "on"
                    lowerText.contains("off") -> "off"
                    else -> "toggle"
                }
                ParsedCommand(Constants.CMD_FLASHLIGHT, state, originalText = text)
            }

            lowerText.contains("weather") || lowerText.contains("temperature") -> {
                ParsedCommand(Constants.CMD_WEATHER, "get", originalText = text)
            }

            lowerText.contains("battery") || lowerText.contains("charge") -> {
                ParsedCommand(Constants.CMD_BATTERY, "status", originalText = text)
            }

            lowerText.contains("alarm") || lowerText.contains("wake me up") -> {
                ParsedCommand(Constants.CMD_ALARM, "set", originalText = text)
            }

            lowerText.contains("call") || lowerText.contains("phone") || lowerText.contains("dial") -> {
                val action = when {
                    lowerText.contains("cut") || lowerText.contains("end") -> "end"
                    lowerText.contains("uthao") && lowerText.contains("speaker") -> "speaker"
                    lowerText.contains("uthao") -> "answer"
                    lowerText.contains("mute") -> "mute"
                    lowerText.contains("unmute") -> "unmute"
                    else -> "call"
                }
                ParsedCommand(Constants.CMD_CALL, action, originalText = text)
            }

            lowerText.contains("who is calling") || lowerText.contains("who called") -> {
                ParsedCommand(Constants.CMD_WHO_IS_CALLING, "identify", originalText = text)
            }

            lowerText.contains("notification") -> {
                ParsedCommand(Constants.CMD_READ_NOTIFICATIONS, "read", originalText = text)
            }

            lowerText.contains("settings") || lowerText.contains("wifi") || lowerText.contains("bluetooth") -> {
                val setting = when {
                    lowerText.contains("wifi") -> "wifi"
                    lowerText.contains("bluetooth") -> "bluetooth"
                    else -> "settings"
                }
                ParsedCommand(Constants.CMD_OPEN_SETTINGS, "open", mapOf("setting" to setting), text)
            }

            else -> {
                ParsedCommand("unknown", "chat", originalText = text)
            }
        }
    }
}
