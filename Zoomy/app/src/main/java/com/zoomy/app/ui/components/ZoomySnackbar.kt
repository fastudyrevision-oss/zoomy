package com.zoomy.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoomy.app.ui.theme.EmphasizedDecelerateEasing
import com.zoomy.app.ui.theme.InkPrimary
import com.zoomy.app.ui.theme.Quicksand
import kotlinx.coroutines.delay

// ALLOWED UI ADDITIONS
// 1. Snackbar: pill, Ink fill, text Quicksand Bold 14sp white, horizontal padding 20dp, vertical padding 12dp,
// max width screen - 40dp, centered, bottom margin 24dp + inset, appears with translationY +24dp -> 0 and alpha 0 -> 1
// over 250ms EmphasizedDecelerate, holds 2200ms, fades out over 200ms. One at a time; a new one replaces the current.

@Composable
fun ZoomySnackbarHost(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val maxWidth = (configuration.screenWidthDp - 40).dp

    var currentMessage by remember { mutableStateOf<String?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            currentMessage = message
            visible = true
            delay(2200)
            visible = false
            delay(200)
            currentMessage = null
            onDismiss()
        } else {
            visible = false
            currentMessage = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { 24 },
                animationSpec = tween(250, easing = EmphasizedDecelerateEasing)
            ) + fadeIn(animationSpec = tween(250, easing = EmphasizedDecelerateEasing)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            currentMessage?.let { text ->
                Box(
                    modifier = Modifier
                        .widthIn(max = maxWidth)
                        .clip(CircleShape)
                        .background(InkPrimary)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = Quicksand,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}
