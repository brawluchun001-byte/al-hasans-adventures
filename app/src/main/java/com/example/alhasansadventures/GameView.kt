package com.example.alhasansadventures

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private enum class ScreenState {
        MENU,
        SETTINGS,
        PLAYING,
        PAUSED,
        GAME_OVER
    }

    private val settings = GameSettings(context.applicationContext)
    private val particleSystem = ParticleSystem()
    private val vibrationManager = VibrationManager(context.applicationContext)
    private val audioManager = AudioManager(context.applicationContext)

    private var screenState = ScreenState.MENU
    private var score = 0
    private var highScore = settings.highScore
    private var soundEnabled = settings.soundEnabled
    private var vibrationEnabled = settings.vibrationEnabled

    private var boatX = 0f
    private var boatY = 0f
    private var targetX = 0f
    private var boatWidth = 150f
    private var boatHeight = 78f

    private var obstacleSpeed = 9f
    private var obstacleSpawnTimer = 0f
    private var coinSpawnTimer = 0f
    private var difficultyTimer = 0f

    private val obstacles = mutableListOf<Obstacle>()
    private val coins = mutableListOf<Coin>()
    private val random = Random(System.nanoTime())

    private var playRect = RectF()
    private var settingsRect = RectF()
    private var pauseRect = RectF()
    private var resumeRect = RectF()
    private var menuRect = RectF()
    private var restartRect = RectF()
    private var soundToggleRect = RectF()
    private var vibrationToggleRect = RectF()
    private var backRect = RectF()

    data class Obstacle(
        var x: Float,
        var y: Float,
        val size: Float,
        val speed: Float
    )

    data class Coin(
        var x: Float,
        var y: Float,
        val radius: Float,
        val speed: Float
    )

    private val skyPaint = Paint().apply {
        color = Color.parseColor("#061827")
        style = Paint.Style.FILL
    }

    private val oceanPaint = Paint().apply {
        color = Color.parseColor("#0C3B5A")
        style = Paint.Style.FILL
    }

    private val wavePaint = Paint().apply {
        color = Color.parseColor("#7FDBFF")
        style = Paint.Style.FILL
        alpha = 90
    }

    private val buttonPaint = Paint().apply {
        color = Color.parseColor("#11D1E4")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val buttonSecondaryPaint = Paint().apply {
        color = Color.parseColor("#0F1E40")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val titlePaint = Paint().apply {
        color = Color.parseColor("#E8F9FF")
        textSize = 86f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val hudPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        isFakeBoldText = true
        textAlign = Paint.Align.LEFT
        isAntiAlias = true
    }

    private val menuPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val darkTextPaint = Paint().apply {
        color = Color.parseColor("#061827")
        textSize = 32f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val obstaclePaint = Paint().apply {
        color = Color.parseColor("#2F405A")
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val coinPaint = Paint().apply {
        color = Color.parseColor("#FFD757")
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val coinInnerPaint = Paint().apply {
        color = Color.parseColor("#FFF5C3")
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val boatHullPaint = Paint().apply {
        color = Color.parseColor("#7C4D3A")
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val boatMidPaint = Paint().apply {
        color = Color.parseColor("#B98B72")
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val sailPaint = Paint().apply {
        color = Color.parseColor("#F4F9FF")
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val glowPaint = Paint().apply {
        color = Color.parseColor("#6EE7FF")
        alpha = 90
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    init {
        isFocusable = true
        isClickable = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(updateRunnable)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(updateRunnable)
        audioManager.release()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        boatX = w * 0.5f
        boatY = h * 0.68f
        targetX = boatX

        val centerX = w * 0.5f
        val centerY = h * 0.5f

        playRect = RectF(centerX - 170f, centerY + 70f, centerX + 170f, centerY + 190f)
        settingsRect = RectF(centerX - 170f, centerY + 210f, centerX + 170f, centerY + 330f)
        pauseRect = RectF(w - 150f, 22f, w - 30f, 100f)
        resumeRect = RectF(centerX - 170f, centerY - 40f, centerX + 170f, centerY + 80f)
        menuRect = RectF(centerX - 170f, centerY + 110f, centerX + 170f, centerY + 230f)
        restartRect = RectF(centerX - 170f, centerY + 110f, centerX + 170f, centerY + 230f)
        soundToggleRect = RectF(centerX - 220f, centerY - 40f, centerX + 220f, centerY + 50f)
        vibrationToggleRect = RectF(centerX - 220f, centerY + 90f, centerX + 220f, centerY + 180f)
        backRect = RectF(30f, 30f, 170f, 100f)

        resetGameState()
    }

    private fun resetGameState() {
        obstacles.clear()
        coins.clear()
        particleSystem.clear()
        score = 0
        obstacleSpeed = 9f
        obstacleSpawnTimer = 0f
        coinSpawnTimer = 0f
        difficultyTimer = 0f
        screenState = ScreenState.MENU
        highScore = settings.highScore
    }

    private fun startGame() {
        score = 0
        obstacleSpeed = 9f
        obstacleSpawnTimer = 0f
        coinSpawnTimer = 0f
        difficultyTimer = 0f
        obstacles.clear()
        coins.clear()
        particleSystem.clear()
        screenState = ScreenState.PLAYING
        targetX = width * 0.5f
        boatX = width * 0.5f
    }

    private fun pauseGame() {
        if (screenState == ScreenState.PLAYING) {
            screenState = ScreenState.PAUSED
        }
    }

    private fun resumeGame() {
        if (screenState == ScreenState.PAUSED) {
            screenState = ScreenState.PLAYING
        }
    }

    private fun goToMenu() {
        screenState = ScreenState.MENU
        obstacles.clear()
        coins.clear()
        particleSystem.clear()
        score = 0
        highScore = settings.highScore
        invalidate()
    }

    fun handleBackPress(): Boolean {
        return when (screenState) {
            ScreenState.PLAYING -> {
                pauseGame()
                true
            }
            ScreenState.PAUSED -> {
                goToMenu()
                true
            }
            ScreenState.SETTINGS -> {
                screenState = ScreenState.MENU
                true
            }
            ScreenState.GAME_OVER -> {
                goToMenu()
                true
            }
            ScreenState.MENU -> false
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)

        when (screenState) {
            ScreenState.MENU -> drawMenu(canvas)
            ScreenState.SETTINGS -> drawSettings(canvas)
            ScreenState.PLAYING -> drawGame(canvas)
            ScreenState.PAUSED -> drawPauseMenu(canvas)
            ScreenState.GAME_OVER -> drawGameOver(canvas)
        }

        particleSystem.draw(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawRect(0f, 0f, w, h, skyPaint)

        val cloudColor = Paint().apply {
            color = Color.argb(80, 255, 255, 255)
            isAntiAlias = true
        }
        for (i in 0..4) {
            val cx = (i * 220f + ((score * 0.8f) % 220f)) % (w + 200f) - 100f
            val cy = 80f + i * 70f
            canvas.drawCircle(cx, cy, 36f, cloudColor)
            canvas.drawCircle(cx + 34f, cy + 8f, 28f, cloudColor)
            canvas.drawCircle(cx - 32f, cy + 10f, 26f, cloudColor)
        }

        val seaTop = h * 0.5f
        canvas.drawRect(0f, seaTop, w, h, oceanPaint)

        for (i in 0..(w / 40f).toInt() + 2) {
            val x = i * 40f
            val y = seaTop + ((i % 2) * 18f)
            canvas.drawCircle(x, y, 18f, wavePaint)
        }
    }

    private fun drawMenu(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawText("Al-Hasan's Adventures", w * 0.5f, h * 0.22f, titlePaint)

        drawBoatIcon(canvas, w * 0.5f, h * 0.38f, 135f, 72f)

        canvas.drawRoundRect(playRect, 34f, 34f, buttonPaint)
        canvas.drawText("PLAY", w * 0.5f, h * 0.55f + 18f, menuPaint)

        canvas.drawRoundRect(settingsRect, 28f, 28f, buttonSecondaryPaint)
        canvas.drawText("SETTINGS", w * 0.5f, h * 0.74f + 12f, menuPaint)

        canvas.drawText("High Score: $highScore", w * 0.5f, h * 0.9f, hudPaint.apply { textAlign = Paint.Align.CENTER })
    }

    private fun drawSettings(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawRoundRect(RectF(w * 0.18f, h * 0.15f, w * 0.82f, h * 0.85f), 32f, 32f, buttonSecondaryPaint)

        canvas.drawText("SETTINGS", w * 0.5f, h * 0.23f, titlePaint.apply { textSize = 56f })

        val soundLabel = if (soundEnabled) "SOUND: ON" else "SOUND: OFF"
        canvas.drawRoundRect(soundToggleRect, 26f, 26f, buttonPaint)
        canvas.drawText(soundLabel, w * 0.5f, h * 0.45f + 12f, darkTextPaint)

        val vibLabel = if (vibrationEnabled) "VIBRATION: ON" else "VIBRATION: OFF"
        canvas.drawRoundRect(vibrationToggleRect, 26f, 26f, buttonPaint)
        canvas.drawText(vibLabel, w * 0.5f, h * 0.62f + 12f, darkTextPaint)

        canvas.drawRoundRect(backRect, 18f, 18f, buttonPaint)
        canvas.drawText("BACK", 100f, 72f, darkTextPaint)
    }

    private fun drawGame(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        drawBoat(canvas)

        for (obstacle in obstacles) {
            val rect = RectF(
                obstacle.x - obstacle.size * 0.5f,
                obstacle.y - obstacle.size * 0.5f,
                obstacle.x + obstacle.size * 0.5f,
                obstacle.y + obstacle.size * 0.5f
            )
            canvas.drawRoundRect(rect, 16f, 16f, obstaclePaint)
            canvas.drawRoundRect(
                RectF(
                    rect.left + 10f,
                    rect.top + 10f,
                    rect.right - 10f,
                    rect.bottom - 10f
                ),
                12f,
                12f,
                glowPaint
            )
        }

        for (coin in coins) {
            canvas.drawCircle(coin.x, coin.y, coin.radius, coinPaint)
            canvas.drawCircle(coin.x, coin.y, coin.radius * 0.53f, coinInnerPaint)
        }

        canvas.drawText("Score: $score", 28f, 52f, hudPaint)
        canvas.drawText("Best: $highScore", 28f, 100f, hudPaint)

        canvas.drawRoundRect(pauseRect, 18f, 18f, buttonPaint)
        canvas.drawText("PAUSE", w - 90f, 70f, menuPaint.apply { textSize = 24f; textAlign = Paint.Align.CENTER })
    }

    private fun drawPauseMenu(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val overlay = Paint().apply { color = Color.argb(180, 0, 0, 0); style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, w, h, overlay)

        canvas.drawText("PAUSED", w * 0.5f, h * 0.35f, titlePaint.apply { textSize = 62f })
        canvas.drawRoundRect(resumeRect, 28f, 28f, buttonPaint)
        canvas.drawText("RESUME", w * 0.5f, h * 0.46f + 18f, menuPaint)
        canvas.drawRoundRect(menuRect, 28f, 28f, buttonSecondaryPaint)
        canvas.drawText("MENU", w * 0.5f, h * 0.68f + 18f, menuPaint)
    }

    private fun drawGameOver(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val overlay = Paint().apply { color = Color.argb(170, 0, 0, 0); style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, w, h, overlay)

        canvas.drawText("GAME OVER", w * 0.5f, h * 0.28f, titlePaint.apply { textSize = 60f })
        canvas.drawText("Score: $score", w * 0.5f, h * 0.42f, hudPaint.apply { textAlign = Paint.Align.CENTER; textSize = 36f })

        canvas.drawRoundRect(restartRect, 28f, 28f, buttonPaint)
        canvas.drawText("RESTART", w * 0.5f, h * 0.62f + 18f, menuPaint)

        canvas.drawRoundRect(menuRect, 28f, 28f, buttonSecondaryPaint)
        canvas.drawText("MENU", w * 0.5f, h * 0.8f + 18f, menuPaint)
    }

    private fun drawBoat(canvas: Canvas) {
        val hullLeft = boatX - boatWidth * 0.5f
        val hullTop = boatY - boatHeight * 0.5f
        val hullRight = boatX + boatWidth * 0.5f
        val hullBottom = boatY + boatHeight * 0.5f

        val hull = Path().apply {
            moveTo(hullLeft, boatY)
            lineTo(hullLeft + boatWidth * 0.22f, hullTop + boatHeight * 0.45f)
            lineTo(hullRight - boatWidth * 0.22f, hullTop + boatHeight * 0.45f)
            lineTo(hullRight, boatY)
            lineTo(hullRight - boatWidth * 0.2f, hullBottom)
            lineTo(hullLeft + boatWidth * 0.2f, hullBottom)
            close()
        }
        canvas.drawPath(hull, boatHullPaint)
        canvas.drawPath(hull, glowPaint)

        val deck = RectF(hullLeft + 18f, hullTop + 20f, hullRight - 18f, hullBottom - 18f)
        canvas.drawRoundRect(deck, 12f, 12f, boatMidPaint)

        val sail = Path().apply {
            moveTo(boatX, hullTop + 12f)
            lineTo(boatX + boatWidth * 0.32f, boatY - boatHeight * 0.25f)
            lineTo(boatX, hullBottom - 12f)
            close()
        }
        canvas.drawPath(sail, sailPaint)
        canvas.drawLine(boatX, hullTop + 10f, boatX, hullBottom - 10f, glowPaint)
    }

    private fun drawBoatIcon(canvas: Canvas, cx: Float, cy: Float, w: Float, h: Float) {
        val hull = Path().apply {
            moveTo(cx - w * 0.34f, cy)
            lineTo(cx - w * 0.12f, cy - h * 0.35f)
            lineTo(cx + w * 0.12f, cy - h * 0.35f)
            lineTo(cx + w * 0.34f, cy)
            lineTo(cx + w * 0.17f, cy + h * 0.28f)
            lineTo(cx - w * 0.17f, cy + h * 0.28f)
            close()
        }
        canvas.drawPath(hull, boatHullPaint)
        val sail = Path().apply {
            moveTo(cx, cy - h * 0.35f)
            lineTo(cx + w * 0.16f, cy - h * 0.5f)
            lineTo(cx, cy + h * 0.16f)
            close()
        }
        canvas.drawPath(sail, sailPaint)
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (screenState == ScreenState.PLAYING) {
                updateGame()
            }
            invalidate()
            postDelayed(this, 16L)
        }
    }

    private fun updateGame() {
        val w = width.toFloat()
        val h = height.toFloat()
        val dt = 0.016f

        boatX += (targetX - boatX) * 0.18f
        boatY = h * 0.68f + sin(System.nanoTime() / 6_000_000.0).toFloat() * 16f

        obstacleSpawnTimer += dt
        coinSpawnTimer += dt
        difficultyTimer += dt

        val spawnInterval = (1.2f - minOf(0.65f, difficultyTimer * 0.025f)).coerceAtLeast(0.5f)
        if (obstacleSpawnTimer >= spawnInterval) {
            obstacles.add(
                Obstacle(
                    x = w + 30f,
                    y = random.nextFloat() * (h * 0.35f) + h * 0.22f,
                    size = random.nextFloat() * 52f + 38f,
                    speed = obstacleSpeed + random.nextFloat() * 2.5f
                )
            )
            obstacleSpawnTimer = 0f
        }

        if (coinSpawnTimer >= 1.1f) {
            coins.add(
                Coin(
                    x = w + 20f,
                    y = random.nextFloat() * (h * 0.36f) + h * 0.18f,
                    radius = 14f,
                    speed = obstacleSpeed * 1.25f
                )
            )
            coinSpawnTimer = 0f
        }

        for (obstacle in obstacles) {
            obstacle.x -= obstacle.speed
        }

        for (coin in coins) {
            coin.x -= coin.speed
            coin.y += (sin((coin.x + coin.y) / 28f).toFloat()) * 0.9f
        }

        obstacles.removeAll { obstacle ->
            val collided = abs(boatX - obstacle.x) < (boatWidth * 0.46f + obstacle.size * 0.52f) &&
                abs(boatY - obstacle.y) < (boatHeight * 0.48f + obstacle.size * 0.52f)
            if (collided) {
                handleCrash()
            }
            obstacle.x + obstacle.size < 0f || collided
        }

        coins.removeAll { coin ->
            val distance = hypot((boatX - coin.x).toDouble(), (boatY - coin.y).toDouble()).toFloat()
            val collected = distance < (boatWidth * 0.38f + coin.radius)
            if (collected) {
                score += 10
                particleSystem.emitCoinCollected(coin.x, coin.y)
                audioManager.playCoinSound()
                if (vibrationEnabled) vibrationManager.coinCollected()
            }
            coin.x + coin.radius < 0f || collected
        }

        obstacleSpeed += 0.02f
        particleSystem.update(dt)
        highScore = maxOf(highScore, score)
        settings.updateHighScore(highScore)
    }

    private fun handleCrash() {
        if (vibrationEnabled) vibrationManager.collision()
        particleSystem.emitCollision(boatX, boatY)
        if (screenState == ScreenState.PLAYING) {
            screenState = ScreenState.GAME_OVER
        }
        highScore = maxOf(highScore, score)
        settings.updateHighScore(highScore)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (screenState == ScreenState.PLAYING) {
                    targetX = event.x.coerceIn(0f, width.toFloat())
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                when (screenState) {
                    ScreenState.MENU -> {
                        if (playRect.contains(event.x, event.y)) {
                            if (vibrationEnabled) vibrationManager.buttonPress()
                            startGame()
                            return true
                        }
                        if (settingsRect.contains(event.x, event.y)) {
                            if (vibrationEnabled) vibrationManager.buttonPress()
                            screenState = ScreenState.SETTINGS
                            return true
                        }
                    }
                    ScreenState.SETTINGS -> {
                        if (backRect.contains(event.x, event.y)) {
                            if (vibrationEnabled) vibrationManager.buttonPress()
                            screenState = ScreenState.MENU
                            return true
                        }
                        if (soundToggleRect.contains(event.x, event.y)) {
                            soundEnabled = !soundEnabled
                            settings.soundEnabled = soundEnabled
                            audioManager.setEnabled(soundEnabled)
                            if (vibrationEnabled) vibrationManager.buttonPress()
                            return true
                        }
                        if (vibrationToggleRect.contains(event.x, event.y)) {
                            vibrationEnabled = !vibrationEnabled
                            settings.vibrationEnabled = vibrationEnabled
                            vibrationManager.setEnabled(vibrationEnabled)
                            if (vibrationEnabled) vibrationManager.buttonPress()
                            return true
                        }
                    }
                    ScreenState.PAUSED -> {
                        if (resumeRect.contains(event.x, event.y)) {
                            if (vibrationEnabled) vibrationManager.buttonPress()
                            resumeGame()
                            return true
                        }
                        if (menuRect.contains(event.x, event.y)) {
                            if (vibrationEnabled) vibrationManager.buttonPress()
                            goToMenu()
                            return true
                        }
                    }
                    ScreenState.GAME_OVER -> {
                        if (restartRect.contains(event.x, event.y)) {
                            if (vibrationEnabled) vibrationManager.buttonPress()
                            startGame()
                            return true
                        }
                        if (menuRect.contains(event.x, event.y)) {
                            if (vibrationEnabled) vibrationManager.buttonPress()
                            goToMenu()
                            return true
                        }
                    }
                    else -> Unit
                }

                if (screenState == ScreenState.PLAYING) {
                    if (pauseRect.contains(event.x, event.y)) {
                        if (vibrationEnabled) vibrationManager.buttonPress()
                        pauseGame()
                        return true
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return handleBackPress() || super.onKeyDown(keyCode, event)
        }
        return super.onKeyDown(keyCode, event)
    }
}
