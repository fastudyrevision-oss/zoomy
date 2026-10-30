package com.zoomy.app.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.zoomy.app.R

// Variable fonts in res/font: baloo2.ttf (headings, numbers), quicksand.ttf (text). Weights via FontVariation (API 26+).
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun vf(res: Int, w: FontWeight) =
    Font(res, w, variationSettings = FontVariation.Settings(FontVariation.weight(w.weight)))

val Baloo = FontFamily(
    vf(R.font.baloo2, FontWeight.SemiBold), vf(R.font.baloo2, FontWeight.Bold), vf(R.font.baloo2, FontWeight.ExtraBold)
)
val Quicksand = FontFamily(
    vf(R.font.quicksand, FontWeight.SemiBold), vf(R.font.quicksand, FontWeight.Bold)
)

// Typography spec:
// - Display: Baloo ExtraBold 56sp / 56sp
// - Score: Baloo ExtraBold 40sp / 44sp
// - H1: Baloo Bold 30sp / 34sp
// - H2: Baloo Bold 20sp / 26sp
// - Number: Baloo ExtraBold 18sp / 22sp
// - Title: Quicksand Bold 17sp / 24sp
// - Body: Quicksand SemiBold 15sp / 22sp
// - Label: Quicksand Bold 13sp / 18sp
// - Caption: Quicksand Bold 11sp / 14sp, UPPERCASE, letterSpacing 1sp

object ZoomyTypography {
    val Display = androidx.compose.ui.text.TextStyle(
        fontFamily = Baloo,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 56.sp,
        lineHeight = 56.sp
    )
    val Score = androidx.compose.ui.text.TextStyle(
        fontFamily = Baloo,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 44.sp
    )
    val H1 = androidx.compose.ui.text.TextStyle(
        fontFamily = Baloo,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 34.sp
    )
    val H2 = androidx.compose.ui.text.TextStyle(
        fontFamily = Baloo,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    )
    val Number = androidx.compose.ui.text.TextStyle(
        fontFamily = Baloo,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        lineHeight = 22.sp
    )
    val Title = androidx.compose.ui.text.TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 24.sp
    )
    val Body = androidx.compose.ui.text.TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp
    )
    val Label = androidx.compose.ui.text.TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
    val Caption = androidx.compose.ui.text.TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp
    )
}
