package com.example.alhasansadventures

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // Game States
    private enum class GameState {
        MENU, PLAYING, PAUSED, GAME_OVER
    }

    private var gameState = GameState.MENU

    // Paints
    private val waterPaint = Paint().apply {
        color = Color.parseColor("#1E90FF")
        style = Paint.Style.FILL
    }

    private val foamPaint = Paint().apply {
        color = Color.parseColor("#DFF7FF")
        style = Paint.Style.FILL
    }

    private val boatBodyPaint = Paint().apply {
        color = Color.parseColor("#8B4513")
        isAntiAlias = true
    }

    private val sailPaint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val obstaclePaint = Paint().apply {
        color = Color.parseColor("#4B2E2B")
        isAntiAlias = true
    }

    private val coinPaint = Paint().apply {
        color = Color.parseColor("#FFD700")
        isAntiAlias = true
    }

    private val coinHighlightPaint = Paint().apply {
        color = Color.parseColor("#FFF7A8")
        isAntiAlias = true
    }

    private val scorePaint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        isFakeBoldText = true
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
    }

    private val gameOverPaint = Paint().apply {
        color = Color.WHITE
        textSize = 72f
        isFakeBoldText = true
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val buttonPaint = Paint().apply {
        color = Color.parseColor("#FF6B35")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val buttonTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 42f
        isFakeBoldText = true
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val titlePaint = Paint().apply {
        color = Color.parseColor("#1E90FF")
        textSize = 96f
        isFakeBoldText = true
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val logoPaint = Paint().apply {
        color = Color.parseColor("#FFD700")
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
    }

    private val pauseTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 64f
        isFakeBoldText = true
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    // Game Variables
    private var boatX = 0f
    private var boatY = 0f
    private var targetX = 0f
    private var boatWidth = 140f
    private var boatHeight = 80f

    private var score = 0
    private var obstacleSpeed = 12f
    private var lastObstacleSpawn = 0L
    private var lastCoinSpawn = 0L

    private val obstacles = mutableListOf<Obstacle>()
    private val coins = mutableListOf<Coin>()
    private val random = Random(System.nanoTime())

    // Buttons
    private var playButtonRect = RectF()
    private var pauseButtonRect = RectF()
    private var resumeButtonRect = RectF()
    private var restartButtonRect = RectF()
    private var quitButtonRect = RectF()

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

    init {
        isFocusable = true
        isClickable = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        boatX = w / 2f
        boatY = h * 0.65f
        targetX = boatX
        
        // Setup button positions
        val centerX = w / 2f
        val centerY = h / 2f
        
        playButtonRect = RectF(centerX - 120f, centerY + 40f, centerX + 120f, centerY + 120f)
        pauseButtonRect = RectF(w - 140f, 20f, w - 20f, 100f)
        resumeButtonRect = RectF(centerX - 150f, centerY - 40f, centerX + 150f, centerY + 40f)
        restartButtonRect = RectF(centerX - 150f, centerY + 80f, centerX + 150f, centerY + 160f)
        quitButtonRect = RectF(centerX - 120f, centerY + 180f, centerX + 120f, centerY + 260f)
        
        resetGame()
        post(updateRunnable)
    }

    private fun resetGame() {
        obstacles.clear()
        coins.clear()
        score = 0
        gameState = GameState.MENU
        obstacleSpeed = 12f
        lastObstacleSpawn = 0L
        lastCoinSpawn = 0L
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()

        canvas.drawColor(Color.parseColor("#87CEEB"))

        when (gameState) {
            GameState.MENU -> drawMenu(canvas)
            GameState.PLAYING -> drawGame(canvas)
            GameState.PAUSED -> drawPauseMenu(canvas)
            GameState.GAME_OVER -> drawGameOverScreen(canvas)
        }
    }

    private fun drawMenu(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()

        // Draw small logo
        drawSmallLogo(canvas, width / 2f, height * 0.25f)

        // Draw title
        canvas.drawText("Al-Hasan's Adventures", width / 2f, height * 0.42f, titlePaint)

        // Draw play button
        canvas.drawRoundRect(playButtonRect, 20f, 20f, buttonPaint)
        canvas.drawText("START", playButtonRect.centerX(), playButtonRect.centerY() + 15f, buttonTextPaint)
    }

    private fun drawSmallLogo(canvas: Canvas, centerX: Float, centerY: Float) {
        val radius = 35f
        
        // Circle border
        canvas.drawCircle(centerX, centerY, radius, logoPaint)
        
        // Boat inside logo
        val boatLogoWidth = radius * 1.2f
        val boatLogoHeight = radius * 0.8f
        
        val hullLeft = centerX - boatLogoWidth / 2f
        val hullRight = centerX + boatLogoWidth / 2f
        val hullTop = centerY - boatLogoHeight / 2f
        val hullBottom = centerY + boatLogoHeight / 2f
        
        val hull = Path().apply {
            moveTo(hullLeft, centerY)
            lineTo(hullLeft + boatLogoWidth * 0.18f, hullTop + boatLogoHeight * 0.45f)
            lineTo(hullRight - boatLogoWidth * 0.18f, hullTop + boatLogoHeight * 0.45f)
            lineTo(hullRight, centerY)
            lineTo(hullRight - boatLogoWidth * 0.22f, hullBottom)
            lineTo(hullLeft + boatLogoWidth * 0.22f, hullBottom)
            close()
        }
        canvas.drawPath(hull, logoPaint)
    }

    private fun drawGame(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()

        val seaTop = height * 0.50f
        canvas.drawRect(0f, seaTop, width, height, waterPaint)

        for (i in 0..((width / 35f).toInt() + 2)) {
            val x = i * 35f
            val y = seaTop + ((i % 2) * 18f)
            canvas.drawCircle(x, y, 18f, foamPaint)
        }

        for (obstacle in obstacles) {
            val rect = RectF(
                obstacle.x - obstacle.size / 2f,
                obstacle.y - obstacle.size / 2f,
                obstacle.x + obstacle.size / 2f,
                obstacle.y + obstacle.size / 2f
            )
            canvas.drawRoundRect(rect, 14f, 14f, obstaclePaint)
        }

        for (coin in coins) {
            canvas.drawCircle(coin.x, coin.y, coin.radius, coinPaint)
            canvas.drawCircle(coin.x, coin.y, coin.radius * 0.5f, coinHighlightPaint)
        }

        drawBoat(canvas)

        canvas.drawText("Score: $score", 30f, 60f, scorePaint)
        
        // Draw pause button
        canvas.drawRoundRect(pauseButtonRect, 12f, 12f, buttonPaint)
        canvas.drawText("PAUSE", pauseButtonRect.centerX(), pauseButtonRect.centerY() + 12f, Paint().apply {
            color = Color.WHITE
            textSize = 28f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        })
    }

    private fun drawBoat(canvas: Canvas) {
        val hullLeft = boatX - boatWidth / 2f
        val hullTop = boatY - boatHeight / 2f
        val hullRight = boatX + boatWidth / 2f
        val hullBottom = boatY + boatHeight / 2f

        val hull = Path().apply {
            moveTo(hullLeft, boatY)
            lineTo(hullLeft + boatWidth * 0.18f, hullTop + boatHeight * 0.45f)
            lineTo(hullRight - boatWidth * 0.18f, hullTop + boatHeight * 0.45f)
            lineTo(hullRight, boatY)
            lineTo(hullRight - boatWidth * 0.22f, hullBottom)
            lineTo(hullLeft + boatWidth * 0.22f, hullBottom)
            close()
        }
        canvas.drawPath(hull, boatBodyPaint)

        val sail = Path().apply {
            moveTo(boatX, hullTop + 10f)
            lineTo(boatX + boatWidth * 0.28f, boatY - boatHeight * 0.2f)
            lineTo(boatX, hullBottom - 10f)
            close()
        }
        canvas.drawPath(sail, sailPaint)

        canvas.drawRect(boatX, hullTop + 8f, boatX + 3f, hullTop + 50f, Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        })
    }

    private fun drawPauseMenu(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()

        // Semi-transparent overlay
        val overlayPaint = Paint().apply {
            color = Color.argb(180, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width, height, overlayPaint)

        canvas.drawText("PAUSED", width / 2f, height / 2f - 80f, pauseTextPaint)

        // Resume button
        canvas.drawRoundRect(resumeButtonRect, 20f, 20f, buttonPaint)
        canvas.drawText("RESUME", resumeButtonRect.centerX(), resumeButtonRect.centerY() + 15f, buttonTextPaint)

        // Quit button
        canvas.drawRoundRect(quitButtonRect, 20f, 20f, buttonPaint)
        canvas.drawText("MENU", quitButtonRect.centerX(), quitButtonRect.centerY() + 15f, buttonTextPaint)
    }

    private fun drawGameOverScreen(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()

        // Semi-transparent overlay
        val overlayPaint = Paint().apply {
            color = Color.argb(200, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width, height, overlayPaint)

        canvas.drawText("Game Over", width / 2f, height / 2f - 120f, gameOverPaint)
        
        canvas.drawText("Final Score: $score", width / 2f, height / 2f - 20f, Paint().apply {
            color = Color.WHITE
            textSize = 48f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        })

        // Restart button
        canvas.drawRoundRect(restartButtonRect, 20f, 20f, buttonPaint)
        canvas.drawText("RESTART", restartButtonRect.centerX(), restartButtonRect.centerY() + 15f, buttonTextPaint)

        // Menu button
        canvas.drawRoundRect(quitButtonRect, 20f, 20f, buttonPaint)
        canvas.drawText("MENU", quitButtonRect.centerX(), quitButtonRect.centerY() + 15f, buttonTextPaint)
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (gameState == GameState.PLAYING) {
                updateGame()
            }
            invalidate()
            postDelayed(this, 16L)
        }
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(updateRunnable)
        super.onDetachedFromWindow()
    }

    private fun updateGame() {
        val w = width.toFloat()
        val h = height.toFloat()

        boatX += (targetX - boatX) * 0.18f
        boatY = h * 0.65f + (Math.sin(System.nanoTime() / 5_000_000.0) * 18.0).toFloat()

        val now = System.currentTimeMillis()

        if (now - lastObstacleSpawn > 900) {
            obstacles.add(
                Obstacle(
                    x = w + 60f,
                    y = random.nextFloat() * (h * 0.35f) + h * 0.20f,
                    size = random.nextFloat() * 50f + 45f,
                    speed = obstacleSpeed + random.nextFloat() * 3f
                )
            )
            lastObstacleSpawn = now
        }

        if (now - lastCoinSpawn > 1200) {
            coins.add(
                Coin(
                    x = w + 20f,
                    y = random.nextFloat() * (h * 0.30f) + h * 0.15f,
                    radius = 16f,
                    speed = obstacleSpeed * 1.1f
                )
            )
            lastCoinSpawn = now
        }

        for (obstacle in obstacles) {
            obstacle.x -= obstacle.speed
        }

        for (coin in coins) {
            coin.x -= coin.speed
            coin.y += (Math.sin((coin.x + coin.y) / 20f).toFloat()) * 0.8f
        }

        obstacles.removeAll { obstacle ->
            val collided = abs(boatX - obstacle.x) < (boatWidth * 0.55f + obstacle.size * 0.5f) &&
                    abs(boatY - obstacle.y) < (boatHeight * 0.58f + obstacle.size * 0.5f)

            if (collided) {
                gameState = GameState.GAME_OVER
            }

            obstacle.x + obstacle.size < 0f || collided
        }

        coins.removeAll { coin ->
            val distance = hypot((boatX - coin.x).toDouble(), (boatY - coin.y).toDouble()).toFloat()
            val collected = distance < (boatWidth * 0.35f + coin.radius)

            if (collected) {
                score += 10
            }

            coin.x + coin.radius < 0f || collected
        }

        obstacleSpeed += 0.008f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                when (gameState) {
                    GameState.MENU -> {
                        if (playButtonRect.contains(x, y)) {
                            gameState = GameState.PLAYING
                            score = 0
                            obstacles.clear()
                            coins.clear()
                            obstacleSpeed = 12f
                            lastObstacleSpawn = 0L
                            lastCoinSpawn = 0L
                        }
                    }
                    GameState.PLAYING -> {
                        if (pauseButtonRect.contains(x, y)) {
                            gameState = GameState.PAUSED
                        } else {
                            targetX = x.coerceIn(0f, width.toFloat())
                        }
                    }
                    GameState.PAUSED -> {
                        when {
                            resumeButtonRect.contains(x, y) -> gameState = GameState.PLAYING
                            quitButtonRect.contains(x, y) -> gameState = GameState.MENU
                        }
                    }
                    GameState.GAME_OVER -> {
                        when {
                            restartButtonRect.contains(x, y) -> {
                                gameState = GameState.PLAYING
                                score = 0
                                obstacles.clear()
                                coins.clear()
                                obstacleSpeed = 12f
                            }
                            quitButtonRect.contains(x, y) -> gameState = GameState.MENU
                        }
                    }
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (gameState == GameState.PLAYING) {
                    targetX = event.x.coerceIn(0f, width.toFloat())
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
