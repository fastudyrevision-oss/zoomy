package com.zoomy.app.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zoomy.app.AppContainer
import com.zoomy.app.billing.Products
import com.zoomy.app.data.UserPreferences
import com.zoomy.app.data.carIdToIndex
import com.zoomy.app.data.getCarPrice
import com.zoomy.app.game.DailyRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AppViewModel(private val container: AppContainer) : ViewModel() {
    val prefsRepository = container.prefsRepository
    val soundManager = container.soundManager
    val adsManager = container.adsManager
    val consentManager = container.consentManager
    val billingManager = container.billingManager

    val userPrefs: StateFlow<UserPreferences> = prefsRepository.prefsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserPreferences()
        )

    val productDetailsMap = billingManager.productDetailsMap

    // Daily countdown string: "Resets in HH:MM:SS"
    private val _dailyCountdown = MutableStateFlow("Resets in 00:00:00")
    val dailyCountdown: StateFlow<String> = _dailyCountdown.asStateFlow()

    // In-flight action guard to prevent double taps
    private var inFlightDebounceUntil = 0L

    fun canPerformAction(): Boolean {
        val now = System.currentTimeMillis()
        if (now < inFlightDebounceUntil) return false
        inFlightDebounceUntil = now + 500L
        return true
    }

    init {
        // Ticker for daily countdown
        viewModelScope.launch {
            while (true) {
                updateDailyCountdown()
                delay(1000)
            }
        }
    }

    private fun updateDailyCountdown() {
        val now = LocalDateTime.now()
        val midnight = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT)
        val duration = Duration.between(now, midnight)
        val hours = duration.toHours()
        val minutes = (duration.toMinutes() % 60)
        val seconds = (duration.seconds % 60)
        _dailyCountdown.value = String.format(java.util.Locale.US, "Resets in %02d:%02d:%02d", hours, minutes, seconds)
    }

    fun onResumeCheck(activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            val todayStr = LocalDate.now().toString()
            prefsRepository.checkAndApplyPlusDailyGrant(todayStr)
            billingManager.restorePurchases(isUserInitiated = false)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundManager.soundOn = enabled
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepository.updateSound(enabled)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepository.updateHaptics(enabled)
        }
    }

    fun buyCar(carId: String, onNotEnoughCoins: () -> Unit) {
        if (!canPerformAction()) return
        val price = getCarPrice(carId)
        viewModelScope.launch(Dispatchers.IO) {
            val currentCoins = userPrefs.value.coins
            if (currentCoins < price) {
                container.showSnackbar("Not enough coins")
                launch(Dispatchers.Main) { onNotEnoughCoins() }
            } else {
                val bought = prefsRepository.buyCar(carId, price)
                if (bought) {
                    soundManager.playReward()
                }
            }
        }
    }

    fun equipCar(carId: String) {
        if (!canPerformAction()) return
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepository.equipCar(carId)
            soundManager.playTap()
        }
    }

    fun buyBooster(type: String, cost: Int, count: Int, onNotEnoughCoins: () -> Unit) {
        if (!canPerformAction()) return
        viewModelScope.launch(Dispatchers.IO) {
            val currentCoins = userPrefs.value.coins
            if (currentCoins < cost) {
                container.showSnackbar("Not enough coins")
                launch(Dispatchers.Main) { onNotEnoughCoins() }
            } else {
                val deducted = prefsRepository.deductCoins(cost)
                if (deducted) {
                    prefsRepository.addBooster(type, count)
                    soundManager.playReward()
                }
            }
        }
    }

    fun requestRewardedForBooster(activity: Activity, boosterType: String) {
        if (!canPerformAction()) return
        adsManager.showRewarded(
            activity = activity,
            onRewardEarned = {
                viewModelScope.launch(Dispatchers.IO) {
                    prefsRepository.addBooster(boosterType, 1)
                    soundManager.playReward()
                }
            },
            onAdClosed = {},
            onAdNotReady = {
                container.showSnackbar("Ad isn't ready. Try again soon")
            }
        )
    }

    fun purchaseProduct(activity: Activity, productId: String, basePlanId: String? = null) {
        if (!canPerformAction()) return
        billingManager.launch(activity, productId, basePlanId)
    }

    fun restorePurchases() {
        if (!canPerformAction()) return
        billingManager.restorePurchases(isUserInitiated = true)
    }

    fun addDebugCoins() {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepository.addCoins(1000)
            soundManager.playReward()
        }
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AppViewModel(container) as T
                }
            }
    }
}
