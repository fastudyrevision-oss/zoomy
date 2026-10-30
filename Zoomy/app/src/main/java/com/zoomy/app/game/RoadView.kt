package com.zoomy.app.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoomy.app.ui.components.CarSprite
import com.zoomy.app.ui.theme.Baloo
import com.zoomy.app.ui.theme.Danger
import com.zoomy.app.ui.theme.Grape
import com.zoomy.app.ui.theme.GrapeDeep
import com.zoomy.app.ui.theme.InkPrimary
import com.zoomy.app.ui.theme.InkShadow
import com.zoomy.app.ui.theme.Lime
import com.zoomy.app.ui.theme.LimeDeep
import com.zoomy.app.ui.theme.RoadColor
import com.zoomy.app.ui.theme.RoadDeepColor
import com.zoomy.app.ui.theme.RoadShape
import com.zoomy.app.ui.theme.Sky
import com.zoomy.app.ui.theme.SkyTint
import com.zoomy.app.ui.theme.Sun
import com.zoomy.app.ui.theme.SunDeep
import com.zoomy.app.ui.theme.SunTint
import com.zoomy.app.ui.theme.darken
import com.zoomy.app.ui.theme.getCarColor
import com.zoomy.app.ui.theme.lighten
import kotlin.math.sin

// GAME: ROAD AND ENGINE
// Road geometry: interior 340dp wide x 540dp tall, centered horizontally, 3 lanes of 113.33dp;
// lane center x = 56.67, 170, 283.33. Visual: Road color fill (vertical gradient Road -> Road deep),
// white 90% curb strips 6dp wide on both sides, lime grass strips 24dp wide outside both curbs with 5 tiny white flower dots
// and 3 Lime deep bushes (circles 14-22dp) scrolling with the road, rounded corners 28dp on the whole frame (clip),
// a 3dp white 90% frame stroke, floor shadow ellipse (width 280dp, height 18dp, Ink at 10%) 10dp below.
// Lane markings: white 90% dashes (4dp wide, 28dp long, 28dp gap) on the two lane borders scrolling down at world speed.

