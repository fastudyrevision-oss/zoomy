package com.zoomy.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoomy.app.BuildConfig
import com.zoomy.app.data.UserPreferences
import com.zoomy.app.data.carIdToIndex
import com.zoomy.app.game.DailyRules
import com.zoomy.app.game.RoadView
import com.zoomy.app.game.ZoomyEngine
import com.zoomy.app.ui.HapticsHelper
import com.zoomy.app.ui.components.ScreenBackground
import com.zoomy.app.ui.components.StatChip
import com.zoomy.app.ui.components.StatChipType
import com.zoomy.app.ui.components.ZoomyButton
import com.zoomy.app.ui.components.ZoomyCard
import com.zoomy.app.ui.components.ZoomyIconButton
import com.zoomy.app.ui.theme.InkPrimary
import com.zoomy.app.ui.theme.InkSecondary
import com.zoomy.app.ui.theme.LimeDeep
import com.zoomy.app.ui.theme.SunDeep
import com.zoomy.app.ui.theme.SunTint
import com.zoomy.app.ui.theme.TomatoDeep
import com.zoomy.app.ui.theme.ZoomyTypography
import java.time.LocalDate

@Composable
fun HomeScreen(
    userPrefs: UserPreferences,
    dailyCountdown: String,
    hapticsHelper: HapticsHelper,
    onNavigatePlay: () -> Unit,
    onNavigateDaily: () -> Unit,
    onNavigateGarage: () -> Unit,
    onNavigateShop: () -> Unit,
    onNavigatePlus: () -> Unit,
    onDebugAddCoins: () -> Unit
) {
    val attractEngine = remember(userPrefs.equippedCar) {
        ZoomyEngine(isAttractMode = true, playerColorIndex = carIdToIndex(userPrefs.equippedCar))
    }

    val todayDateStr = LocalDate.now().toString()
    val isDailyClaimed = userPrefs.dailyClaimedDate == todayDateStr
    val dailyModifier = remember { DailyRules.getModifier() }

    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatChip(
                    type = StatChipType.Coins,
                    value = userPrefs.coins,
                    onClick = onNavigateShop,
                    onLongClick = if (BuildConfig.DEBUG) onDebugAddCoins else null
                )
                StatChip(
                    type = StatChipType.Best,
                    value = userPrefs.bestScore
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ZOOMY",
                style = ZoomyTypography.Caption.copy(color = InkSecondary)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Hero: attract mode road
            RoadView(engine = attractEngine)

            Spacer(modifier = Modifier.height(20.dp))

            // Controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ZoomyIconButton(
                    icon = Icons.Rounded.DirectionsCar,
                    onClick = onNavigateGarage,
                    size = 64.dp,
                    iconSize = 30.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                ZoomyButton(
                    text = "Play",
                    onClick = {
                        hapticsHelper.perform(HapticFeedbackType.LongPress)
                        onNavigatePlay()
                    },
                    modifier = Modifier.width(200.dp),
                    height = 64.dp,
                    leadingIcon = Icons.Rounded.PlayArrow,
                    isPlayIdleBreathing = true
                )

                Spacer(modifier = Modifier.width(16.dp))

                ZoomyIconButton(
                    icon = Icons.Rounded.Storefront,
                    onClick = onNavigateShop,
                    size = 64.dp,
                    iconSize = 30.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Today's Road Card (tappable -> starts daily run)
            ZoomyCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateDaily
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TODAY'S ROAD",
                            style = ZoomyTypography.Caption.copy(color = TomatoDeep)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dailyModifier.dayName,
                            style = ZoomyTypography.Title.copy(color = InkPrimary)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (isDailyClaimed) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = LimeDeep,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Claimed",
                                    style = ZoomyTypography.Label.copy(color = LimeDeep)
                                )
                            }
                        } else {
                            Text(
                                text = dailyCountdown,
                                style = ZoomyTypography.Label.copy(color = InkSecondary)
                            )
                        }
                    }

                    // Sun tint pill with "+50 coins"
                    Box(
                        modifier = Modifier
                            .alpha(if (isDailyClaimed) 0.40f else 1.0f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SunTint)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.MonetizationOn,
                                contentDescription = null,
                                tint = SunDeep,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+50 coins",
                                style = ZoomyTypography.Label.copy(color = SunDeep)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Zoomy Plus Card
            ZoomyCard(
                modifier = Modifier.fillMaxWidth(),
                fillColor = SunTint,
                borderColor = null,
                onClick = onNavigatePlus
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ZOOMY PLUS",
                            style = ZoomyTypography.Caption.copy(color = SunDeep)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "No ads, double coins",
                            style = ZoomyTypography.Title.copy(color = InkPrimary)
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.WorkspacePremium,
                        contentDescription = null,
                        tint = SunDeep,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
