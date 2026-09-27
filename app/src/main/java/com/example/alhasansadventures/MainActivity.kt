package com.example.alhasansadventures

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val gameView = findViewById<GameView?>(R.id.gameView)
        if (keyCode == KeyEvent.KEYCODE_BACK && gameView != null) {
            return gameView.handleBackPress() || super.onKeyDown(keyCode, event)
        }
        return super.onKeyDown(keyCode, event)
    }
}
