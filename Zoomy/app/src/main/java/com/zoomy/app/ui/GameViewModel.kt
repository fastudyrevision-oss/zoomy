package com.zoomy.app.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zoomy.app.AppContainer
import com.zoomy.app.data.carIdToIndex
import com.zoomy.app.game.GameMode
import com.zoomy.app.game.GameSession
import com.zoomy.app.game.ZoomyEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class GameViewModel(
    private val container: AppContainer
) : ViewModel() {
    val prefsRepository = container.prefsRepository
    val soundManager = container.soundManager
    val adsManager = container.adsManager

    val engine = ZoomyEngine()
    val session = GameSession(engine, prefsRepository, soundManager, viewModelScope)

    val sessionState = session.sessionState

    fun startRun(mode: GameMode) {
        viewModelScope.launch {
            val userPrefs = prefsRepository.prefsFlow.first()
            val carIndex = carIdToIndex(userPrefs.equippedCar)
            session.startRun(mode, carIndex)
        }
    }

    fun onPauseRun() {
        engine.isPaused = true
        session.syncState()
    }

    fun onResumeRun() {
        engine.isPaused = false
        session.syncState()
    }

    fun onReviveViaAd(activity: Activity, onRevived: () -> Unit, onFailed: () -> Unit) {
        adsManager.showRewarded(
            activity = activity,
            onRewardEarned = {
                val ok = session.reviveViaAd()
                if (ok) onRevived() else onFailed()
            },
            onAdClosed = {},
            onAdNotReady = {
                container.showSnackbar("Ad isn't ready. Try again soon")
                onFailed()
            }
        )
    }

    fun onReviveViaCoins(onRevived: () -> Unit, onNotEnoughCoins: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentCoins = prefsRepository.prefsFlow.first().coins
            if (currentCoins < 50) {
                container.showSnackbar("Not enough coins")
                launch(Dispatchers.Main) { onNotEnoughCoins() }
            } else {
                val deducted = prefsRepository.deductCoins(50)
                if (deducted) {
                    launch(Dispatchers.Main) {
                        session.reviveViaCoins()
                        onRevived()
                    }
                }
            }
        }
    }

    fun onGameOverFinish(onFinished: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val userPrefs = prefsRepository.prefsFlow.first()
            val score = engine.score
            val distM = (engine.distance / 20f).toInt()
            val runCoins = engine.runCoins
            val multiplier = if (userPrefs.plusActive) 2 else 1
            val coinsEarned = runCoins * multiplier

            prefsRepository.recordGameOver()
            prefsRepository.addCoins(coinsEarned)

            if (engine.mode == GameMode.NORMAL) {
                prefsRepository.updateBestScore(score, distM)
            } else if (engine.mode == GameMode.DAILY) {
                prefsRepository.recordDailyScore(LocalDate.now().toString(), score)
            }

            launch(Dispatchers.Main) {
                onFinished()
            }
        }
    }

    fun onHomeFromPause(onFinished: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val userPrefs = prefsRepository.prefsFlow.first()
            val runCoins = engine.runCoins
            val multiplier = if (userPrefs.plusActive) 2 else 1
            val coinsEarned = runCoins * multiplier
            prefsRepository.addCoins(coinsEarned)
            launch(Dispatchers.Main) {
                onFinished()
            }
        }
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GameViewModel(container) as T
                }
            }
    }
}
