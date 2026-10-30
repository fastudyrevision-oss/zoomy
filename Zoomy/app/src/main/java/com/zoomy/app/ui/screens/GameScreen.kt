package com.zoomy.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoomy.app.data.UserPreferences
import com.zoomy.app.game.GameMode
import com.zoomy.app.game.RoadView
import com.zoomy.app.game.ZoomyEngine
import com.zoomy.app.ui.HapticsHelper
import com.zoomy.app.ui.components.BoosterButton
import com.zoomy.app.ui.components.ScreenBackground
import com.zoomy.app.ui.components.ZoomyIconButton
import com.zoomy.app.ui.theme.Grape
import com.zoomy.app.ui.theme.GrapeDeep
import com.zoomy.app.ui.theme.InkPrimary
import com.zoomy.app.ui.theme.InkSecondary
import com.zoomy.app.ui.theme.InkShadow
import com.zoomy.app.ui.theme.LimeDeep
import com.zoomy.app.ui.theme.LimeTint
import com.zoomy.app.ui.theme.Sky
import com.zoomy.app.ui.theme.StandardEasing
import com.zoomy.app.ui.theme.SunDeep
import com.zoomy.app.ui.theme.SurfaceBorderColor
import com.zoomy.app.ui.theme.SurfaceColor
import com.zoomy.app.ui.theme.TrackBgColor
import com.zoomy.app.ui.theme.ZoomyTypography
import com.zoomy.app.ui.theme.pressSpring
import kotlinx.coroutines.launch
import java.text.NumberFormat
import kotlin.math.roundToInt

// ALLOWED UI ADDITIONS #3:
// Daily mode Game screen: the "Best" pill text becomes "Daily · 3,000 to win".
// After claiming, the pill turns Lime tint fill with Lime deep text "+50 coins claimed" and Icons.Rounded.CheckCircle 16dp Lime deep.

