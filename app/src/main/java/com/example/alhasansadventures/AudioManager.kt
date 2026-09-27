package com.example.alhasansadventures

import android.content.Context
import android.media.SoundPool

class AudioManager(context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .build()

    private val soundIds = mutableMapOf<String, Int>()
    private var enabled = true

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun loadCoinSound(context: Context) {
        // Optional sound file load hook for later
        // if (soundIds["coin"] == null) {
        //     soundIds["coin"] = soundPool.load(context, R.raw.coin, 1)
        // }
    }

    fun playCoinSound() {
        if (!enabled) return
    }

    fun playCollisionSound() {
        if (!enabled) return
    }

    fun playButtonSound() {
        if (!enabled) return
    }

    fun release() {
        soundPool.release()
    }
}
