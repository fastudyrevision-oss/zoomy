package com.zoomy.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoomy.app.ui.theme.Baloo
import com.zoomy.app.ui.theme.CardShape
import com.zoomy.app.ui.theme.EmphasizedDecelerateEasing
import com.zoomy.app.ui.theme.GrapeDeep
import com.zoomy.app.ui.theme.InkPrimary
import com.zoomy.app.ui.theme.InkSecondary
import com.zoomy.app.ui.theme.InkShadow
import com.zoomy.app.ui.theme.Lime
import com.zoomy.app.ui.theme.Quicksand
import com.zoomy.app.ui.theme.SunDeep
import com.zoomy.app.ui.theme.SurfaceBorderColor
import com.zoomy.app.ui.theme.SurfaceColor
import com.zoomy.app.ui.theme.ZoomyTypography
import com.zoomy.app.ui.theme.pressSpring
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// 7. Card: Surface fill + border, RoundedCornerShape(32), Card shadow, padding 20dp.
// Tappable: press scale 0.97, Press spring.
@Composable
fun ZoomyCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = CardShape,
    fillColor: Color = SurfaceColor,
    borderColor: Color? = SurfaceBorderColor,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    val clickModifier = if (onClick != null) {
        Modifier
            .scale(scale.value)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        coroutineScope.launch { scale.animateTo(0.97f, tween(80)) }
                        tryAwaitRelease()
                        coroutineScope.launch { scale.animateTo(1f, pressSpring()) }
                        onClick()
                    }
                )
            }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(clickModifier)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val r = shape.topStart.toPx(size, this)

            // Card shadow: y 8dp, blur 24dp, Ink at 10%
            drawRoundRect(
                color = InkShadow,
                topLeft = Offset(0f, 8.dp.toPx()),
                size = Size(w, h),
                cornerRadius = CornerRadius(r, r)
            )
            // Surface fill
            drawRoundRect(
                color = fillColor,
                topLeft = Offset.Zero,
                size = Size(w, h),
                cornerRadius = CornerRadius(r, r)
            )
            // Surface border 1.5dp
            if (borderColor != null) {
                drawRoundRect(
                    color = borderColor,
                    topLeft = Offset.Zero,
                    size = Size(w, h),
                    cornerRadius = CornerRadius(r, r),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        Box(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

// 8. StatChip: height 40dp pill, Surface fill + border, padding 14dp; icon 22dp + Number text.
// Variants: Coins (Icons.Rounded.MonetizationOn, Sun deep, "1,250"), Best (Icons.Rounded.EmojiEvents, Grape deep, "12,480").
// Coin counters count up over 800ms EmphasizedDecelerate on first entry.
enum class StatChipType {
    Coins,
    Best
}

@Composable
fun StatChip(
    type: StatChipType,
    value: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    var startValue by remember { mutableIntStateOf(0) }
    val animatedValue by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 500, easing = EmphasizedDecelerateEasing),
        label = "StatCount"
    )

    val icon = when (type) {
        StatChipType.Coins -> Icons.Rounded.MonetizationOn
        StatChipType.Best -> Icons.Rounded.EmojiEvents
    }
    val iconTint = when (type) {
        StatChipType.Coins -> SunDeep
        StatChipType.Best -> GrapeDeep
    }

    val formatted = NumberFormat.getNumberInstance(Locale.US).format(animatedValue)

    val tapMod = if (onClick != null || onLongClick != null) {
        Modifier
            .scale(scale.value)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        coroutineScope.launch { scale.animateTo(0.95f, tween(80)) }
                        tryAwaitRelease()
                        coroutineScope.launch { scale.animateTo(1f, pressSpring()) }
                    },
                    onTap = { onClick?.invoke() },
                    onLongPress = { onLongClick?.invoke() }
                )
            }
    } else Modifier

    Box(
        modifier = modifier
            .then(tapMod)
            .height(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val r = h / 2f

            drawRoundRect(
                color = InkShadow,
                topLeft = Offset(0f, 3.dp.toPx()),
                size = Size(w, h),
                cornerRadius = CornerRadius(r, r)
            )
            drawRoundRect(
                color = SurfaceColor,
                topLeft = Offset.Zero,
                size = Size(w, h),
                cornerRadius = CornerRadius(r, r)
            )
            drawRoundRect(
                color = SurfaceBorderColor,
                topLeft = Offset.Zero,
                size = Size(w, h),
                cornerRadius = CornerRadius(r, r),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = formatted,
                style = ZoomyTypography.Number.copy(color = InkPrimary)
            )
        }
    }
}

// 9. BoosterButton: 72dp wide x 80dp tall, RoundedCornerShape(28), Surface fill + border, Card shadow;
// icon 28dp over Label; a badge at the top-right (offset 6dp):
// 22dp circle Grape deep with white Number 12sp count, or an ad badge (Lime circle with Icons.Rounded.PlayArrow 14dp white) when count is 0.
@Composable
fun BoosterButton(
    icon: ImageVector,
    label: String,
    count: Int,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    Box(
        modifier = modifier
            .size(width = 72.dp, height = 80.dp)
            .scale(scale.value)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        coroutineScope.launch { scale.animateTo(0.95f, tween(80)) }
                        tryAwaitRelease()
                        coroutineScope.launch { scale.animateTo(1f, pressSpring()) }
                        onClick()
                    }
                )
            }
    ) {
        // Main card
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val r = 28.dp.toPx()

                // Card shadow
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
                // Surface border 1.5dp
                drawRoundRect(
                    color = SurfaceBorderColor,
                    topLeft = Offset.Zero,
                    size = Size(w, h),
                    cornerRadius = CornerRadius(r, r),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = ZoomyTypography.Label.copy(color = InkSecondary, fontSize = 11.sp)
                )
            }
        }

        // Top right badge (offset 6dp)
        // 22dp circle Grape deep with white Number 12sp count, or Lime circle with PlayArrow 14dp white when count == 0
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(if (count > 0) GrapeDeep else Lime),
            contentAlignment = Alignment.Center
        ) {
            if (count > 0) {
                Text(
                    text = count.toString(),
                    style = ZoomyTypography.Number.copy(
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 12.sp
                    )
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
