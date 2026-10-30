package com.zoomy.app.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoomy.app.billing.Products
import com.zoomy.app.data.carIdToIndex
import com.zoomy.app.game.GameMode
import com.zoomy.app.ui.AppViewModel
import com.zoomy.app.ui.GameViewModel
import com.zoomy.app.ui.HapticsHelper
import com.zoomy.app.ui.screens.GameOverOverlay
import com.zoomy.app.ui.screens.GameScreen
import com.zoomy.app.ui.screens.GarageScreen
import com.zoomy.app.ui.screens.HomeScreen
import com.zoomy.app.ui.screens.PauseOverlay
import com.zoomy.app.ui.screens.PlusScreen
import com.zoomy.app.ui.screens.ShopScreen
import com.zoomy.app.ui.screens.SplashScreen
import com.zoomy.app.ui.theme.EmphasizedDecelerateEasing
import com.zoomy.app.ui.theme.StandardEasing
import com.zoomy.app.ui.theme.Tomato
import com.zoomy.app.ui.theme.pushSpring
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot

sealed class ScreenRoute {
    object Splash : ScreenRoute()
    object Home : ScreenRoute()
    object Game : ScreenRoute()
    object Garage : ScreenRoute()
    object Shop : ScreenRoute()
    object Plus : ScreenRoute()
}

