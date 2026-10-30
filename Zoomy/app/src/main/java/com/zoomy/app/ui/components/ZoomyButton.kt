package com.zoomy.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoomy.app.ui.theme.ButtonPillShape
import com.zoomy.app.ui.theme.InkPrimary
import com.zoomy.app.ui.theme.Quicksand
import com.zoomy.app.ui.theme.StandardEasing
import com.zoomy.app.ui.theme.Sun
import com.zoomy.app.ui.theme.SunDeep
import com.zoomy.app.ui.theme.Tomato
import com.zoomy.app.ui.theme.TomatoDeep
import com.zoomy.app.ui.theme.TomatoLight
import com.zoomy.app.ui.theme.lighten
import com.zoomy.app.ui.theme.pressSpring
import kotlinx.coroutines.launch

// ZoomyButton (Primary/Accent): pill, vertical gradient (light top -> base bottom;
// Primary = Tomato light -> Tomato; Accent = Sun lightened 12% -> Sun),
// gloss: a white 35% ellipse covering the top 45% of the button inset 8dp horizontally,
// text Quicksand Bold 17sp UPPERCASE letterSpacing 0.5sp (Primary white; Accent Ink), Button shadow.
// Optional leading icon 24dp, 8dp gap.
// Idle (Play button only): scaleX 1.00 <-> 1.03 and scaleY 1.00 <-> 0.97 in counter-phase, 1600ms sinusoidal (Standard), infinite.
// Press: scale (1.06 x, 0.92 y) for 80ms then Press spring back to (1, 1), shadow alpha halves while pressed.

enum class ZoomyButtonStyle {
    Primary,
    Accent
}

@Composable
fun ZoomyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ZoomyButtonStyle = ZoomyButtonStyle.Primary,
    height: Dp = 56.dp,
    leadingIcon: ImageVector? = null,
    isPlayIdleBreathing: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 17.sp,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val scaleX = remember { Animatable(1f) }
    val scaleY = remember { Animatable(1f) }
    var isPressed by remember { mutableStateOf(false) }

    // Idle breathing (Play button only)
    val idleTransition = rememberInfiniteTransition(label = "IdleBreath")
    val idleScaleX by idleTransition.animateFloat(
        initialValue = 1.00f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = StandardEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdleX"
    )
    val idleScaleY by idleTransition.animateFloat(
        initialValue = 1.00f,
        targetValue = 0.97f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = StandardEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdleY"
    )

    val currentScaleX = if (isPlayIdleBreathing && !isPressed) idleScaleX else scaleX.value
    val currentScaleY = if (isPlayIdleBreathing && !isPressed) idleScaleY else scaleY.value

    val gradientColors = when (style) {
        ZoomyButtonStyle.Primary -> listOf(TomatoLight, Tomato)
        ZoomyButtonStyle.Accent -> listOf(Sun.lighten(0.12f), Sun)
    }
    val shadowColor = when (style) {
        ZoomyButtonStyle.Primary -> TomatoDeep.copy(alpha = if (isPressed) 0.225f else 0.45f)
        ZoomyButtonStyle.Accent -> SunDeep.copy(alpha = if (isPressed) 0.225f else 0.45f)
    }
    val textColor = when (style) {
        ZoomyButtonStyle.Primary -> Color.White
        ZoomyButtonStyle.Accent -> InkPrimary
    }

    Box(
        modifier = modifier
            .scale(currentScaleX, currentScaleY)
            .height(height)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        coroutineScope.launch {
                            scaleX.animateTo(1.06f, tween(80))
                        }
                        coroutineScope.launch {
                            scaleY.animateTo(0.92f, tween(80))
                        }
                        tryAwaitRelease()
                        isPressed = false
                        coroutineScope.launch {
                            scaleX.animateTo(1f, pressSpring())
                        }
                        coroutineScope.launch {
                            scaleY.animateTo(1f, pressSpring())
                        }
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Button background with shadow and gloss
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val r = h / 2f // Pill radius

            // Button shadow: y 8dp, blur 16dp in deep color at 45% (simulated)
            drawRoundRect(
                color = shadowColor,
                topLeft = Offset(0f, 6.dp.toPx()),
                size = Size(w, h),
                cornerRadius = CornerRadius(r, r)
            )

            // Button main body gradient
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = h
                ),
                topLeft = Offset.Zero,
                size = Size(w, h),
                cornerRadius = CornerRadius(r, r)
            )

            // Gloss: a white 35% ellipse covering the top 45% of the button inset 8dp horizontally
            val glossInsetX = 8.dp.toPx()
            val glossW = (w - glossInsetX * 2).coerceAtLeast(0f)
            val glossH = h * 0.45f
            drawRoundRect(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = Offset(glossInsetX, 2.dp.toPx()),
                size = Size(glossW, glossH),
                cornerRadius = CornerRadius(glossW / 2f, glossH / 2f)
            )
        }

        // Button Content: icon + text + optional trailing content
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text.uppercase(),
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = Quicksand,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = fontSize,
                    letterSpacing = 0.5.sp,
                    color = textColor
                )
            )
            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingContent()
            }
        }
    }
}
