package com.zoomy.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoomy.app.ui.components.CarSprite
import com.zoomy.app.ui.components.ScreenBackground
import com.zoomy.app.ui.theme.Baloo
import com.zoomy.app.ui.theme.EmphasizedDecelerateEasing
import com.zoomy.app.ui.theme.InkPrimary
import com.zoomy.app.ui.theme.InkSecondary
import com.zoomy.app.ui.theme.Quicksand
import com.zoomy.app.ui.theme.RoadColor
import com.zoomy.app.ui.theme.RoadDeepColor
import com.zoomy.app.ui.theme.Tomato
import com.zoomy.app.ui.theme.TrackBgColor
import com.zoomy.app.ui.theme.bounceSpring
import com.zoomy.app.ui.theme.getCarColor
import kotlinx.coroutines.delay

// [splash]
// A 120 x 200dp road strip (same road drawing) in the center;
// the Pip car drives in from below the screen to the center over 700ms EmphasizedDecelerate and stops with a squash (scaleY 0.94 -> 1, Bounce spring);
// then the wordmark "Zoomy" Baloo ExtraBold 48sp Tomato pops in (scale 0.5 -> 1 with Bounce spring) 500ms after the stop;
// the tagline "Dodge. Collect. Zoom." fades in 300ms after;
// a loading track (160dp x 8dp pill, Ink at 8%, Tomato fill) fills 0 -> 100% over 1400ms linear.
// hold 400ms after the loading track completes, then auto-navigate.

@Composable
fun SplashScreen(
    onNavigateHome: () -> Unit
) {
    val carOffsetY = remember { Animatable(300f) }
    val carScaleY = remember { Animatable(1f) }
    val wordmarkScale = remember { Animatable(0.5f) }
    val wordmarkAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val loadingProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Car drives in from below to center over 700ms EmphasizedDecelerate
        carOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(700, easing = EmphasizedDecelerateEasing)
        )
        // Stops with a squash (scaleY 0.94 -> 1, Bounce spring)
        carScaleY.snapTo(0.94f)
        carScaleY.animateTo(1f, bounceSpring())

        // 500ms after stop: wordmark pops in
        delay(500)
        wordmarkAlpha.snapTo(1f)
        wordmarkScale.animateTo(1f, bounceSpring())

        // 300ms after: tagline fades in
        delay(300)
        taglineAlpha.animateTo(1f, tween(300))

        // Loading track fills 0 -> 100% over 1400ms linear
        loadingProgress.animateTo(1f, tween(1400, easing = LinearEasing))

        // hold 400ms after loading completes, then auto-navigate
        delay(400)
        onNavigateHome()
    }

    ScreenBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Wordmark "Zoomy"
                Text(
                    text = "Zoomy",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Baloo,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        fontSize = 48.sp,
                        color = Tomato
                    ),
                    modifier = Modifier
                        .alpha(wordmarkAlpha.value)
                        .scale(wordmarkScale.value)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Tagline "Dodge. Collect. Zoom."
                Text(
                    text = "Dodge. Collect. Zoom.",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Quicksand,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = 15.sp,
                        color = InkSecondary
                    ),
                    modifier = Modifier.alpha(taglineAlpha.value)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Center 120 x 200dp road strip
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val w = size.width
                        val h = size.height
                        val r = 20.dp.toPx()

                        // Road strip body
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(RoadColor, RoadDeepColor)
                            ),
                            topLeft = Offset.Zero,
                            size = size,
                            cornerRadius = CornerRadius(r, r)
                        )
                        // Border
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.90f),
                            topLeft = Offset.Zero,
                            size = size,
                            cornerRadius = CornerRadius(r, r),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        // Dashed center line
                        val dashW = 3.dp.toPx()
                        val dashH = 20.dp.toPx()
                        val gap = 16.dp.toPx()
                        var y = 10.dp.toPx()
                        while (y < h - 10.dp.toPx()) {
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.85f),
                                topLeft = Offset((w - dashW) / 2f, y),
                                size = Size(dashW, dashH),
                                cornerRadius = CornerRadius(dashW / 2f, dashW / 2f)
                            )
                            y += dashH + gap
                        }
                    }

                    // Pip Car
                    Box(
                        modifier = Modifier
                            .offset(y = carOffsetY.value.dp)
                            .scale(scaleX = 1f, scaleY = carScaleY.value)
                    ) {
                        CarSprite(
                            color = getCarColor(1), // Pip
                            height = 76.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Loading track: 160dp x 8dp pill, Ink at 8%, Tomato fill
                Box(
                    modifier = Modifier
                        .size(width = 160.dp, height = 8.dp)
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val w = size.width
                        val h = size.height
                        val r = h / 2f

                        // Background track
                        drawRoundRect(
                            color = TrackBgColor,
                            topLeft = Offset.Zero,
                            size = size,
                            cornerRadius = CornerRadius(r, r)
                        )
                        // Filled progress
                        val fillW = w * loadingProgress.value
                        if (fillW > 0f) {
                            drawRoundRect(
                                color = Tomato,
                                topLeft = Offset.Zero,
                                size = Size(fillW, h),
                                cornerRadius = CornerRadius(r, r)
                            )
                        }
                    }
                }
            }
        }
    }
}
