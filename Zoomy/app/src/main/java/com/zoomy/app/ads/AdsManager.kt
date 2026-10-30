package com.zoomy.app.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.zoomy.app.data.PrefsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AdsManager(
    private val context: Context,
    private val prefsRepository: PrefsRepository,
    private val scope: CoroutineScope
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewarded = false
    private var rewardedRetryCount = 0

    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false
    private var interstitialRetryCount = 0

    private suspend fun isAdsDisabled(): Boolean {
        val userPrefs = prefsRepository.prefsFlow.first()
        return userPrefs.removeAds || userPrefs.plusActive
    }

    fun preloadAds() {
        scope.launch(Dispatchers.Main) {
            if (isAdsDisabled()) return@launch
            loadRewarded()
            loadInterstitial()
        }
    }

    fun loadRewarded() {
        scope.launch(Dispatchers.Main) {
            if (isAdsDisabled() || isLoadingRewarded || rewardedAd != null) return@launch
            isLoadingRewarded = true

            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                AdIds.REWARDED,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isLoadingRewarded = false
                        rewardedRetryCount = 0
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        isLoadingRewarded = false
                        scheduleRewardedRetry()
                    }
                }
            )
        }
    }

    private fun scheduleRewardedRetry() {
        if (rewardedRetryCount >= 3) return
        val delayMs = when (rewardedRetryCount) {
            0 -> 2000L
            1 -> 4000L
            else -> 8000L
        }
        rewardedRetryCount++
        mainHandler.postDelayed({ loadRewarded() }, delayMs)
    }

    fun loadInterstitial() {
        scope.launch(Dispatchers.Main) {
            if (isAdsDisabled() || isLoadingInterstitial || interstitialAd != null) return@launch
            isLoadingInterstitial = true

            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                AdIds.INTERSTITIAL,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoadingInterstitial = false
                        interstitialRetryCount = 0
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isLoadingInterstitial = false
                        scheduleInterstitialRetry()
                    }
                }
            )
        }
    }

    private fun scheduleInterstitialRetry() {
        if (interstitialRetryCount >= 3) return
        val delayMs = when (interstitialRetryCount) {
            0 -> 2000L
            1 -> 4000L
            else -> 8000L
        }
        interstitialRetryCount++
        mainHandler.postDelayed({ loadInterstitial() }, delayMs)
    }

    fun isRewardedReady(): Boolean = rewardedAd != null

    fun showRewarded(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdClosed: () -> Unit,
        onAdNotReady: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            onAdNotReady()
            loadRewarded()
            return
        }

        var rewardGranted = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                prefsRepository.recordAdShown()
                loadRewarded()
                if (rewardGranted) {
                    onRewardEarned()
                }
                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                loadRewarded()
                onAdNotReady()
            }
        }

        ad.show(activity) { _ ->
            rewardGranted = true
        }
    }

    fun maybeShowInterstitial(
        activity: Activity,
        gameOverCount: Int,
        lastAdShownEpochMs: Long,
        onDismissed: () -> Unit
    ) {
        // Interstitial rule: shown only on every 3rd game over (multiple of 3, >= 3)
        // AND only after 90s (90000ms) since the last ad
        val now = System.currentTimeMillis()
        val isEvery3rd = gameOverCount > 0 && (gameOverCount % 3 == 0)
        val hasPassed90s = (now - lastAdShownEpochMs) >= 90000L

        scope.launch(Dispatchers.Main) {
            if (isAdsDisabled() || !isEvery3rd || !hasPassed90s) {
                onDismissed()
                return@launch
            }

            val ad = interstitialAd
            if (ad == null) {
                loadInterstitial()
                onDismissed()
                return@launch
            }

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    prefsRepository.recordAdShown()
                    loadInterstitial()
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    loadInterstitial()
                    onDismissed()
                }
            }

            ad.show(activity)
        }
    }
}
