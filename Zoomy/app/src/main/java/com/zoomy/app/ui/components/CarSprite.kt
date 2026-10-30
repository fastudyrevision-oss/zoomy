package com.zoomy.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zoomy.app.ui.theme.Danger
import com.zoomy.app.ui.theme.InkShadowCar
import com.zoomy.app.ui.theme.InkTruckCargo
import com.zoomy.app.ui.theme.InkWheel
import com.zoomy.app.ui.theme.SkyTint
import com.zoomy.app.ui.theme.Sun
import com.zoomy.app.ui.theme.SunDeep
import com.zoomy.app.ui.theme.SunTint
import com.zoomy.app.ui.theme.darken
import com.zoomy.app.ui.theme.lighten

// CarSprite(color, height, crown): draws one top-down toy car, width = 0.52 x height.
// Body: rounded rectangle (corner 0.28 x width) with a vertical gradient (light top -> base -> deep bottom).
// Soft drop shadow: Ink at 18%, offset y 0.04 x height.
// Windshield: rounded rectangle (0.70 x width by 0.22 x height) in Sky tint at 90%, near the front (top) at 0.18 x height from the top;
// rear window 0.70 x width by 0.12 x height at 0.62 x height.
// Roof: rounded rectangle 0.70 x width by 0.24 x height between them, base color lightened 12%.
// Gloss: a white 45% ellipse (0.5 x width by 0.16 x height) on the roof's left.
// Headlights: two circles of 0.14 x width in Sun tint at the front corners (inset 0.10 x width) and two taillights of 0.10 x width in Danger at the back.
// Wheels: four rounded rectangles (0.16 x width by 0.20 x height) in Ink at 85%, half outside the body at 0.20 and 0.72 x height.
// Crown (Goldie): 5-point Sun crown width 0.5 x width on the roof with Sun deep outline 1.5dp.
// Draw with cached Paths/Brushes; no per-frame allocation.
// Trucks (traffic only) use CarSprite scaled to 1.4 x height with a cargo box: rounded rectangle on the back 62% in Ink at 15% fill with a white 60% stripe.