@Composable
fun ZoomyNavHost(
    appViewModel: AppViewModel,
    gameViewModel: GameViewModel
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<ScreenRoute>(ScreenRoute.Splash) }
    val userPrefs by appViewModel.userPrefs.collectAsStateWithLifecycle()
    val productDetailsMap by appViewModel.productDetailsMap.collectAsStateWithLifecycle()
    val dailyCountdown by appViewModel.dailyCountdown.collectAsStateWithLifecycle()

    val hapticsHelper = remember(userPrefs.hapticsOn) {
        HapticsHelper(hapticFeedback, userPrefs.hapticsOn)
    }

    var showPauseOverlay by remember { mutableStateOf(false) }
    var showGameOverOverlay by remember { mutableStateOf(false) }
    var lastFinalScore by remember { mutableIntStateOf(0) }
    var lastCoinsCollected by remember { mutableIntStateOf(0) }
    var lastDistanceMeters by remember { mutableFloatStateOf(0f) }

    val irisScale = remember { Animatable(0f) }
    val irisAlpha = remember { Animatable(1f) }
    var isIrisActive by remember { mutableStateOf(false) }
    var irisCenterYFactor by remember { mutableFloatStateOf(0.58f) } // default: Play button center

    val sessionState by gameViewModel.sessionState.collectAsStateWithLifecycle()

    // Activity Lifecycle Observer: on ON_PAUSE, pause game and show Pause overlay
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                if (currentScreen is ScreenRoute.Game && !showGameOverOverlay) {
                    gameViewModel.onPauseRun()
                    showPauseOverlay = true
                }
            } else if (event == Lifecycle.Event.ON_RESUME) {
                activity?.let { appViewModel.onResumeCheck(it) }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Android System Back Button Handling
    BackHandler {
        when (currentScreen) {
            is ScreenRoute.Game -> {
                if (showGameOverOverlay) {
                    showGameOverOverlay = false
                    currentScreen = ScreenRoute.Home
                } else if (showPauseOverlay) {
                    showPauseOverlay = false
                    gameViewModel.onResumeRun()
                } else {
                    gameViewModel.onPauseRun()
                    showPauseOverlay = true
                }
            }
            is ScreenRoute.Garage -> currentScreen = ScreenRoute.Home
            is ScreenRoute.Shop -> currentScreen = ScreenRoute.Home
            is ScreenRoute.Plus -> currentScreen = ScreenRoute.Home
            is ScreenRoute.Home -> activity?.finish()
            is ScreenRoute.Splash -> {}
        }
    }

    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }
    val maxRadius = hypot(screenWidthPx, screenHeightPx)

    fun launchGameRun(mode: GameMode, fromDailyCard: Boolean) {
        irisCenterYFactor = if (fromDailyCard) 0.76f else 0.58f
        isIrisActive = true
        coroutineScope.launch {
            irisScale.snapTo(0f)
            irisAlpha.snapTo(1f)
            launch {
                irisScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(520, easing = EmphasizedDecelerateEasing)
                )
            }
            delay(320)
            gameViewModel.startRun(mode)
            showPauseOverlay = false
            showGameOverOverlay = false
            currentScreen = ScreenRoute.Game
            delay(200)
            irisAlpha.animateTo(0f, tween(200))
            isIrisActive = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                when {
                    initialState is ScreenRoute.Splash && targetState is ScreenRoute.Home -> {
                        (fadeIn(animationSpec = tween(250, delayMillis = 150, easing = StandardEasing)) +
                                scaleIn(initialScale = 0.97f, animationSpec = tween(250, delayMillis = 150, easing = StandardEasing)))
                            .togetherWith(fadeOut(animationSpec = tween(150, easing = StandardEasing)))
                    }
                    initialState is ScreenRoute.Home && targetState is ScreenRoute.Game -> {
                        fadeIn(tween(200, delayMillis = 300)).togetherWith(fadeOut(tween(150)))
                    }
                    initialState is ScreenRoute.Game && targetState is ScreenRoute.Home -> {
                        fadeIn(tween(200, delayMillis = 200)).togetherWith(fadeOut(tween(150)))
                    }
                    (initialState is ScreenRoute.Home && (targetState is ScreenRoute.Garage || targetState is ScreenRoute.Shop)) ||
                    (initialState is ScreenRoute.Shop && targetState is ScreenRoute.Garage) -> {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = pushSpring()
                        ).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { -(it * 0.2f).toInt() },
                                animationSpec = pushSpring()
                            ) + fadeOut(animationSpec = pushSpring(), targetAlpha = 0.6f)
                        )
                    }
                    ((initialState is ScreenRoute.Garage || initialState is ScreenRoute.Shop) && targetState is ScreenRoute.Home) ||
                    (initialState is ScreenRoute.Garage && targetState is ScreenRoute.Shop) -> {
                        slideInHorizontally(
                            initialOffsetX = { -(it * 0.2f).toInt() },
                            animationSpec = tween(350, easing = StandardEasing)
                        ).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(350, easing = StandardEasing)
                            )
                        )
                    }
                    targetState is ScreenRoute.Plus -> {
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(450, easing = EmphasizedDecelerateEasing)
                        ).togetherWith(
                            fadeOut(animationSpec = tween(300))
                        )
                    }
                    initialState is ScreenRoute.Plus -> {
                        fadeIn(animationSpec = tween(200)).togetherWith(
                            slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(320, easing = StandardEasing)
                            )
                        )
                    }
                    else -> fadeIn(tween(250)).togetherWith(fadeOut(tween(250)))
                }
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                is ScreenRoute.Splash -> {
                    SplashScreen(
                        onNavigateHome = {
                            currentScreen = ScreenRoute.Home
                        }
                    )
                }
                is ScreenRoute.Home -> {
                    HomeScreen(
                        userPrefs = userPrefs,
                        dailyCountdown = dailyCountdown,
                        hapticsHelper = hapticsHelper,
                        onNavigatePlay = {
                            launchGameRun(GameMode.NORMAL, fromDailyCard = false)
                        },
                        onNavigateDaily = {
                            launchGameRun(GameMode.DAILY, fromDailyCard = true)
                        },
                        onNavigateGarage = { currentScreen = ScreenRoute.Garage },
                        onNavigateShop = { currentScreen = ScreenRoute.Shop },
                        onNavigatePlus = { currentScreen = ScreenRoute.Plus },
                        onDebugAddCoins = { appViewModel.addDebugCoins() }
                    )
                }
                is ScreenRoute.Game -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        GameScreen(
                            engine = gameViewModel.engine,
                            userPrefs = userPrefs,
                            hapticsHelper = hapticsHelper,
                            onOpenPause = {
                                gameViewModel.onPauseRun()
                                showPauseOverlay = true
                            },
                            onOpenGameOver = { finalScore, coins, dist ->
                                lastFinalScore = finalScore
                                lastCoinsCollected = coins
                                lastDistanceMeters = dist
                                gameViewModel.onGameOverFinish {
                                    showGameOverOverlay = true
                                }
                            },
                            onRequestBoosterAd = { boosterType ->
                                activity?.let {
                                    appViewModel.requestRewardedForBooster(it, boosterType)
                                }
                            },
                            onBoosterUsed = { boosterType ->
                                coroutineScope.launch {
                                    when (boosterType) {
                                        "shield" -> appViewModel.prefsRepository.setShieldCount(userPrefs.boosterShield - 1)
                                        "magnet" -> appViewModel.prefsRepository.setMagnetCount(userPrefs.boosterMagnet - 1)
                                        "slowmo" -> appViewModel.prefsRepository.setSlowMoCount(userPrefs.boosterSlowmo - 1)
                                    }
                                }
                            }
                        )

                        // Pause overlay
                        PauseOverlay(
                            visible = showPauseOverlay,
                            soundOn = userPrefs.soundOn,
                            hapticsOn = userPrefs.hapticsOn,
                            onSoundChanged = { appViewModel.setSoundEnabled(it) },
                            onHapticsChanged = { appViewModel.setHapticsEnabled(it) },
                            showPrivacySettings = appViewModel.consentManager.isPrivacyOptionsRequired(),
                            onPrivacySettings = {
                                activity?.let {
                                    appViewModel.consentManager.showPrivacyOptionsForm(it) {}
                                }
                            },
                            onResume = {
                                showPauseOverlay = false
                                gameViewModel.onResumeRun()
                            },
                            onRestart = {
                                showPauseOverlay = false
                                gameViewModel.startRun(gameViewModel.engine.mode)
                            },
                            onHome = {
                                showPauseOverlay = false
                                gameViewModel.onHomeFromPause {
                                    isIrisActive = true
                                    coroutineScope.launch {
                                        irisAlpha.snapTo(1f)
                                        irisScale.snapTo(1f)
                                        irisScale.animateTo(0f, tween(400, easing = StandardEasing))
                                        currentScreen = ScreenRoute.Home
                                        isIrisActive = false
                                    }
                                }
                            }
                        )

                        // Game Over overlay
                        GameOverOverlay(
                            visible = showGameOverOverlay,
                            score = lastFinalScore,
                            bestScore = userPrefs.bestScore,
                            distanceMeters = lastDistanceMeters,
                            coinsCollected = lastCoinsCollected,
                            plusActive = userPrefs.plusActive,
                            revivesUsedAd = sessionState.revivesUsedAd,
                            revivesUsedCoins = sessionState.revivesUsedCoins,
                            soundManager = appViewModel.soundManager,
                            hapticsHelper = hapticsHelper,
                            onContinueFree = {
                                activity?.let { act ->
                                    gameViewModel.onReviveViaAd(
                                        activity = act,
                                        onRevived = { showGameOverOverlay = false },
                                        onFailed = {}
                                    )
                                }
                            },
                            onContinueCoins = {
                                gameViewModel.onReviveViaCoins(
                                    onRevived = { showGameOverOverlay = false },
                                    onNotEnoughCoins = {}
                                )
                            },
                            onPlayAgain = {
                                showGameOverOverlay = false
                                if (activity != null) {
                                    appViewModel.adsManager.maybeShowInterstitial(
                                        activity = activity,
                                        gameOverCount = userPrefs.gameOverCount,
                                        lastAdShownEpochMs = userPrefs.lastAdShownEpochMs,
                                        onDismissed = {
                                            gameViewModel.startRun(gameViewModel.engine.mode)
                                        }
                                    )
                                } else {
                                    gameViewModel.startRun(gameViewModel.engine.mode)
                                }
                            },
                            onHome = {
                                showGameOverOverlay = false
                                if (activity != null) {
                                    appViewModel.adsManager.maybeShowInterstitial(
                                        activity = activity,
                                        gameOverCount = userPrefs.gameOverCount,
                                        lastAdShownEpochMs = userPrefs.lastAdShownEpochMs,
                                        onDismissed = {
                                            currentScreen = ScreenRoute.Home
                                        }
                                    )
                                } else {
                                    currentScreen = ScreenRoute.Home
                                }
                            }
                        )
                    }
                }
                is ScreenRoute.Garage -> {
                    GarageScreen(
                        userPrefs = userPrefs,
                        onBack = { currentScreen = ScreenRoute.Home },
                        onEquipCar = { carId -> appViewModel.equipCar(carId) },
                        onBuyCar = { carId, onShake ->
                            appViewModel.buyCar(carId, onShake)
                        },
                        onNavigatePlus = { currentScreen = ScreenRoute.Plus }
                    )
                }
                is ScreenRoute.Shop -> {
                    ShopScreen(
                        userPrefs = userPrefs,
                        productDetailsMap = productDetailsMap,
                        onBack = { currentScreen = ScreenRoute.Home },
                        onNavigatePlus = { currentScreen = ScreenRoute.Plus },
                        onBuyCoinPack = { productId ->
                            activity?.let { appViewModel.purchaseProduct(it, productId) }
                        },
                        onBuyBooster = { type, cost, count, onShake ->
                            appViewModel.buyBooster(type, cost, count, onShake)
                        },
                        onBuyRemoveAds = {
                            activity?.let { appViewModel.purchaseProduct(it, Products.REMOVE_ADS) }
                        }
                    )
                }
                is ScreenRoute.Plus -> {
                    PlusScreen(
                        productDetailsMap = productDetailsMap,
                        onClose = { currentScreen = ScreenRoute.Home },
                        onStartTrial = { isYearly ->
                            activity?.let {
                                val plan = if (isYearly) Products.BASE_PLAN_YEARLY else Products.BASE_PLAN_MONTHLY
                                appViewModel.purchaseProduct(it, Products.ZOOMY_PLUS, plan)
                            }
                        },
                        onRestore = {
                            appViewModel.restorePurchases()
                        }
                    )
                }
            }
        }

        // Iris Reveal
        if (isIrisActive) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height * irisCenterYFactor
                val currentR = maxRadius * irisScale.value
                drawCircle(
                    color = Tomato.copy(alpha = irisAlpha.value),
                    radius = currentR,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}
