package com.zoomy.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zoomy.app.ui.components.SecondaryButton
import com.zoomy.app.ui.components.ZoomyButton
import com.zoomy.app.ui.components.ZoomyCard
import com.zoomy.app.ui.components.ZoomyTextButton
import com.zoomy.app.ui.theme.InkPrimary
import com.zoomy.app.ui.theme.InkScrim
import com.zoomy.app.ui.theme.Lime
import com.zoomy.app.ui.theme.LimeDeep
import com.zoomy.app.ui.theme.OverlayCardShape
import com.zoomy.app.ui.theme.StandardEasing
import com.zoomy.app.ui.theme.ZoomyTypography
import com.zoomy.app.ui.theme.bounceSpring

// ALLOWED UI ADDITIONS #2:
// Pause card additions: after "Restart" (12dp gap) add two rows (each 48dp, full width):
// Title "Sound" with a Switch on the right (on = Lime);
// Title "Haptics" with a Switch.
// Then, only if required, a TextButton "Privacy settings".
// Then the existing "Home" TextButton.
// Toggles write soundOn/hapticsOn immediately.

@Composable
fun PauseOverlay(
    visible: Boolean,
    soundOn: Boolean,
    hapticsOn: Boolean,
    onSoundChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    showPrivacySettings: Boolean,
    onPrivacySettings: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(220, easing = StandardEasing))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(InkScrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(initialScale = 0.8f, animationSpec = bounceSpring()),
                exit = scaleOut(targetScale = 0.8f, animationSpec = tween(220, easing = StandardEasing))
            ) {
                ZoomyCard(
                    modifier = Modifier.width(300.dp),
                    shape = OverlayCardShape
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Paused",
                            style = ZoomyTypography.H1.copy(color = InkPrimary)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        ZoomyButton(
                            text = "Resume",
                            onClick = onResume,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SecondaryButton(
                            text = "Restart",
                            onClick = onRestart,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sound row (48dp, full width)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sound",
                                style = ZoomyTypography.Title.copy(color = InkPrimary)
                            )
                            Switch(
                                checked = soundOn,
                                onCheckedChange = onSoundChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Lime,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray
                                )
                            )
                        }

                        // Haptics row (48dp, full width)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Haptics",
                                style = ZoomyTypography.Title.copy(color = InkPrimary)
                            )
                            Switch(
                                checked = hapticsOn,
                                onCheckedChange = onHapticsChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Lime,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray
                                )
                            )
                        }

                        if (showPrivacySettings) {
                            Spacer(modifier = Modifier.height(4.dp))
                            ZoomyTextButton(
                                text = "Privacy settings",
                                onClick = onPrivacySettings
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        ZoomyTextButton(
                            text = "Home",
                            onClick = onHome
                        )
                    }
                }
            }
        }
    }
}
