package com.zoomy.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoomy.app.audio.SoundManager
import com.zoomy.app.ui.HapticsHelper
import com.zoomy.app.ui.components.SecondaryButton
import com.zoomy.app.ui.components.ZoomyButton
import com.zoomy.app.ui.components.ZoomyButtonStyle
import com.zoomy.app.ui.components.ZoomyCard
import com.zoomy.app.ui.components.ZoomyTextButton
import com.zoomy.app.ui.theme.InkPrimary
import com.zoomy.app.ui.theme.InkScrim
import com.zoomy.app.ui.theme.InkSecondary
import com.zoomy.app.ui.theme.Lime
import com.zoomy.app.ui.theme.LimeDeep
import com.zoomy.app.ui.theme.LimeTint
import com.zoomy.app.ui.theme.OverlayCardShape
import com.zoomy.app.ui.theme.SkyDeep
import com.zoomy.app.ui.theme.SkyTint
import com.zoomy.app.ui.theme.StandardEasing
import com.zoomy.app.ui.theme.Sun
import com.zoomy.app.ui.theme.SunDeep
import com.zoomy.app.ui.theme.ZoomyTypography
import com.zoomy.app.ui.theme.bounceSpring
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import kotlin.math.sin

@Composable
fun GameOverOverlay(
    visible: Boolean,
    score: Int,
    bestScore: Int,
    distanceMeters: Float,
    coinsCollected: Int,
    plusActive: Boolean,
    revivesUsedAd: Int,
    revivesUsedCoins: Int,
    soundManager: SoundManager,
    hapticsHelper: HapticsHelper,
    onContinueFree: () -> Unit,
    onContinueCoins: () -> Unit,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isNewBest = score > bestScore

    // Shake animators for used revives or not enough coins
    val shakeFreeX = remember { Animatable(0f) }
    val shakeCoinsX = remember { Animatable(0f) }

    fun shake(animatable: Animatable<Float, *>) {
        coroutineScope.launch {
            // translationX +-6dp, 4 cycles, 300ms
            for (i in 0..3) {
                animatable.animateTo(6f, tween(37))
                animatable.animateTo(-6f, tween(37))
            }
            animatable.animateTo(0f, tween(37))
        }
    }

    LaunchedEffect(visible) {
        if (visible) {
            soundManager.playGameOver()
            if (isNewBest) {
                delay(300)
                soundManager.playNewBest()
            }
        }
    }

    val formattedDistance = if (distanceMeters >= 1000f) {
        String.format(java.util.Locale.US, "%.1f km", distanceMeters / 1000f)
    } else {
        "${distanceMeters.toInt()} m"
    }

    val finalCoinsEarned = coinsCollected * (if (plusActive) 2 else 1)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(220, easing = StandardEasing))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(InkScrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { -420 },
                    animationSpec = bounceSpring()
                ),
                exit = slideOutVertically(
                    targetOffsetY = { -420 },
                    animationSpec = tween(220, easing = StandardEasing)
                )
            ) {
                Box(
                    modifier = Modifier.width(320.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    ZoomyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = OverlayCardShape
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "CRASHED!",
                                style = ZoomyTypography.Caption.copy(color = InkSecondary)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = NumberFormat.getNumberInstance().format(score),
                                style = ZoomyTypography.Display.copy(color = InkPrimary)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(SkyTint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "DISTANCE",
                                            style = ZoomyTypography.Caption.copy(color = SkyDeep, fontSize = 10.sp)
                                        )
                                        Text(
                                            text = formattedDistance,
                                            style = ZoomyTypography.Number.copy(color = InkPrimary)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(LimeTint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "COINS",
                                            style = ZoomyTypography.Caption.copy(color = LimeDeep, fontSize = 10.sp)
                                        )
                                        Text(
                                            text = coinsCollected.toString(),
                                            style = ZoomyTypography.Number.copy(color = InkPrimary)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.MonetizationOn,
                                    contentDescription = null,
                                    tint = SunDeep,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+$finalCoinsEarned coins",
                                    style = ZoomyTypography.Label.copy(color = InkSecondary)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Continue Free (rewarded ad revive)
                            val isFreeUsed = revivesUsedAd >= 1
                            ZoomyButton(
                                text = "Continue",
                                onClick = {
                                    if (isFreeUsed) {
                                        shake(shakeFreeX)
                                    } else {
                                        hapticsHelper.perform(HapticFeedbackType.LongPress)
                                        onContinueFree()
                                    }
                                },
                                style = ZoomyButtonStyle.Accent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(x = shakeFreeX.value.dp)
                                    .alpha(if (isFreeUsed) 0.40f else 1.0f),
                                leadingIcon = Icons.Rounded.PlayCircle,
                                trailingContent = {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Lime)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "FREE",
                                            style = ZoomyTypography.Label.copy(color = Color.White, fontSize = 10.sp)
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Continue · 50 coins
                            val isCoinsUsed = revivesUsedCoins >= 1
                            SecondaryButton(
                                text = "Continue · 50 coins",
                                onClick = {
                                    if (isCoinsUsed) {
                                        shake(shakeCoinsX)
                                    } else {
                                        hapticsHelper.perform(HapticFeedbackType.LongPress)
                                        onContinueCoins()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(x = shakeCoinsX.value.dp)
                                    .alpha(if (isCoinsUsed) 0.40f else 1.0f),
                                leadingIcon = Icons.Rounded.MonetizationOn
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            ZoomyButton(
                                text = "Play again",
                                onClick = {
                                    hapticsHelper.perform(HapticFeedbackType.LongPress)
                                    onPlayAgain()
                                },
                                style = ZoomyButtonStyle.Primary,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ZoomyTextButton(
                                text = "Home",
                                onClick = onHome
                            )
                        }
                    }

                    if (isNewBest) {
                        Box(
                            modifier = Modifier
                                .offset(y = 0.dp)
                                .clip(CircleShape)
                                .background(Sun)
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = InkPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "NEW BEST!",
                                    style = ZoomyTypography.Caption.copy(color = InkPrimary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