@Composable
fun CarSprite(
    color: Color,
    height: Dp,
    crown: Boolean = false,
    isTruck: Boolean = false,
    modifier: Modifier = Modifier
) {
    val width = height * 0.52f
    val lightColor = remember(color) { color.lighten(0.55f) }
    val deepColor = remember(color) { color.darken(0.14f) }
    val roofColor = remember(color) { color.lighten(0.12f) }

    Canvas(
        modifier = modifier.size(width = width, height = height)
    ) {
        val w = size.width
        val h = size.height

        // 1. Soft drop shadow: Ink at 18%, offset y 0.04 * h
        val shadowOffsetY = 0.04f * h
        val bodyCorner = 0.28f * w
        drawRoundRect(
            color = InkShadowCar,
            topLeft = Offset(0f, shadowOffsetY),
            size = Size(w, h),
            cornerRadius = CornerRadius(bodyCorner, bodyCorner)
        )

        // 2. Wheels: four rounded rectangles (0.16 * w by 0.20 * h) in Ink at 85%, half outside body at 0.20 and 0.72 * h
        val wheelW = 0.16f * w
        val wheelH = 0.20f * h
        val wheelCorner = CornerRadius(wheelW * 0.35f, wheelW * 0.35f)

        // Left wheels (half outside: -wheelW / 2)
        drawRoundRect(
            color = InkWheel,
            topLeft = Offset(-wheelW / 2f, 0.20f * h - wheelH / 2f),
            size = Size(wheelW, wheelH),
            cornerRadius = wheelCorner
        )
        drawRoundRect(
            color = InkWheel,
            topLeft = Offset(-wheelW / 2f, 0.72f * h - wheelH / 2f),
            size = Size(wheelW, wheelH),
            cornerRadius = wheelCorner
        )
        // Right wheels (w - wheelW / 2)
        drawRoundRect(
            color = InkWheel,
            topLeft = Offset(w - wheelW / 2f, 0.20f * h - wheelH / 2f),
            size = Size(wheelW, wheelH),
            cornerRadius = wheelCorner
        )
        drawRoundRect(
            color = InkWheel,
            topLeft = Offset(w - wheelW / 2f, 0.72f * h - wheelH / 2f),
            size = Size(wheelW, wheelH),
            cornerRadius = wheelCorner
        )

        // 3. Body: rounded rectangle with vertical gradient (light top -> base -> deep bottom)
        val bodyBrush = Brush.verticalGradient(
            colors = listOf(lightColor, color, deepColor),
            startY = 0f,
            endY = h
        )
        drawRoundRect(
            brush = bodyBrush,
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = CornerRadius(bodyCorner, bodyCorner)
        )

        if (!isTruck) {
            // Standard car details:
            // Windshield: (0.70 * w by 0.22 * h) in Sky tint at 90%, near the front (top) at 0.18 * h
            val windW = 0.70f * w
            val windH = 0.22f * h
            val windX = (w - windW) / 2f
            val windY = 0.18f * h
            val glassColor = SkyTint.copy(alpha = 0.90f)
            val glassCorner = CornerRadius(windW * 0.20f, windW * 0.20f)

            drawRoundRect(
                color = glassColor,
                topLeft = Offset(windX, windY),
                size = Size(windW, windH),
                cornerRadius = glassCorner
            )

            // Roof: rounded rectangle 0.70 * w by 0.24 * h between windshield and rear window
            val roofW = 0.70f * w
            val roofH = 0.24f * h
            val roofX = (w - roofW) / 2f
            val roofY = windY + windH - 0.02f * h // seamlessly between them
            val roofCorner = CornerRadius(roofW * 0.18f, roofW * 0.18f)

            // Rear window: 0.70 * w by 0.12 * h at 0.62 * h
            val rearW = 0.70f * w
            val rearH = 0.12f * h
            val rearX = (w - rearW) / 2f
            val rearY = 0.62f * h

            drawRoundRect(
                color = glassColor,
                topLeft = Offset(rearX, rearY),
                size = Size(rearW, rearH),
                cornerRadius = glassCorner
            )

            // Draw Roof over them
            drawRoundRect(
                color = roofColor,
                topLeft = Offset(roofX, roofY),
                size = Size(roofW, roofH),
                cornerRadius = roofCorner
            )

            // Gloss: a white 45% ellipse (0.5 * w by 0.16 * h) on roof's left
            val glossW = 0.50f * w
            val glossH = 0.16f * h
            drawRoundRect(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(roofX + 0.04f * w, roofY + 0.03f * h),
                size = Size(glossW, glossH),
                cornerRadius = CornerRadius(glossW / 2f, glossH / 2f)
            )

            // Crown (Goldie): 5-point Sun crown width 0.5 * w on the roof with Sun deep outline 1.5dp
            if (crown) {
                val crownW = 0.50f * w
                val crownH = crownW * 0.55f
                val crownX = (w - crownW) / 2f
                val crownY = roofY + (roofH - crownH) / 2f

                val crownPath = Path().apply {
                    moveTo(crownX, crownY + crownH)
                    lineTo(crownX + crownW, crownY + crownH)
                    lineTo(crownX + crownW, crownY)
                    lineTo(crownX + crownW * 0.75f, crownY + crownH * 0.45f)
                    lineTo(crownX + crownW * 0.50f, crownY)
                    lineTo(crownX + crownW * 0.25f, crownY + crownH * 0.45f)
                    lineTo(crownX, crownY)
                    close()
                }
                drawPath(crownPath, color = Sun, style = Fill)
                drawPath(crownPath, color = SunDeep, style = Stroke(width = 1.5.dp.toPx()))
            }
        } else {
            // Truck cargo box: rounded rectangle on the back 62% in Ink at 15% fill with a white 60% stripe
            val cargoH = 0.62f * h
            val cargoW = 0.88f * w
            val cargoX = (w - cargoW) / 2f
            val cargoY = h - cargoH - 0.04f * h
            val cargoCorner = CornerRadius(cargoW * 0.15f, cargoW * 0.15f)

            // Cabin windshield (front 25%)
            val windW = 0.72f * w
            val windH = 0.18f * h
            val windX = (w - windW) / 2f
            val windY = 0.10f * h
            drawRoundRect(
                color = SkyTint.copy(alpha = 0.90f),
                topLeft = Offset(windX, windY),
                size = Size(windW, windH),
                cornerRadius = CornerRadius(windW * 0.2f, windW * 0.2f)
            )

            // Cargo box
            drawRoundRect(
                color = InkTruckCargo,
                topLeft = Offset(cargoX, cargoY),
                size = Size(cargoW, cargoH),
                cornerRadius = cargoCorner
            )
            // White 60% stripe along the cargo box center
            val stripeH = cargoH * 0.8f
            val stripeW = 0.18f * w
            drawRoundRect(
                color = Color.White.copy(alpha = 0.60f),
                topLeft = Offset((w - stripeW) / 2f, cargoY + (cargoH - stripeH) / 2f),
                size = Size(stripeW, stripeH),
                cornerRadius = CornerRadius(stripeW / 2f, stripeW / 2f)
            )
        }

        // 4. Headlights: two circles of 0.14 * w in Sun tint at front corners (inset 0.10 * w)
        val hlRadius = (0.14f * w) / 2f
        val hlInsetX = 0.10f * w + hlRadius
        val hlY = 0.08f * h
        drawCircle(
            color = SunTint,
            radius = hlRadius,
            center = Offset(hlInsetX, hlY)
        )
        drawCircle(
            color = SunTint,
            radius = hlRadius,
            center = Offset(w - hlInsetX, hlY)
        )

        // 5. Taillights: two circles of 0.10 * w in Danger at the back
        val tlRadius = (0.10f * w) / 2f
        val tlInsetX = 0.12f * w + tlRadius
        val tlY = h - 0.06f * h
        drawCircle(
            color = Danger,
            radius = tlRadius,
            center = Offset(tlInsetX, tlY)
        )
        drawCircle(
            color = Danger,
            radius = tlRadius,
            center = Offset(w - tlInsetX, tlY)
        )
    }
}
