package com.zoomy.app.game

import androidx.compose.ui.geometry.Offset

// Effects:
// - Coin sparkle: 4 stars, 6dp, Sun light, 240ms EmphasizedDecelerate
// - Floating combo text: "Close!" (2), "Zoom!" (3), "Wow!" (4+) in Baloo ExtraBold 22sp Grape with a 3dp white outline, rising 40dp and fading over 700ms Standard
// - Crash effect: road shake +-5dp 6 cycles 300ms, the car spins 0 -> 360 degrees over 400ms EmphasizedDecelerate and fades, a puff burst (8 circles 12dp, white 80% -> 0%, radial to 40dp, 400ms Standard) and 8 sparkles (Sun)
// - Shield pop sparkles: 6 sparkles

data class CoinSparkle(
    val id: Long,
    val x: Float,
    val y: Float,
    var ageSec: Float = 0f,
    val durationSec: Float = 0.24f
)

data class FloatingComboText(
    val id: Long,
    val text: String,
    val x: Float,
    var startY: Float,
    var currentY: Float,
    var ageSec: Float = 0f,
    val durationSec: Float = 0.70f
)

data class CrashPuff(
    val angleRad: Float,
    val maxDist: Float = 40f,
    var currentDist: Float = 0f,
    var alpha: Float = 0.8f
)

data class CrashSparkle(
    val angleRad: Float,
    val maxDist: Float = 45f,
    var currentDist: Float = 0f
)

class CrashEffectState {
    var active: Boolean = false
    var elapsedSec: Float = 0f
    val durationSec: Float = 0.60f
    var shakeOffsetX: Float = 0f
    var spinAngleDeg: Float = 0f
    var playerAlpha: Float = 1f
    val puffs = mutableListOf<CrashPuff>()
    val sparkles = mutableListOf<CrashSparkle>()

    fun start() {
        active = true
        elapsedSec = 0f
        shakeOffsetX = 0f
        spinAngleDeg = 0f
        playerAlpha = 1f
        puffs.clear()
        sparkles.clear()

        // 8 circles 12dp, radial to 40dp
        for (i in 0 until 8) {
            val angle = (i * (2 * Math.PI / 8)).toFloat()
            puffs.add(CrashPuff(angleRad = angle))
        }
        // 8 sparkles (Sun)
        for (i in 0 until 8) {
            val angle = ((i + 0.5f) * (2 * Math.PI / 8)).toFloat()
            sparkles.add(CrashSparkle(angleRad = angle))
        }
    }

    fun update(dt: Float) {
        if (!active) return
        elapsedSec += dt

        // Road shake +-5dp 6 cycles 300ms (0.3s)
        if (elapsedSec <= 0.30f) {
            val progress = elapsedSec / 0.30f
            // 6 cycles = 6 * 2pi
            val cycle = kotlin.math.sin(progress * 6f * 2f * Math.PI.toFloat())
            shakeOffsetX = cycle * 5f * (1f - progress)
        } else {
            shakeOffsetX = 0f
        }

        // Car spins 0 -> 360 over 400ms EmphasizedDecelerate and fades
        if (elapsedSec <= 0.40f) {
            val p = (elapsedSec / 0.40f).coerceIn(0f, 1f)
            // EmphasizedDecelerate cubic curve approx
            val inv = 1f - p
            val t = 1f - inv * inv * inv
            spinAngleDeg = t * 360f
            playerAlpha = (1f - p).coerceIn(0f, 1f)
        } else {
            spinAngleDeg = 360f
            playerAlpha = 0f
        }

        // Puffs & Sparkles over 400ms Standard
        val puffProgress = (elapsedSec / 0.40f).coerceIn(0f, 1f)
        puffs.forEach { puff ->
            puff.currentDist = puffProgress * puff.maxDist
            puff.alpha = (0.8f * (1f - puffProgress)).coerceIn(0f, 1f)
        }
        sparkles.forEach { sparkle ->
            sparkle.currentDist = puffProgress * sparkle.maxDist
        }
    }
}
