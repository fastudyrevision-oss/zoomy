package com.zoomy.app.ui.theme

import androidx.compose.ui.graphics.Color

// Colors:
// - Canvas: vertical gradient #E6F7FF (top) to #FFF9E6 (bottom)
// - Surface: #FFFFFF at 80% | Surface border: #FFFFFF 1.5dp
// - Ink (text primary) #2B3A55 | secondary #6B7A99 | tertiary #B0BAD0
// - Tomato (primary) #FF5A4D | Tomato light #FF8577 | Tomato deep #D93A2E | Tomato tint #FFE3E0
// - Sky #4FB8FF | Sky deep #2E9BE0 | Sky tint #DDF3FF
// - Sun #FFC83D | Sun deep #E0A314 | Sun tint #FFF1C4
// - Lime #7ED957 | Lime deep #4FAE2B | Lime tint #E3F7D8
// - Grape #A98BFF | Grape deep #7A5FE0 | Grape tint #ECE5FF
// - Road #8E9AB5 | Road deep #7A87A5 | Danger #FF7A7A | Locked grey #E4E8F0 | Locked deep #CBD1E2 | Divider: Ink at 8%

val CanvasTop = Color(0xFFE6F7FF)
val CanvasBottom = Color(0xFFFFF9E6)

val SurfaceBase = Color(0xFFFFFFFF)
val SurfaceColor = Color(0xCCFFFFFF) // 80% alpha
val SurfaceBorderColor = Color(0xFFFFFFFF)

val InkPrimary = Color(0xFF2B3A55)
val InkSecondary = Color(0xFF6B7A99)
val InkTertiary = Color(0xFFB0BAD0)
val InkShadow = Color(0x1A2B3A55) // Ink at 10%
val InkShadowCar = Color(0x2E2B3A55) // Ink at 18%
val InkTruckCargo = Color(0x262B3A55) // Ink at 15%
val InkWheel = Color(0xD92B3A55) // Ink at 85%
val InkScrim = Color(0x5C2B3A55) // Ink at 36%
val DividerColor = Color(0x142B3A55) // Ink at 8%
val TrackBgColor = Color(0x142B3A55) // Ink at 8%

val Tomato = Color(0xFFFF5A4D)
val TomatoLight = Color(0xFFFF8577)
val TomatoDeep = Color(0xFFD93A2E)
val TomatoTint = Color(0xFFFFE3E0)

val Sky = Color(0xFF4FB8FF)
val SkyDeep = Color(0xFF2E9BE0)
val SkyTint = Color(0xFFDDF3FF)

val Sun = Color(0xFFFFC83D)
val SunDeep = Color(0xFFE0A314)
val SunTint = Color(0xFFFFF1C4)

val Lime = Color(0xFF7ED957)
val LimeDeep = Color(0xFF4FAE2B)
val LimeTint = Color(0xFFE3F7D8)

val Grape = Color(0xFFA98BFF)
val GrapeDeep = Color(0xFF7A5FE0)
val GrapeTint = Color(0xFFECE5FF)

val RoadColor = Color(0xFF8E9AB5)
val RoadDeepColor = Color(0xFF7A87A5)
val Danger = Color(0xFFFF7A7A)
val LockedGrey = Color(0xFFE4E8F0)
val LockedDeep = Color(0xFFCBD1E2)

// Car colors (index 1-8, the garage):
// 1 "Pip" #FF5A4D; 2 "Bumble" #FFC83D; 3 "Minty" #5ED9B0; 4 "Blueberry" #4FB8FF;
// 5 "Grapey" #A98BFF; 6 "Orangy" #FF9A4D; 7 "Pinky" #FF86B5; 8 "Goldie" #FFD84D (crown on roof).
// Car deep color = base darkened 14%; light color = base mixed 55% with white.
fun getCarColor(index: Int): Color = when (index) {
    1 -> Color(0xFFFF5A4D) // Pip
    2 -> Color(0xFFFFC83D) // Bumble
    3 -> Color(0xFF5ED9B0) // Minty
    4 -> Color(0xFF4FB8FF) // Blueberry
    5 -> Color(0xFFA98BFF) // Grapey
    6 -> Color(0xFFFF9A4D) // Orangy
    7 -> Color(0xFFFF86B5) // Pinky
    8 -> Color(0xFFFFD84D) // Goldie
    else -> Color(0xFFFF5A4D)
}

fun getCarName(index: Int): String = when (index) {
    1 -> "Pip"
    2 -> "Bumble"
    3 -> "Minty"
    4 -> "Blueberry"
    5 -> "Grapey"
    6 -> "Orangy"
    7 -> "Pinky"
    8 -> "Goldie"
    else -> "Pip"
}

fun Color.darken(factor: Float = 0.14f): Color {
    val f = (1f - factor).coerceIn(0f, 1f)
    return Color(
        red = red * f,
        green = green * f,
        blue = blue * f,
        alpha = alpha
    )
}

fun Color.lighten(factor: Float = 0.55f): Color {
    return Color(
        red = red + (1f - red) * factor,
        green = green + (1f - green) * factor,
        blue = blue + (1f - blue) * factor,
        alpha = alpha
    )
}