@Composable
fun RoadView(
    engine: ZoomyEngine,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val isAttract = engine.isAttractMode
    val scaleFactor = if (isAttract) 0.6f else 1.0f

    // 280dp asphalt road fits cleanly on 360dp phones with 20dp padding and 18dp grass strips (total 316dp)
    val interiorWidth = (if (isAttract) 204.dp else 280.dp)
    val interiorHeight = engine.roadHeight.dp
    val grassStripWidth = (if (isAttract) 16f else 18f).dp
    val totalRoadWidth = interiorWidth + grassStripWidth * 2

    // Continuous frame loop via withFrameNanos
    var frameTimeNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastTime != 0L) {
                    val dtSec = ((now - lastTime) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    engine.update(dtSec)
                }
                lastTime = now
                frameTimeNanos = now
            }
        }
    }

    // Input drag handling
    var totalDragX by remember { mutableFloatStateOf(0f) }

    // Pre-allocate paints and stationary lists to eliminate garbage collection stutters
    val textPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(169, 139, 255) // Grape
            textAlign = android.graphics.Paint.Align.CENTER
            style = android.graphics.Paint.Style.FILL
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }
    val strokePaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textAlign = android.graphics.Paint.Align.CENTER
            style = android.graphics.Paint.Style.STROKE
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    val bushSizes = remember { floatArrayOf(16f, 22f, 14f) }
    val bushOffsets = remember { floatArrayOf(20f, 90f, 150f) }
    val flowerOffsets = remember { floatArrayOf(10f, 50f, 80f, 120f, 160f) }

    Box(
        modifier = modifier
            .size(width = totalRoadWidth, height = interiorHeight + (if (isAttract) 10.dp else 24.dp)),
        contentAlignment = Alignment.TopCenter
    ) {
        // Floor shadow ellipse below the road frame
        Canvas(
            modifier = Modifier
                .size(
                    width = if (isAttract) 180.dp else 260.dp,
                    height = if (isAttract) 12.dp else 18.dp
                )
                .align(Alignment.BottomCenter)
        ) {
            drawOval(
                color = InkShadow,
                topLeft = Offset.Zero,
                size = size
            )
        }

        // Road Frame with Clip and Gesture Detection
        Box(
            modifier = Modifier
                .size(width = totalRoadWidth, height = interiorHeight)
                .offset(x = engine.crashEffect.shakeOffsetX.dp, y = 0.dp)
                .clip(RoadShape)
                .pointerInput(isAttract) {
                    if (isAttract) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startPos = down.position
                        var totalDragX = 0f
                        var swipeTriggered = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break

                            if (change.pressed) {
                                val dx = change.position.x - startPos.x
                                totalDragX = dx
                                if (!swipeTriggered) {
                                    if (totalDragX > 32f) {
                                        swipeTriggered = true
                                        engine.onRightInput()
                                        change.consume()
                                    } else if (totalDragX < -32f) {
                                        swipeTriggered = true
                                        engine.onLeftInput()
                                        change.consume()
                                    }
                                }
                            } else {
                                // Up event
                                if (!swipeTriggered && kotlin.math.abs(totalDragX) < 25f) {
                                    val midX = size.width / 2f
                                    if (startPos.x < midX) {
                                        engine.onLeftInput()
                                    } else {
                                        engine.onRightInput()
                                    }
                                }
                                break
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val _tick = frameTimeNanos
                val w = size.width
                val h = size.height
                val grassW = grassStripWidth.toPx()
                val curbW = 6.dp.toPx() * scaleFactor
                val roadStart = grassW + curbW
                val roadRealW = (w - roadStart * 2f).coerceAtLeast(1f)

                // 1. Grass strips (Lime fill) outside curbs
                drawRect(color = Lime, topLeft = Offset.Zero, size = Size(grassW, h))
                drawRect(color = Lime, topLeft = Offset(w - grassW, 0f), size = Size(grassW, h))

                // Grass decor scrolling with world distance
                val grassScroll = engine.distance * scaleFactor
                for (side in 0..1) {
                    val grassBaseX = if (side == 0) grassW / 2f else w - grassW / 2f
                    // 3 Lime deep bushes (circles 14-22dp)
                    for (b in 0..2) {
                        val initialY = bushOffsets[b] * scaleFactor
                        val currentY = (initialY + grassScroll) % (h + 40f) - 20f
                        drawCircle(
                            color = LimeDeep,
                            radius = (bushSizes[b] / 2f).dp.toPx() * scaleFactor,
                            center = Offset(grassBaseX, currentY)
                        )
                    }

                    // 5 tiny white flower dots
                    for (f in 0..4) {
                        val initialY = flowerOffsets[f] * scaleFactor
                        val currentY = (initialY + grassScroll) % (h + 20f) - 10f
                        drawCircle(
                            color = Color.White.copy(alpha = 0.9f),
                            radius = 2.5f.dp.toPx() * scaleFactor,
                            center = Offset(grassBaseX + (if (f % 2 == 0) -4f else 4f), currentY)
                        )
                    }
                }

                // 2. Curbs: white 90% strips 6dp wide
                drawRect(
                    color = Color.White.copy(alpha = 0.90f),
                    topLeft = Offset(grassW, 0f),
                    size = Size(curbW, h)
                )
                drawRect(
                    color = Color.White.copy(alpha = 0.90f),
                    topLeft = Offset(w - grassW - curbW, 0f),
                    size = Size(curbW, h)
                )

                // 3. Road asphalt fill: vertical gradient Road -> Road deep
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(RoadColor, RoadDeepColor),
                        startY = 0f,
                        endY = h
                    ),
                    topLeft = Offset(roadStart, 0f),
                    size = Size(roadRealW, h)
                )

                // 4. Lane markings: white 90% dashes (4dp wide, 28dp long, 28dp gap)
                // scrolling down at world speed
                val dashW = 4.dp.toPx() * scaleFactor
                val dashH = 28.dp.toPx() * scaleFactor
                val dashGap = 28.dp.toPx() * scaleFactor
                val period = dashH + dashGap
                val dashOffset = (engine.distance * scaleFactor) % period

                val lane1X = roadStart + (roadRealW / 3f)
                val lane2X = roadStart + (roadRealW * 2f / 3f)

                var yDash = -period + dashOffset
                while (yDash < h + period) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.90f),
                        topLeft = Offset(lane1X - dashW / 2f, yDash),
                        size = Size(dashW, dashH),
                        cornerRadius = CornerRadius(dashW / 2f, dashW / 2f)
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.90f),
                        topLeft = Offset(lane2X - dashW / 2f, yDash),
                        size = Size(dashW, dashH),
                        cornerRadius = CornerRadius(dashW / 2f, dashW / 2f)
                    )
                    yDash += period
                }

                // 5. Speed lines: when speed > 480 dp/s draw 6 thin white 30% vertical streaks (2dp x 60dp)
                if (engine.speed > 480f) {
                    val streakW = 2.dp.toPx() * scaleFactor
                    val streakH = 60.dp.toPx() * scaleFactor
                    val streakOffset = (engine.distance * 1.6f * scaleFactor) % (h + streakH)
                    val streakXPositions = listOf(
                        lane1X - 20.dp.toPx(),
                        lane1X + 20.dp.toPx(),
                        lane2X - 20.dp.toPx(),
                        lane2X + 20.dp.toPx(),
                        roadStart + 20.dp.toPx(),
                        w - roadStart - 20.dp.toPx()
                    )
                    streakXPositions.forEach { sx ->
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.30f),
                            topLeft = Offset(sx, streakOffset - streakH),
                            size = Size(streakW, streakH),
                            cornerRadius = CornerRadius(streakW / 2f, streakW / 2f)
                        )
                    }
                }

                // 6. Slow-mo overlay: Sky tint at 8%
                if (engine.slowMoTimerSec > 0f) {
                    drawRect(
                        color = SkyTint.copy(alpha = 0.08f),
                        topLeft = Offset.Zero,
                        size = size
                    )
                }

                // Scale entities down so cars and traffic are clearly compact with plenty of open space in lanes
                val entityScale = if (isAttract) 0.6f else 0.65f

                // 7. Draw Coins
                engine.coins.forEach { coin ->
                    val cx = roadStart + (coin.x / engine.roadWidth) * roadRealW
                    val cy = (coin.y * scaleFactor).dp.toPx()
                    val cr = (coin.radius * scaleFactor).dp.toPx() * (if (!isAttract) 0.75f else 1f)

                    // Gold coin circle
                    drawCircle(color = Sun, radius = cr, center = Offset(cx, cy))
                    drawCircle(color = SunDeep, radius = cr * 0.75f, center = Offset(cx, cy), style = Stroke(width = 1.5.dp.toPx()))
                    drawCircle(color = Color.White.copy(alpha = 0.6f), radius = cr * 0.3f, center = Offset(cx - cr * 0.25f, cy - cr * 0.25f))
                }

                // 8. Draw Obstacles
                engine.obstacles.forEach { obs ->
                    val ox = roadStart + (obs.x / engine.roadWidth) * roadRealW
                    val oy = (obs.y * scaleFactor).dp.toPx()
                    val ow = (obs.width).dp.toPx() * entityScale
                    val oh = (obs.height).dp.toPx() * entityScale

                    if (obs.isPopping) {
                        // Pop scale 1.0 -> 1.25 then 0 over 240ms
                        val p = obs.popProgress
                        val scale = if (p < 0.4f) 1f + 0.25f * (p / 0.4f) else (1.25f * (1f - (p - 0.4f) / 0.6f)).coerceAtLeast(0f)
                        scale(scale, pivot = Offset(ox, oy)) {
                            drawCircle(color = Color.White.copy(alpha = 0.8f * (1f - p)), radius = ow / 2f, center = Offset(ox, oy))
                        }
                    } else {
                        when (obs.type) {
                            ObstacleType.Car -> {
                                // Draw traffic car
                                val carCol = getCarColor(obs.carColorIndex)
                                val lightC = carCol.lighten(0.55f)
                                val deepC = carCol.darken(0.14f)
                                val bodyCorner = 0.28f * ow

                                // Drop shadow
                                drawRoundRect(
                                    color = InkShadow,
                                    topLeft = Offset(ox - ow / 2f, oy - oh / 2f + 0.04f * oh),
                                    size = Size(ow, oh),
                                    cornerRadius = CornerRadius(bodyCorner, bodyCorner)
                                )
                                // Body
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(lightC, carCol, deepC),
                                        startY = oy - oh / 2f,
                                        endY = oy + oh / 2f
                                    ),
                                    topLeft = Offset(ox - ow / 2f, oy - oh / 2f),
                                    size = Size(ow, oh),
                                    cornerRadius = CornerRadius(bodyCorner, bodyCorner)
                                )
                                // Windshield
                                val windW = 0.70f * ow
                                val windH = 0.22f * oh
                                drawRoundRect(
                                    color = SkyTint.copy(alpha = 0.90f),
                                    topLeft = Offset(ox - windW / 2f, oy - oh / 2f + 0.18f * oh),
                                    size = Size(windW, windH),
                                    cornerRadius = CornerRadius(windW * 0.2f, windW * 0.2f)
                                )
                                // Roof
                                val roofW = 0.70f * ow
                                val roofH = 0.24f * oh
                                drawRoundRect(
                                    color = carCol.lighten(0.12f),
                                    topLeft = Offset(ox - roofW / 2f, oy - oh / 2f + 0.38f * oh),
                                    size = Size(roofW, roofH),
                                    cornerRadius = CornerRadius(roofW * 0.18f, roofW * 0.18f)
                                )
                                // Headlights
                                val hlR = (0.14f * ow) / 2f
                                drawCircle(color = SunTint, radius = hlR, center = Offset(ox - ow * 0.35f, oy - oh * 0.42f))
                                drawCircle(color = SunTint, radius = hlR, center = Offset(ox + ow * 0.35f, oy - oh * 0.42f))
                                // Taillights
                                val tlR = (0.10f * ow) / 2f
                                drawCircle(color = Danger, radius = tlR, center = Offset(ox - ow * 0.35f, oy + oh * 0.44f))
                                drawCircle(color = Danger, radius = tlR, center = Offset(ox + ow * 0.35f, oy + oh * 0.44f))
                            }
                            ObstacleType.Truck -> {
                                val truckCol = RoadDeepColor
                                val bodyCorner = 0.20f * ow
                                drawRoundRect(
                                    color = InkShadow,
                                    topLeft = Offset(ox - ow / 2f, oy - oh / 2f + 4.dp.toPx()),
                                    size = Size(ow, oh),
                                    cornerRadius = CornerRadius(bodyCorner, bodyCorner)
                                )
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(truckCol.lighten(0.2f), truckCol),
                                        startY = oy - oh / 2f,
                                        endY = oy + oh / 2f
                                    ),
                                    topLeft = Offset(ox - ow / 2f, oy - oh / 2f),
                                    size = Size(ow, oh),
                                    cornerRadius = CornerRadius(bodyCorner, bodyCorner)
                                )
                                // Cargo box on back 62%
                                val cargoH = oh * 0.62f
                                drawRoundRect(
                                    color = Color(0x262B3A55),
                                    topLeft = Offset(ox - ow * 0.44f, oy + oh / 2f - cargoH - 4.dp.toPx()),
                                    size = Size(ow * 0.88f, cargoH),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                                // White 60% stripe
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.60f),
                                    topLeft = Offset(ox - ow * 0.1f, oy + oh / 2f - cargoH),
                                    size = Size(ow * 0.2f, cargoH * 0.85f),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                                // Front windshield
                                drawRoundRect(
                                    color = SkyTint.copy(alpha = 0.90f),
                                    topLeft = Offset(ox - ow * 0.36f, oy - oh * 0.42f),
                                    size = Size(ow * 0.72f, oh * 0.18f),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                            }
                            ObstacleType.Cone -> {
                                // Traffic cone: orange triangle / circle in Sun / Danger
                                val coneR = ow / 2f
                                drawCircle(color = SunDeep, radius = coneR, center = Offset(ox, oy))
                                drawCircle(color = Color(0xFFFF9A4D), radius = coneR * 0.7f, center = Offset(ox, oy))
                                drawCircle(color = Color.White.copy(alpha = 0.9f), radius = coneR * 0.35f, center = Offset(ox, oy))
                            }
                        }
                    }
                }

                // 9. Draw Player Car
                val px = roadStart + (engine.playerX / engine.roadWidth) * roadRealW
                val py = (engine.playerY * scaleFactor).dp.toPx()
                val pw = (engine.playerWidth).dp.toPx() * entityScale
                val ph = (engine.playerHeight).dp.toPx() * entityScale

                // Face / idle 1dp jitter at 30Hz
                val jitterY = (sin(engine.totalTimeSec * 30f * 2 * Math.PI.toFloat()) * 1f.dp.toPx()).coerceIn(-1.5f, 1.5f)

                // Crash spin angle and alpha
                val carAlpha = if (engine.crashEffect.active) {
                    engine.crashEffect.playerAlpha
                } else if (engine.invulnerableTimerSec > 0f) {
                    // Invulnerable: alpha pulses 100% <-> 40%, 200ms
                    if ((engine.invulnerableTimerSec * 5f).toInt() % 2 == 0) 1f else 0.4f
                } else {
                    1f
                }

                val finalRotation = if (engine.crashEffect.active) {
                    engine.crashEffect.spinAngleDeg
                } else {
                    engine.playerTiltDeg
                }

                if (carAlpha > 0f) {
                    rotate(finalRotation, pivot = Offset(px, py + jitterY)) {
                        // Headlights glow: Sun tint circle 1.6 x size, alpha 25% always on
                        drawCircle(
                            color = SunTint.copy(alpha = 0.25f * carAlpha),
                            radius = (pw * 0.7f),
                            center = Offset(px, py + jitterY - ph * 0.45f)
                        )

                        // Player Car Body
                        val carCol = getCarColor(engine.playerColorIndex)
                        val lightC = carCol.lighten(0.55f)
                        val deepC = carCol.darken(0.14f)
                        val bodyCorner = 0.28f * pw

                        // Soft drop shadow: Ink at 18%, offset y 0.04 * ph
                        drawRoundRect(
                            color = InkShadow.copy(alpha = 0.18f * carAlpha),
                            topLeft = Offset(px - pw / 2f, py + jitterY - ph / 2f + 0.04f * ph),
                            size = Size(pw, ph),
                            cornerRadius = CornerRadius(bodyCorner, bodyCorner)
                        )
                        // Wheels
                        val wheelW = 0.16f * pw
                        val wheelH = 0.20f * ph
                        val wheelCorner = CornerRadius(wheelW * 0.35f, wheelW * 0.35f)
                        val wheelColor = Color(0xD92B3A55).copy(alpha = 0.85f * carAlpha)
                        drawRoundRect(color = wheelColor, topLeft = Offset(px - pw / 2f - wheelW / 2f, py + jitterY - ph * 0.30f), size = Size(wheelW, wheelH), cornerRadius = wheelCorner)
                        drawRoundRect(color = wheelColor, topLeft = Offset(px - pw / 2f - wheelW / 2f, py + jitterY + ph * 0.22f), size = Size(wheelW, wheelH), cornerRadius = wheelCorner)
                        drawRoundRect(color = wheelColor, topLeft = Offset(px + pw / 2f - wheelW / 2f, py + jitterY - ph * 0.30f), size = Size(wheelW, wheelH), cornerRadius = wheelCorner)
                        drawRoundRect(color = wheelColor, topLeft = Offset(px + pw / 2f - wheelW / 2f, py + jitterY + ph * 0.22f), size = Size(wheelW, wheelH), cornerRadius = wheelCorner)

                        // Body
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(lightC.copy(alpha = carAlpha), carCol.copy(alpha = carAlpha), deepC.copy(alpha = carAlpha)),
                                startY = py + jitterY - ph / 2f,
                                endY = py + jitterY + ph / 2f
                            ),
                            topLeft = Offset(px - pw / 2f, py + jitterY - ph / 2f),
                            size = Size(pw, ph),
                            cornerRadius = CornerRadius(bodyCorner, bodyCorner)
                        )

                        // Windshield: (0.70 * pw by 0.22 * ph) in Sky tint at 90%
                        val windW = 0.70f * pw
                        val windH = 0.22f * ph
                        drawRoundRect(
                            color = SkyTint.copy(alpha = 0.90f * carAlpha),
                            topLeft = Offset(px - windW / 2f, py + jitterY - ph / 2f + 0.18f * ph),
                            size = Size(windW, windH),
                            cornerRadius = CornerRadius(windW * 0.2f, windW * 0.2f)
                        )

                        // Roof & Gloss
                        val roofW = 0.70f * pw
                        val roofH = 0.24f * ph
                        val roofY = py + jitterY - ph / 2f + 0.38f * ph
                        drawRoundRect(
                            color = carCol.lighten(0.12f).copy(alpha = carAlpha),
                            topLeft = Offset(px - roofW / 2f, roofY),
                            size = Size(roofW, roofH),
                            cornerRadius = CornerRadius(roofW * 0.18f, roofW * 0.18f)
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.45f * carAlpha),
                            topLeft = Offset(px - roofW * 0.45f, roofY + 0.03f * ph),
                            size = Size(0.50f * pw, 0.16f * ph),
                            cornerRadius = CornerRadius(0.25f * pw, 0.08f * ph)
                        )

                        // Goldie crown
                        if (engine.playerColorIndex == 8) {
                            val crownW = 0.50f * pw
                            val crownH = crownW * 0.55f
                            val crownPath = Path().apply {
                                val left = px - crownW / 2f
                                val bot = roofY + roofH * 0.75f
                                moveTo(left, bot)
                                lineTo(left + crownW, bot)
                                lineTo(left + crownW, bot - crownH)
                                lineTo(left + crownW * 0.75f, bot - crownH * 0.45f)
                                lineTo(left + crownW * 0.50f, bot - crownH)
                                lineTo(left + crownW * 0.25f, bot - crownH * 0.45f)
                                lineTo(left, bot - crownH)
                                close()
                            }
                            drawPath(crownPath, color = Sun.copy(alpha = carAlpha), style = Fill)
                            drawPath(crownPath, color = SunDeep.copy(alpha = carAlpha), style = Stroke(width = 1.5.dp.toPx()))
                        }

                        // Rear window
                        val rearW = 0.70f * pw
                        val rearH = 0.12f * ph
                        drawRoundRect(
                            color = SkyTint.copy(alpha = 0.90f * carAlpha),
                            topLeft = Offset(px - rearW / 2f, py + jitterY - ph / 2f + 0.62f * ph),
                            size = Size(rearW, rearH),
                            cornerRadius = CornerRadius(rearW * 0.2f, rearW * 0.2f)
                        )

                        // Headlights
                        val hlR = (0.14f * pw) / 2f
                        drawCircle(color = SunTint.copy(alpha = carAlpha), radius = hlR, center = Offset(px - pw * 0.35f, py + jitterY - ph * 0.42f))
                        drawCircle(color = SunTint.copy(alpha = carAlpha), radius = hlR, center = Offset(px + pw * 0.35f, py + jitterY - ph * 0.42f))
                        // Taillights
                        val tlR = (0.10f * pw) / 2f
                        drawCircle(color = Danger.copy(alpha = carAlpha), radius = tlR, center = Offset(px - pw * 0.35f, py + jitterY + ph * 0.44f))
                        drawCircle(color = Danger.copy(alpha = carAlpha), radius = tlR, center = Offset(px + pw * 0.35f, py + jitterY + ph * 0.44f))
                    }
                }

                // 10. Booster active visuals around car
                // Shield: Sky ring (3dp, Sky at 70%) pulses around car (scale 1.0 <-> 1.06, 600ms)
                if (engine.shieldActive) {
                    val pulse = 1.0f + 0.06f * sin(engine.totalTimeSec * 2 * Math.PI.toFloat() / 0.60f)
                    val ringR = (pw * 0.85f) * pulse
                    drawCircle(
                        color = Sky.copy(alpha = 0.70f),
                        radius = ringR,
                        center = Offset(px, py),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Magnet: Grape ring (2dp dashed, rotating 1 turn / 1200ms) shows around the car
                if (engine.magnetTimerSec > 0f) {
                    val rotDeg = (engine.totalTimeSec / 1.20f * 360f) % 360f
                    rotate(rotDeg, pivot = Offset(px, py)) {
                        drawCircle(
                            color = Grape,
                            radius = pw * 0.90f,
                            center = Offset(px, py),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                            )
                        )
                    }
                }

                // 11. Crash Effects: puff burst & sparkles
                if (engine.crashEffect.active) {
                    engine.crashEffect.puffs.forEach { puff ->
                        val puffX = px + kotlin.math.cos(puff.angleRad) * puff.currentDist.dp.toPx()
                        val puffY = py + kotlin.math.sin(puff.angleRad) * puff.currentDist.dp.toPx()
                        drawCircle(
                            color = Color.White.copy(alpha = puff.alpha),
                            radius = 6.dp.toPx(),
                            center = Offset(puffX, puffY)
                        )
                    }
                    engine.crashEffect.sparkles.forEach { sp ->
                        val spX = px + kotlin.math.cos(sp.angleRad) * sp.currentDist.dp.toPx()
                        val spY = py + kotlin.math.sin(sp.angleRad) * sp.currentDist.dp.toPx()
                        drawCircle(
                            color = Sun,
                            radius = 3.dp.toPx(),
                            center = Offset(spX, spY)
                        )
                    }
                }

                // 12. Coin sparkles
                engine.coinSparkles.forEach { csp ->
                    val cx = roadStart + (csp.x / engine.roadWidth) * roadRealW
                    val cy = (csp.y * scaleFactor).dp.toPx()
                    val p = csp.ageSec / csp.durationSec
                    val spDist = p * 18.dp.toPx()
                    for (i in 0 until 4) {
                        val ang = i * (Math.PI.toFloat() / 2f)
                        drawCircle(
                            color = SunTint.copy(alpha = 1f - p),
                            radius = 3.dp.toPx(),
                            center = Offset(cx + kotlin.math.cos(ang) * spDist, cy + kotlin.math.sin(ang) * spDist)
                        )
                    }
                }

                // 13. Floating Combo Text
                engine.floatingTexts.forEach { ft ->
                    val fx = roadStart + (ft.x / engine.roadWidth) * roadRealW
                    val fy = (ft.currentY * scaleFactor).dp.toPx()
                    val alpha = (1f - (ft.ageSec / ft.durationSec)).coerceIn(0f, 1f)

                    drawIntoCanvas { canvas ->
                        val textAlpha = (alpha * 255).toInt()
                        textPaint.textSize = 22.sp.toPx()
                        textPaint.alpha = textAlpha
                        strokePaint.textSize = 22.sp.toPx()
                        strokePaint.alpha = textAlpha
                        strokePaint.strokeWidth = 3.dp.toPx()

                        canvas.nativeCanvas.drawText(ft.text, fx, fy, strokePaint)
                        canvas.nativeCanvas.drawText(ft.text, fx, fy, textPaint)
                    }
                }

                // 14. Road Frame Stroke: 3dp white 90%
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.90f),
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}
