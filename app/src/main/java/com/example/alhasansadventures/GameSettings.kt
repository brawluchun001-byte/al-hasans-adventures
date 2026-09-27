package com.example.alhasansadventures

import android.content.Context
import android.content.SharedPreferences

class GameSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("game_settings", Context.MODE_PRIVATE)

    companion object {
        private const val HIGH_SCORE_KEY = "high_score"
        private const val SOUND_ENABLED_KEY = "sound_enabled"
        private const val VIBRATION_ENABLED_KEY = "vibration_enabled"
    }

    var highScore: Int
        get() = prefs.getInt(HIGH_SCORE_KEY, 0)
        set(value) = prefs.edit().putInt(HIGH_SCORE_KEY, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(SOUND_ENABLED_KEY, true)
        set(value) = prefs.edit().putBoolean(SOUND_ENABLED_KEY, value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(VIBRATION_ENABLED_KEY, true)
        set(value) = prefs.edit().putBoolean(VIBRATION_ENABLED_KEY, value).apply()

    fun updateHighScore(newScore: Int) {
        if (newScore > highScore) {
            highScore = newScore
        }
    }
}
