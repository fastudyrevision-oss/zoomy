package com.zoomy.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.ProductDetails
import com.zoomy.app.billing.Products
import com.zoomy.app.data.UserPreferences
import com.zoomy.app.ui.components.CompactButton
import com.zoomy.app.ui.components.ScreenBackground
import com.zoomy.app.ui.components.StatChip
import com.zoomy.app.ui.components.StatChipType
import com.zoomy.app.ui.components.ZoomyCard
import com.zoomy.app.ui.components.ZoomyIconButton
import com.zoomy.app.ui.theme.GrapeDeep
import com.zoomy.app.ui.theme.GrapeTint
import com.zoomy.app.ui.theme.InkPrimary
import com.zoomy.app.ui.theme.InkSecondary
import com.zoomy.app.ui.theme.Lime
import com.zoomy.app.ui.theme.SkyDeep
import com.zoomy.app.ui.theme.SkyTint
import com.zoomy.app.ui.theme.Sun
import com.zoomy.app.ui.theme.SunDeep
import com.zoomy.app.ui.theme.SunTint
import com.zoomy.app.ui.theme.ZoomyTypography
import kotlinx.coroutines.launch

@Composable
fun ShopScreen(
    userPrefs: UserPreferences,
    productDetailsMap: Map<String, ProductDetails>,
    onBack: () -> Unit,
    onNavigatePlus: () -> Unit,
    onBuyCoinPack: (String) -> Unit,
    onBuyBooster: (type: String, cost: Int, count: Int, onShake: () -> Unit) -> Unit,
    onBuyRemoveAds: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val p500Price = productDetailsMap[Products.COINS_500]?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$1.99"
    val p1500Price = productDetailsMap[Products.COINS_1500]?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$4.99"
    val p4000Price = productDetailsMap[Products.COINS_4000]?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$9.99"
    val removeAdsPrice = productDetailsMap[Products.REMOVE_ADS]?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$3.99"

    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ZoomyIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        onClick = onBack
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Shop",
                        style = ZoomyTypography.H1.copy(color = InkPrimary)
                    )
                }

                StatChip(
                    type = StatChipType.Coins,
                    value = userPrefs.coins
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Plus banner Card
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ZOOMY PLUS",
                            style = ZoomyTypography.Caption.copy(color = SunDeep)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "No ads, double coins",
                            style = ZoomyTypography.H2.copy(color = InkPrimary, fontSize = 18.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CompactButton(
                            text = "Try free",
                            onClick = onNavigatePlus
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.60f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WorkspacePremium,
                            contentDescription = null,
                            tint = SunDeep,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Coins section
            Text(
                text = "Coins",
                style = ZoomyTypography.H2.copy(color = InkPrimary)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CoinPackCard(
                    modifier = Modifier.weight(1f),
                    coins = "500",
                    price = p500Price,
                    isPopular = false,
                    onBuy = { onBuyCoinPack(Products.COINS_500) }
                )
                CoinPackCard(
                    modifier = Modifier.weight(1f),
                    coins = "1,500",
                    price = p1500Price,
                    isPopular = true,
                    onBuy = { onBuyCoinPack(Products.COINS_1500) }
                )
                CoinPackCard(
                    modifier = Modifier.weight(1f),
                    coins = "4,000",
                    price = p4000Price,
                    isPopular = false,
                    onBuy = { onBuyCoinPack(Products.COINS_4000) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Boosters section
            Text(
                text = "Boosters",
                style = ZoomyTypography.H2.copy(color = InkPrimary)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Shield x3
            BoosterShopCard(
                icon = Icons.Rounded.Shield,
                title = "Shield x3",
                subtitle = "Survive one crash",
                price = "100",
                onBuy = { onShake ->
                    onBuyBooster("shield", 100, 3, onShake)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Magnet x3
            BoosterShopCard(
                icon = Icons.Rounded.Adjust,
                title = "Magnet x3",
                subtitle = "Pulls in nearby coins",
                price = "150",
                onBuy = { onShake ->
                    onBuyBooster("magnet", 150, 3, onShake)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Remove ads Card
            ZoomyCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GrapeTint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Block,
                                contentDescription = null,
                                tint = GrapeDeep,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Remove ads",
                                style = ZoomyTypography.Title.copy(color = InkPrimary)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "One-time purchase",
                                style = ZoomyTypography.Label.copy(color = InkSecondary)
                            )
                        }
                    }

                    CompactButton(
                        text = if (userPrefs.removeAds) "Owned" else removeAdsPrice,
                        onClick = {
                            if (!userPrefs.removeAds) {
                                onBuyRemoveAds()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CoinPackCard(
    modifier: Modifier = Modifier,
    coins: String,
    price: String,
    isPopular: Boolean,
    onBuy: () -> Unit
) {
    Box(
        modifier = modifier.height(160.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        ZoomyCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = if (isPopular) 10.dp else 0.dp),
            shape = RoundedCornerShape(28.dp),
            borderColor = if (isPopular) Sun else null
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.MonetizationOn,
                        contentDescription = null,
                        tint = Sun,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = coins,
                        style = ZoomyTypography.Number.copy(color = InkPrimary)
                    )
                    Text(
                        text = "coins",
                        style = ZoomyTypography.Label.copy(color = InkSecondary)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(CircleShape)
                        .background(Lime)
                        .clickable(onClick = onBuy),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = price,
                        style = ZoomyTypography.Label.copy(color = Color.White)
                    )
                }
            }
        }

        if (isPopular) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Sun)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "POPULAR",
                    style = ZoomyTypography.Caption.copy(color = InkPrimary, fontSize = 9.sp)
                )
            }
        }
    }
}

@Composable
private fun BoosterShopCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    price: String,
    onBuy: (onShake: () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
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

    ZoomyCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(SkyTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SkyDeep,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = ZoomyTypography.Title.copy(color = InkPrimary)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = ZoomyTypography.Label.copy(color = InkSecondary)
                    )
                }
            }

            Box(modifier = Modifier.offset(x = shakeX.value.dp)) {
                CompactButton(
                    text = price,
                    onClick = { onBuy { doShake() } },
                    leadingIcon = Icons.Rounded.Star
                )
            }
        }
    }
}
