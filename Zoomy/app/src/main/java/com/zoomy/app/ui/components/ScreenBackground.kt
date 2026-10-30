package com.zoomy.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zoomy.app.ui.theme.CanvasBottom
import com.zoomy.app.ui.theme.CanvasTop
import kotlin.math.sin

// EVERY SCREEN BACKGROUND
// Canvas gradient plus 6 soft floating "puffs": circles (diameters 40, 64, 28, 52, 36, 72dp)
// filled white at 35%, at fixed positions spread over the screen, each drifting vertically by 24dp
// (sinusoidal, 9000-14000ms periods, offset phases).
// Static positions (dp from top-left): (30,140), (330,260), (60,520), (300,640), (180,780), (340,860).

private data class PuffInfo(
    val xDp: Float,
    val yDp: Float,
    val diameterDp: Float,
    val durationMs: Int,
    val phaseOffset: Float
)

private val puffs = listOf(
    PuffInfo(30f, 140f, 40f, 9000, 0f),
    PuffInfo(330f, 260f, 64f, 11000, 1.2f),
    PuffInfo(60f, 520f, 28f, 10000, 2.5f),
    PuffInfo(300f, 640f, 52f, 13000, 3.8f),
    PuffInfo(180f, 780f, 36f, 9500, 4.6f),
    PuffInfo(340f, 860f, 72f, 14000, 5.4f)
)

@Composable
fun ScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundPuffs")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BgTime"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CanvasTop, CanvasBottom)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val puffColor = Color(0x59FFFFFF) // white at 35%
            val driftRangePx = 24.dp.toPx()

            puffs.forEach { puff ->
                val periodSec = puff.durationMs / 1000f
                val angle = (2 * Math.PI * (time / periodSec) + puff.phaseOffset).toFloat()
                val dy = sin(angle) * (driftRangePx / 2f)

                val cx = puff.xDp.dp.toPx()
                val cy = puff.yDp.dp.toPx() + dy
                val radius = (puff.diameterDp / 2f).dp.toPx()

                drawCircle(
                    color = puffColor,
                    radius = radius,
                    center = Offset(cx, cy)
                )
            }
        }

        content()
    }
}
