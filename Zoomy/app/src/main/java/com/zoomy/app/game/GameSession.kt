package com.zoomy.app.game

import android.content.Context
import com.zoomy.app.audio.SoundManager
import com.zoomy.app.data.PrefsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

data class GameSessionState(
    val score: Int = 0,
    val bestScore: Int = 0,
    val distanceM: Int = 0,
    val runCoins: Int = 0,
    val speed: Float = 260f,
    val combo: Int = 1,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val shieldActive: Boolean = false,
    val magnetRemainingMs: Long = 0L,
    val slowmoRemainingMs: Long = 0L,
    val revivesUsedAd: Int = 0,
    val revivesUsedCoins: Int = 0,
    val mode: GameMode = GameMode.NORMAL,
    val laneCount: Int = 3,
    val dailyTargetReached: Boolean = false
)

class GameSession(
    val engine: ZoomyEngine,
    private val prefsRepository: PrefsRepository,
    private val soundManager: SoundManager,
    private val scope: CoroutineScope
) {
    private val _sessionState = MutableStateFlow(GameSessionState())
    val sessionState: StateFlow<GameSessionState> = _sessionState.asStateFlow()

    var revivesUsedAd: Int = 0
    var revivesUsedCoins: Int = 0

    init {
        engine.onSwipeSound = { soundManager.playSwipe() }
        engine.onCoinPickup = { soundManager.playCoin() }
        engine.onShieldActivated = { soundManager.playShield() }
        engine.onShieldBroken = { soundManager.playShieldBreak() }
        engine.onMagnetActivated = { soundManager.playMagnet() }
        engine.onSlowMoActivated = { soundManager.playSlowMo() }
        engine.onDailyTargetReached = {
            soundManager.playReward()
            scope.launch(Dispatchers.IO) {
                val prefs = prefsRepository.prefsFlow.first()
                val todayStr = LocalDate.now().toString()
                val coinReward = if (prefs.plusActive) 100 else 50
                prefsRepository.claimDailyReward(todayStr, coinReward)
            }
        }
    }

    fun startRun(mode: GameMode, carColorIndex: Int) {
        this.revivesUsedAd = 0
        this.revivesUsedCoins = 0
        engine.mode = mode
        engine.playerColorIndex = carColorIndex
        if (mode == GameMode.DAILY) {
            val today = LocalDate.now()
            engine.customSeed = today.toEpochDay()
            engine.dailyModifier = DailyRules.getModifier(today)
        } else {
            engine.customSeed = System.nanoTime()
            engine.dailyModifier = DailyRules.getModifier()
        }
        engine.resetGame()
        syncState()
    }

    fun reviveViaAd(): Boolean {
        if (revivesUsedAd >= 1) return false
        revivesUsedAd++
        engine.revive()
        soundManager.playPop()
        syncState()
        return true
    }

    fun reviveViaCoins(): Boolean {
        if (revivesUsedCoins >= 1) return false
        revivesUsedCoins++
        engine.revive()
        soundManager.playPop()
        syncState()
        return true
    }

    fun syncState() {
        _sessionState.value = GameSessionState(
            score = engine.score,
            bestScore = 0,
            distanceM = (engine.distance / 20f).toInt(),
            runCoins = engine.runCoins,
            speed = engine.speed,
            combo = engine.combo,
            isGameOver = engine.isGameOver,
            isPaused = engine.isPaused,
            shieldActive = engine.shieldActive,
            magnetRemainingMs = (engine.magnetTimerSec * 1000).toLong(),
            slowmoRemainingMs = (engine.slowMoTimerSec * 1000).toLong(),
            revivesUsedAd = revivesUsedAd,
            revivesUsedCoins = revivesUsedCoins,
            mode = engine.mode,
            laneCount = 3,
            dailyTargetReached = engine.dailyTargetReached
        )
    }
}