@Composable
fun GameScreen(
    engine: ZoomyEngine,
    userPrefs: UserPreferences,
    hapticsHelper: HapticsHelper,
    onOpenPause: () -> Unit,
    onOpenGameOver: (finalScore: Int, coinsCollected: Int, distanceMeters: Float) -> Unit,
    onRequestBoosterAd: (String) -> Unit,
    onBoosterUsed: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scorePillScale = remember { Animatable(1f) }
    val coinCircleScale = remember { Animatable(1f) }

    DisposableEffect(engine) {
        engine.onGameOver = {
            onOpenGameOver(engine.score, engine.runCoins, engine.distance / 20f)
        }
        engine.onHapticCrash = {
            hapticsHelper.perform(HapticFeedbackType.LongPress)
        }
        engine.onHapticCloseCall = {
            hapticsHelper.perform(HapticFeedbackType.TextHandleMove)
        }
        engine.onCoinPop = {
            coroutineScope.launch {
                coinCircleScale.animateTo(1.15f, tween(60))
                coinCircleScale.animateTo(1.0f, pressSpring())
            }
        }
        engine.onComboScorePillPop = {
            coroutineScope.launch {
                scorePillScale.animateTo(1.08f, tween(60))
                scorePillScale.animateTo(1.0f, pressSpring())
            }
        }
        onDispose { }
    }

    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top row, height 72dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ZoomyIconButton(
                    icon = Icons.Rounded.Pause,
                    onClick = {
                        engine.isPaused = true
                        onOpenPause()
                    }
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.scale(scorePillScale.value)
                ) {
                    Text(
                        text = "SCORE",
                        style = ZoomyTypography.Caption.copy(color = InkSecondary)
                    )
                    RollingScoreText(score = engine.score)
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .scale(coinCircleScale.value),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val r = size.width / 2f
                        drawCircle(color = InkShadow, radius = r, center = Offset(r, r + 3.dp.toPx()))
                        drawCircle(color = SurfaceColor, radius = r, center = Offset(r, r))
                        drawCircle(color = SurfaceBorderColor, radius = r, center = Offset(r, r), style = Stroke(1.5.dp.toPx()))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MonetizationOn,
                            contentDescription = null,
                            tint = SunDeep,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = engine.runCoins.toString(),
                            style = ZoomyTypography.Number.copy(color = InkPrimary)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "COINS",
                            style = ZoomyTypography.Caption.copy(color = InkSecondary, fontSize = 9.sp, letterSpacing = 0.5.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Centered Best / Daily Status Pill
            if (engine.mode == GameMode.DAILY) {
                val isClaimed = engine.dailyTargetReached
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(CircleShape)
                        .background(if (isClaimed) LimeTint else SurfaceColor),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawRoundRect(
                            color = if (isClaimed) LimeDeep else SurfaceBorderColor,
                            topLeft = Offset.Zero,
                            size = size,
                            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                            style = Stroke(1.5.dp.toPx())
                        )
                    }
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isClaimed) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = LimeDeep,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+50 coins claimed",
                                style = ZoomyTypography.Label.copy(color = LimeDeep, fontSize = 12.sp)
                            )
                        } else {
                            Text(
                                text = "Daily · 3,000 to win",
                                style = ZoomyTypography.Label.copy(color = InkPrimary, fontSize = 12.sp)
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(CircleShape)
                        .background(SurfaceColor),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawRoundRect(
                            color = SurfaceBorderColor,
                            topLeft = Offset.Zero,
                            size = size,
                            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                            style = Stroke(1.5.dp.toPx())
                        )
                    }
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = GrapeDeep,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Best ${NumberFormat.getNumberInstance().format(userPrefs.bestScore)}",
                            style = ZoomyTypography.Label.copy(color = InkPrimary, fontSize = 12.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            RoadView(
                engine = engine,
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val speedFraction = ((engine.speed - 260f) / 360f).coerceIn(0f, 1f)
            val kmh = (engine.speed / 4f).roundToInt()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SPEED",
                    style = ZoomyTypography.Caption.copy(color = InkSecondary)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier.size(width = 200.dp, height = 10.dp)
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val r = size.height / 2f
                        drawRoundRect(
                            color = TrackBgColor,
                            topLeft = Offset.Zero,
                            size = size,
                            cornerRadius = CornerRadius(r, r)
                        )
                        val fillW = size.width * speedFraction
                        if (fillW > 0f) {
                            drawRoundRect(
                                color = Sky,
                                topLeft = Offset.Zero,
                                size = Size(fillW, size.height),
                                cornerRadius = CornerRadius(r, r)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "$kmh km/h",
                    style = ZoomyTypography.Number.copy(color = InkPrimary)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Booster row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BoosterButton(
                    icon = Icons.Rounded.Shield,
                    label = "Shield",
                    count = userPrefs.boosterShield,
                    iconTint = Sky,
                    onClick = {
                        if (userPrefs.boosterShield > 0) {
                            if (engine.activateShield()) {
                                onBoosterUsed("shield")
                            }
                        } else {
                            onRequestBoosterAd("shield")
                        }
                    }
                )

                BoosterButton(
                    icon = Icons.Rounded.Adjust,
                    label = "Magnet",
                    count = userPrefs.boosterMagnet,
                    iconTint = Grape,
                    onClick = {
                        if (userPrefs.boosterMagnet > 0) {
                            if (engine.activateMagnet()) {
                                onBoosterUsed("magnet")
                            }
                        } else {
                            onRequestBoosterAd("magnet")
                        }
                    }
                )

                BoosterButton(
                    icon = Icons.Rounded.Timer,
                    label = "Slow-mo",
                    count = userPrefs.boosterSlowmo,
                    iconTint = Sky,
                    onClick = {
                        if (userPrefs.boosterSlowmo > 0) {
                            if (engine.activateSlowMo()) {
                                onBoosterUsed("slowmo")
                            }
                        } else {
                            onRequestBoosterAd("slowmo")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RollingScoreText(score: Int) {
    Text(
        text = score.toString(),
        style = ZoomyTypography.Score.copy(color = InkPrimary)
    )
}
