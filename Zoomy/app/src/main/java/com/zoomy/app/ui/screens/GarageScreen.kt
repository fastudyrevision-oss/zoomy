package com.zoomy.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoomy.app.data.CarItem
import com.zoomy.app.data.UserPreferences
import com.zoomy.app.data.carIdToIndex
import com.zoomy.app.data.carIndexToId
import com.zoomy.app.data.getCarPrice
import com.zoomy.app.ui.components.CarSprite
import com.zoomy.app.ui.components.ScreenBackground
import com.zoomy.app.ui.components.ZoomyCard
import com.zoomy.app.ui.components.ZoomyIconButton
import com.zoomy.app.ui.theme.InkPrimary
import com.zoomy.app.ui.theme.InkSecondary
import com.zoomy.app.ui.theme.LimeDeep
import com.zoomy.app.ui.theme.LimeTint
import com.zoomy.app.ui.theme.LockedDeep
import com.zoomy.app.ui.theme.LockedGrey
import com.zoomy.app.ui.theme.SunDeep
import com.zoomy.app.ui.theme.Tomato
import com.zoomy.app.ui.theme.TomatoDeep
import com.zoomy.app.ui.theme.TomatoTint
import com.zoomy.app.ui.theme.TrackBgColor
import com.zoomy.app.ui.theme.ZoomyTypography
import com.zoomy.app.ui.theme.getCarColor
import com.zoomy.app.ui.theme.pressSpring
import kotlinx.coroutines.launch
import java.text.NumberFormat

val allGarageCars = listOf(
    CarItem(1, "Pip", 0),
    CarItem(2, "Bumble", 500),
    CarItem(3, "Minty", 900),
    CarItem(4, "Blueberry", 1500),
    CarItem(5, "Grapey", 2500),
    CarItem(6, "Orangy", 4000),
    CarItem(7, "Pinky", 6000),
    CarItem(8, "Goldie", 0, isPlusOnly = true)
)

@Composable
fun GarageScreen(
    userPrefs: UserPreferences,
    onBack: () -> Unit,
    onEquipCar: (String) -> Unit,
    onBuyCar: (String, () -> Unit) -> Unit,
    onNavigatePlus: () -> Unit
) {
    val unlockedCount = userPrefs.ownedCars.size + (if (userPrefs.plusActive) 1 else 0)
    val progressFraction = (unlockedCount / 8f).coerceIn(0f, 1f)

    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top row
            Row(verticalAlignment = Alignment.CenterVertically) {
                ZoomyIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    onClick = onBack
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Garage",
                        style = ZoomyTypography.H1.copy(color = InkPrimary)
                    )
                    Text(
                        text = "$unlockedCount of 8 unlocked",
                        style = ZoomyTypography.Body.copy(color = InkSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Thin progress track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val r = size.height / 2f
                    drawRoundRect(
                        color = TrackBgColor,
                        topLeft = Offset.Zero,
                        size = size,
                        cornerRadius = CornerRadius(r, r)
                    )
                    drawRoundRect(
                        color = Tomato,
                        topLeft = Offset.Zero,
                        size = Size(size.width * progressFraction, size.height),
                        cornerRadius = CornerRadius(r, r)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2-column grid of 8 Cars
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(allGarageCars) { car ->
                    val carId = carIndexToId(car.id)
                    val isOwned = userPrefs.ownedCars.contains(carId) || (carId == "goldie" && userPrefs.plusActive)
                    val isEquipped = userPrefs.equippedCar == carId

                    CarGarageCard(
                        car = car,
                        isEquipped = isEquipped,
                        isOwned = isOwned,
                        isPlusActive = userPrefs.plusActive,
                        onPillClick = { onShake ->
                            if (isEquipped) {
                                // Already equipped
                            } else if (isOwned) {
                                onEquipCar(carId)
                            } else if (carId == "goldie") {
                                onNavigatePlus()
                            } else {
                                onBuyCar(carId, onShake)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CarGarageCard(
    car: CarItem,
    isEquipped: Boolean,
    isOwned: Boolean,
    isPlusActive: Boolean,
    onPillClick: (onShake: () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pillScale = remember { Animatable(1f) }
    val shakeX = remember { Animatable(0f) }

    fun doShake() {
        coroutineScope.launch {
            for (i in 0..3) {
                shakeX.animateTo(6f, tween(37))
                shakeX.animateTo(-6f, tween(37))
            }
            shakeX.animateTo(0f, tween(37))
        }
    }

    val bobTransition = rememberInfiniteTransition(label = "CarBob")
    val bobY by bobTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BobY"
    )

    val cardAlpha = if (car.isPlusOnly && !isPlusActive) 0.70f else 1.0f

    ZoomyCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .alpha(cardAlpha),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .height(92.dp)
                    .offset(y = bobY.dp),
                contentAlignment = Alignment.Center
            ) {
                CarSprite(
                    color = getCarColor(car.id),
                    height = 92.dp,
                    crown = car.id == 8
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = car.name,
                style = ZoomyTypography.Title.copy(color = InkPrimary)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .offset(x = shakeX.value.dp)
                    .scale(pillScale.value)
                    .clip(CircleShape)
                    .background(
                        when {
                            isEquipped -> LimeTint
                            isOwned -> TomatoTint
                            else -> LockedGrey
                        }
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                coroutineScope.launch { pillScale.animateTo(0.92f, tween(60)) }
                                tryAwaitRelease()
                                coroutineScope.launch { pillScale.animateTo(1f, pressSpring()) }
                                onPillClick { doShake() }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                when {
                    isEquipped -> {
                        Text(
                            text = "EQUIPPED",
                            style = ZoomyTypography.Label.copy(color = LimeDeep)
                        )
                    }
                    isOwned -> {
                        Text(
                            text = "EQUIP",
                            style = ZoomyTypography.Label.copy(color = TomatoDeep)
                        )
                    }
                    car.isPlusOnly -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.WorkspacePremium,
                                contentDescription = null,
                                tint = SunDeep,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PLUS",
                                style = ZoomyTypography.Label.copy(color = LockedDeep)
                            )
                        }
                    }
                    else -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.MonetizationOn,
                                contentDescription = null,
                                tint = SunDeep,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = NumberFormat.getNumberInstance().format(car.price),
                                style = ZoomyTypography.Label.copy(color = LockedDeep)
                            )
                        }
                    }
                }
            }
        }
    }
}
