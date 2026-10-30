package com.zoomy.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoomy.app.ui.theme.InkPrimary
import com.zoomy.app.ui.theme.InkShadow
import com.zoomy.app.ui.theme.Quicksand
import com.zoomy.app.ui.theme.SurfaceBorderColor
import com.zoomy.app.ui.theme.SurfaceColor
import com.zoomy.app.ui.theme.TomatoDeep
import com.zoomy.app.ui.theme.TomatoTint
import com.zoomy.app.ui.theme.pressSpring
import kotlinx.coroutines.launch

// 3. SecondaryButton: height 56dp, pill, Surface fill, border 2dp Tomato tint, text Bold 16sp Tomato deep. Same press.
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    leadingIcon: ImageVector? = null,
    leadingContent: (@Composable () -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val scaleX = remember { Animatable(1f) }
    val scaleY = remember { Animatable(1f) }

    Box(
        modifier = modifier
            .scale(scaleX.value, scaleY.value)
            .height(height)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        coroutineScope.launch { scaleX.animateTo(1.06f, tween(80)) }
                        coroutineScope.launch { scaleY.animateTo(0.92f, tween(80)) }
                        tryAwaitRelease()
                        coroutineScope.launch { scaleX.animateTo(1f, pressSpring()) }
                        coroutineScope.launch { scaleY.animateTo(1f, pressSpring()) }
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val r = h / 2f

            // Shadow
            drawRoundRect(
                color = InkShadow,
                topLeft = Offset(0f, 4.dp.toPx()),
                size = Size(w, h),
                cornerRadius = CornerRadius(r, r)
            )
            // Surface fill
            drawRoundRect(
                color = SurfaceColor,
                topLeft = Offset.Zero,
                size = Size(w, h),
                cornerRadius = CornerRadius(r, r)
            )
            // Border 2dp Tomato tint
            drawRoundRect(
                color = TomatoTint,
                topLeft = Offset.Zero,
                size = Size(w, h),
                cornerRadius = CornerRadius(r, r),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = TomatoDeep,
                    modifier = Modifier.size(20.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = 6.dp))
            } else if (leadingContent != null) {
                leadingContent()
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = 6.dp))
            }

            Text(
                text = text,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = Quicksand,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TomatoDeep
                )
            )
        }
    }
}

// 4. CompactButton: height 44dp, pill, padding 20dp, ZoomyButton Primary style, text 14sp.
@Composable
fun CompactButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    iconTint: Color = Color.White
) {
    ZoomyButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        height = 44.dp,
        fontSize = 14.sp,
        leadingIcon = leadingIcon
    )
}

// 5. IconButton: 48dp circle, Surface fill + border, Card shadow at 60%, icon 24dp Ink.
// Press: scale (1.08, 0.92) then Press spring.
@Composable
fun ZoomyIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    tint: Color = InkPrimary
) {
    val coroutineScope = rememberCoroutineScope()
    val scaleX = remember { Animatable(1f) }
    val scaleY = remember { Animatable(1f) }

    Box(
        modifier = modifier
            .size(size)
            .scale(scaleX.value, scaleY.value)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        coroutineScope.launch { scaleX.animateTo(1.08f, tween(80)) }
                        coroutineScope.launch { scaleY.animateTo(0.92f, tween(80)) }
                        tryAwaitRelease()
                        coroutineScope.launch { scaleX.animateTo(1f, pressSpring()) }
                        coroutineScope.launch { scaleY.animateTo(1f, pressSpring()) }
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val s = this.size.width
            val r = s / 2f
            // Card shadow at 60%
            drawCircle(
                color = InkShadow.copy(alpha = InkShadow.alpha * 0.6f),
                radius = r,
                center = Offset(r, r + 4.dp.toPx())
            )
            // Surface fill
            drawCircle(
                color = SurfaceColor,
                radius = r,
                center = Offset(r, r)
            )
            // Surface border 1.5dp
            drawCircle(
                color = SurfaceBorderColor,
                radius = r,
                center = Offset(r, r),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

// 6. TextButton: Quicksand Bold 15sp Tomato deep, 48dp min height.
@Composable
fun ZoomyTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = Quicksand,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = 15.sp,
                color = TomatoDeep
            )
        )
    }
}
