package com.zoomy.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Tomato,
    onPrimary = SurfaceBase,
    secondary = Sun,
    onSecondary = InkPrimary,
    background = CanvasTop,
    surface = SurfaceBase,
    onSurface = InkPrimary,
)

@Composable
fun ZoomyTheme(
    content: @Composable () -> Unit
) {
    // STRICT RULE: Light-only. No dark mode.
    // Edge-to-edge, transparent system bars, dark status-bar icons.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
