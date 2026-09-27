package com.example.alhasansadventures

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class VibrationManager(context: Context) {
    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    private var enabled = true

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun coinCollected() {
        if (!enabled || vibrator == null) return
        triggerPattern(intArrayOf(0, 20, 30, 20), -1)
    }

    fun collision() {
        if (!enabled || vibrator == null) return
        triggerPattern(intArrayOf(0, 80, 40, 80), -1)
    }

    fun buttonPress() {
        if (!enabled || vibrator == null) return
        triggerPattern(intArrayOf(0, 25), -1)
    }

    private fun triggerPattern(pattern: IntArray, repeat: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, repeat))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, repeat)
            }
        } catch (_: Exception) {
            // no-op
        }
    }
}
