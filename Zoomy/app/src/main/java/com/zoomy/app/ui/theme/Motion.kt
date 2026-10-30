package com.zoomy.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

// EASINGS (Motion.kt)
// - Standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
// - EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
// - Bounce spring = spring(dampingRatio 0.45, stiffness 500)
// - Press spring = spring(dampingRatio 0.55, stiffness 700)
// - Push spring = spring(dampingRatio 0.8, stiffness 300)

val StandardEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

fun <T> bounceSpring() = spring<T>(dampingRatio = 0.45f, stiffness = 500f)
fun <T> pressSpring() = spring<T>(dampingRatio = 0.55f, stiffness = 700f)
fun <T> pushSpring() = spring<T>(dampingRatio = 0.8f, stiffness = 300f)
