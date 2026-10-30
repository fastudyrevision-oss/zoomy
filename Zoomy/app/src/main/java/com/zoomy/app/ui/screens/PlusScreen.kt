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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.ProductDetails
import com.zoomy.app.billing.Products
import com.zoomy.app.ui.components.CarSprite
import com.zoomy.app.ui.components.ScreenBackground
import com.zoomy.app.ui.components.ZoomyButton
import com.zoomy.app.ui.components.ZoomyButtonStyle
import com.zoomy.app.ui.components.ZoomyCard
import com.zoomy.app.ui.components.ZoomyIconButton
import com.zoomy.app.ui.components.ZoomyTextButton
import com.zoomy.app.ui.theme.CardMediumShape
import com.zoomy.app.ui.theme.InkPrimary
import com.zoomy.app.ui.theme.InkSecondary
import com.zoomy.app.ui.theme.InkTertiary
import com.zoomy.app.ui.theme.Lime
import com.zoomy.app.ui.theme.OverlayCardShape
import com.zoomy.app.ui.theme.StandardEasing
import com.zoomy.app.ui.theme.Tomato
import com.zoomy.app.ui.theme.TomatoLight
import com.zoomy.app.ui.theme.TomatoTint
import com.zoomy.app.ui.theme.ZoomyTypography
import com.zoomy.app.ui.theme.bounceSpring
import com.zoomy.app.ui.theme.getCarColor
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun PlusScreen(
    productDetailsMap: Map<String, ProductDetails>,
    onClose: () -> Unit,
    onStartTrial: (isYearly: Boolean) -> Unit,
    onRestore: () -> Unit
) {
    var isYearlySelected by remember { mutableStateOf(true) }

    val plusProduct = productDetailsMap[Products.ZOOMY_PLUS]
    val yearlyOffer = plusProduct?.subscriptionOfferDetails?.firstOrNull { it.basePlanId == Products.BASE_PLAN_YEARLY }
    val monthlyOffer = plusProduct?.subscriptionOfferDetails?.firstOrNull { it.basePlanId == Products.BASE_PLAN_MONTHLY }

    val yearlyPrice = yearlyOffer?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice ?: "$19.99"
    val monthlyPrice = monthlyOffer?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice ?: "$2.99"

    val bobTransition = rememberInfiniteTransition(label = "GoldieBob")
    val bobY by bobTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = StandardEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GoldieBobY"
    )

    val sparkleTime by bobTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SparkleTime"
    )

    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            ZoomyIconButton(
                icon = Icons.Rounded.Close,
                onClick = onClose
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hero: Tomato gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(OverlayCardShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(TomatoLight, Tomato)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val sparkleOffsets = listOf(
                    Pair(24f, 30f),
                    Pair(300f, 40f),
                    Pair(40f, 150f),
                    Pair(310f, 160f),
                    Pair(160f, 20f),
                    Pair(220f, 190f)
                )
                Canvas(modifier = Modifier.matchParentSize()) {
                    sparkleOffsets.forEachIndexed { idx, pair ->
                        val phase = idx * 1.0f
                        val alphaFactor = 0.40f + 0.60f * (0.5f + 0.5f * sin((sparkleTime * 3.9f + phase)))
                        val cx = pair.first.dp.toPx()
                        val cy = pair.second.dp.toPx()
                        val s = 5.dp.toPx()

                        drawCircle(
                            color = Color.White.copy(alpha = (0.70f * alphaFactor).coerceIn(0f, 1f)),
                            radius = s,
                            center = Offset(cx, cy)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .height(110.dp)
                            .offset(y = bobY.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CarSprite(
                            color = getCarColor(8),
                            height = 110.dp,
                            crown = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Zoomy Plus",
                        style = ZoomyTypography.H1.copy(color = Color.White)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Zoom without limits.",
                        style = ZoomyTypography.Body.copy(color = Color.White.copy(alpha = 0.85f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5 benefit rows
            val benefits = listOf(
                "No ads, ever",
                "3 free boosters every day",
                "Double coins",
                "Exclusive cars",
                "Golden Goldie car"
            )

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                benefits.forEach { benefit ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Lime),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = benefit,
                            style = ZoomyTypography.Title.copy(color = InkPrimary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Plan Cards
            PlanOptionCard(
                title = "Yearly",
                subtitle = "Just $1.67 / month",
                price = yearlyPrice,
                isSelected = isYearlySelected,
                badgeText = "SAVE 44%",
                onClick = { isYearlySelected = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PlanOptionCard(
                title = "Monthly",
                subtitle = "Billed every month",
                price = monthlyPrice,
                isSelected = !isYearlySelected,
                badgeText = null,
                onClick = { isYearlySelected = false }
            )

            Spacer(modifier = Modifier.height(20.dp))

            ZoomyButton(
                text = "Start 3-day free trial",
                onClick = { onStartTrial(isYearlySelected) },
                style = ZoomyButtonStyle.Accent,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Then $yearlyPrice per year. Cancel anytime.",
                style = ZoomyTypography.Label.copy(color = InkTertiary),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(4.dp))

            ZoomyTextButton(
                text = "Restore purchase",
                onClick = onRestore,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PlanOptionCard(
    title: String,
    subtitle: String,
    price: String,
    isSelected: Boolean,
    badgeText: String?,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(if (isSelected) 1.02f else 1.0f) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopEnd
    ) {
        ZoomyCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .scale(scale.value),
            shape = CardMediumShape,
            fillColor = if (isSelected) TomatoTint.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.80f),
            borderColor = if (isSelected) Tomato else null,
            onClick = {
                coroutineScope.launch {
                    scale.animateTo(1.04f, tween(60))
                    scale.animateTo(1.02f, bounceSpring())
                }
                onClick()
            }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Tomato else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Canvas(modifier = Modifier.matchParentSize()) {
                                drawCircle(
                                    color = InkPrimary.copy(alpha = 0.25f),
                                    radius = size.width / 2f,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            style = ZoomyTypography.Title.copy(color = InkPrimary)
                        )
                        Text(
                            text = subtitle,
                            style = ZoomyTypography.Label.copy(color = InkSecondary)
                        )
                    }
                }

                Text(
                    text = price,
                    style = ZoomyTypography.Title.copy(color = InkPrimary)
                )
            }
        }

        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .offset(x = (-12).dp, y = (-6).dp)
                    .clip(CircleShape)
                    .background(Lime)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    style = ZoomyTypography.Caption.copy(color = Color.White, fontSize = 10.sp)
                )
            }
        }
    }
}
