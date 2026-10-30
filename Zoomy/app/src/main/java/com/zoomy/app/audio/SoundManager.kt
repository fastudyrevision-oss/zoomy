package com.zoomy.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.min

class SoundManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds = mutableMapOf<String, Int>()
    var soundOn: Boolean = true

    // Coin sound rate limiting and rising pitch
    private var lastCoinSoundTimeMs = 0L
    private var lastCoinConsecutiveTimeMs = 0L
    private var consecutiveCoinCount = 0

    init {
        scope.launch(Dispatchers.IO) {
            SynthSounds.generateAllSounds(context.cacheDir)
            loadAllSounds()
        }
    }

    private fun loadAllSounds() {
        val sfxDir = File(context.cacheDir, "sfx")
        val names = listOf(
            "tap", "swipe", "coin",
            "close_2", "close_3", "close_4", "close_5",
            "shield", "shield_break", "magnet", "slowmo",
            "crash", "gameover", "newbest", "reward", "pop"
        )
        for (name in names) {
            val file = File(sfxDir, "$name.wav")
            if (file.exists()) {
                val id = soundPool.load(file.absolutePath, 1)
                soundIds[name] = id
            }
        }
    }

    fun play(name: String, rate: Float = 1.0f) {
        if (!soundOn) return
        val id = soundIds[name] ?: return
        soundPool.play(id, 1.0f, 1.0f, 1, 0, rate.coerceIn(0.5f, 2.0f))
    }

    fun playTap() = play("tap")
    fun playSwipe() = play("swipe")
    fun playShield() = play("shield")
    fun playShieldBreak() = play("shield_break")
    fun playMagnet() = play("magnet")
    fun playSlowMo() = play("slowmo")
    fun playCrash() = play("crash")
    fun playGameOver() = play("gameover")
    fun playNewBest() = play("newbest")
    fun playReward() = play("reward")
    fun playPop() = play("pop")

    fun playCloseCall(combo: Int) {
        if (!soundOn || combo <= 1) return
        val clampedCombo = combo.coerceIn(2, 5)
        play("close_$clampedCombo")
    }

    fun playCoin() {
        if (!soundOn) return
        val now = System.currentTimeMillis()
        if (now - lastCoinSoundTimeMs < 50L) return
        lastCoinSoundTimeMs = now

        if (now - lastCoinConsecutiveTimeMs <= 600L) {
            consecutiveCoinCount = min(7, consecutiveCoinCount + 1)
        } else {
            consecutiveCoinCount = 0
        }
        lastCoinConsecutiveTimeMs = now

        val pitch = Math.pow(2.0, (consecutiveCoinCount / 12.0)).toFloat()
        play("coin", pitch)
    }

    fun release() {
        soundPool.release()
    }
}
