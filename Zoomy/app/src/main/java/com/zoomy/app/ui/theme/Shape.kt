package com.zoomy.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Shapes:
// - Buttons: full pill (Play button height 64dp; other buttons 56dp; compact 44dp)
// - Cards: RoundedCornerShape(32dp)
// - Chips, badges, progress tracks: full pill
// - Icon buttons: 48dp circle
// - Bottom sheet / overlay cards: RoundedCornerShape(36dp)
// - Road: custom (28dp corners)

val ButtonPillShape = CircleShape
val CardShape = RoundedCornerShape(32.dp)
val OverlayCardShape = RoundedCornerShape(36.dp)
val CardMediumShape = RoundedCornerShape(28.dp)
val StatPillShape = RoundedCornerShape(20.dp)
val RoadShape = RoundedCornerShape(28.dp)
val IconButtonShape = CircleShape
