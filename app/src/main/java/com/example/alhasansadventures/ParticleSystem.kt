package com.example.alhasansadventures

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    var maxLife: Float,
    val radius: Float,
    val color: Int
)

class ParticleSystem {
    private val particles = mutableListOf<Particle>()
    private val random = Random(System.nanoTime())
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    companion object {
        private const val MAX_PARTICLES = 256
    }

    fun emitCoinCollected(x: Float, y: Float) {
        if (particles.size >= MAX_PARTICLES) return
        repeat(10) {
            val angle = random.nextFloat() * (Math.PI * 2.0).toFloat()
            val speed = 3f + random.nextFloat() * 5f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle.toDouble()).toFloat() * speed,
                    vy = sin(angle.toDouble()).toFloat() * speed,
                    life = 1f,
                    maxLife = 1f,
                    radius = 3f + random.nextFloat() * 4f,
                    color = Color.parseColor("#FFD854")
                )
            )
        }
    }

    fun emitCollision(x: Float, y: Float) {
        if (particles.size >= MAX_PARTICLES) return
        repeat(14) {
            val angle = random.nextFloat() * (Math.PI * 2.0).toFloat()
            val speed = 2f + random.nextFloat() * 6f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle.toDouble()).toFloat() * speed,
                    vy = sin(angle.toDouble()).toFloat() * speed,
                    life = 0.9f,
                    maxLife = 0.9f,
                    radius = 3f + random.nextFloat() * 4f,
                    color = Color.parseColor("#FF7A59")
                )
            )
        }
    }

    fun update(dt: Float) {
        particles.removeAll { particle ->
            particle.life -= dt / particle.maxLife
            particle.x += particle.vx
            particle.y += particle.vy
            particle.vy += 0.12f
            particle.life <= 0f
        }
    }

    fun draw(canvas: Canvas) {
        for (particle in particles) {
            val alpha = ((particle.life / particle.maxLife) * 255f).toInt().coerceIn(0, 255)
            paint.color = particle.color
            paint.alpha = alpha
            canvas.drawCircle(particle.x, particle.y, particle.radius, paint)
        }
        paint.alpha = 255
    }

    fun clear() {
        particles.clear()
    }
}
