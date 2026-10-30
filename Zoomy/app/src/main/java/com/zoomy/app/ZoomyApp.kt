package com.zoomy.app

import android.app.Application
import com.zoomy.app.ads.AdsManager
import com.zoomy.app.ads.ConsentManager
import com.zoomy.app.audio.SoundManager
import com.zoomy.app.billing.BillingManager
import com.zoomy.app.data.PrefsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class AppContainer(val context: Application) {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbarMessages = _snackbarMessages.asSharedFlow()

    fun showSnackbar(message: String) {
        applicationScope.launch {
            _snackbarMessages.emit(message)
        }
    }

    val prefsRepository = PrefsRepository(context, applicationScope)
    val soundManager = SoundManager(context, applicationScope)

    val billingManager = BillingManager(
        context = context,
        prefsRepository = prefsRepository,
        scope = applicationScope,
        onRewardSound = { soundManager.playReward() },
        showSnackbar = { showSnackbar(it) }
    )

    val adsManager = AdsManager(
        context = context,
        prefsRepository = prefsRepository,
        scope = applicationScope
    )

    val consentManager = ConsentManager(context)

    init {
        applicationScope.launch(Dispatchers.IO) {
            val userPrefs = prefsRepository.prefsFlow.first()
            soundManager.soundOn = userPrefs.soundOn
            // Check Plus daily grant
            prefsRepository.checkAndApplyPlusDailyGrant(LocalDate.now().toString())
        }
    }
}

class ZoomyApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.billingManager.startConnection()
        container.adsManager.preloadAds()
    }
}
