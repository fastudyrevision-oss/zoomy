package com.zoomy.app.billing

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.zoomy.app.data.PrefsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class BillingManager(
    private val context: Context,
    private val prefsRepository: PrefsRepository,
    private val scope: CoroutineScope,
    private val onRewardSound: () -> Unit,
    private val showSnackbar: (String) -> Unit
) : PurchasesUpdatedListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var reconnectCount = 0
    private var isConnected = false

    private val _productDetailsMap = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetailsMap: StateFlow<Map<String, ProductDetails>> = _productDetailsMap.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    fun startConnection() {
        if (isConnected) return
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isConnected = true
                    reconnectCount = 0
                    queryAllProducts()
                    restorePurchases(isUserInitiated = false)
                } else {
                    scheduleReconnect()
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnected = false
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (reconnectCount >= 5) return
        val delayMs = when (reconnectCount) {
            0 -> 1000L
            1 -> 2000L
            2 -> 4000L
            3 -> 8000L
            else -> 16000L
        }
        reconnectCount++
        mainHandler.postDelayed({ startConnection() }, delayMs)
    }

    fun queryAllProducts() {
        if (!isConnected) return

        val inAppProducts = listOf(
            Products.COINS_500,
            Products.COINS_1500,
            Products.COINS_4000,
            Products.REMOVE_ADS
        ).map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val subsProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(Products.ZOOMY_PLUS)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(inAppProducts + subsProducts)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val map = productDetailsList.associateBy { it.productId }
                _productDetailsMap.value = map
            }
        }
    }

    fun restorePurchases(isUserInitiated: Boolean) {
        if (!isConnected) {
            if (isUserInitiated) {
                showSnackbar("Store unavailable. Try again later")
            }
            return
        }

        scope.launch(Dispatchers.IO) {
            var foundAny = false

            // 1. INAPP purchases
            val inAppParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            billingClient.queryPurchasesAsync(inAppParams) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (purchase in purchases) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            if (purchase.products.contains(Products.REMOVE_ADS)) {
                                foundAny = true
                                handlePurchase(purchase)
                            }
                        }
                    }
                }
            }

            // 2. SUBS purchases
            val subsParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            billingClient.queryPurchasesAsync(subsParams) { billingResult, purchases ->
                var plusActive = false
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (purchase in purchases) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            if (purchase.products.contains(Products.ZOOMY_PLUS)) {
                                plusActive = true
                                foundAny = true
                                handlePurchase(purchase)
                            }
                        }
                    }
                }
                scope.launch {
                    prefsRepository.setPlusActive(plusActive)
                    if (plusActive) {
                        prefsRepository.checkAndApplyPlusDailyGrant(LocalDate.now().toString())
                    }
                }

                if (isUserInitiated) {
                    mainHandler.post {
                        if (foundAny) {
                            showSnackbar("Purchases restored")
                        } else {
                            showSnackbar("Nothing to restore")
                        }
                    }
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                // USER_CANCELED shows nothing
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                restorePurchases(isUserInitiated = false)
            }
            else -> {
                showSnackbar("Purchase failed. Try again")
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            showSnackbar("Purchase pending")
            return
        }

        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        scope.launch(Dispatchers.IO) {
            val token = purchase.purchaseToken

            // Consumable Coin packs
            for (productId in purchase.products) {
                when (productId) {
                    Products.COINS_500 -> {
                        val isNew = prefsRepository.recordProcessedToken(token)
                        consume(token)
                        if (isNew) {
                            prefsRepository.addCoins(500)
                            mainHandler.post { onRewardSound() }
                        }
                    }
                    Products.COINS_1500 -> {
                        val isNew = prefsRepository.recordProcessedToken(token)
                        consume(token)
                        if (isNew) {
                            prefsRepository.addCoins(1500)
                            mainHandler.post { onRewardSound() }
                        }
                    }
                    Products.COINS_4000 -> {
                        val isNew = prefsRepository.recordProcessedToken(token)
                        consume(token)
                        if (isNew) {
                            prefsRepository.addCoins(4000)
                            mainHandler.post { onRewardSound() }
                        }
                    }
                    Products.REMOVE_ADS -> {
                        acknowledge(token)
                        prefsRepository.setRemoveAds(true)
                    }
                    Products.ZOOMY_PLUS -> {
                        acknowledge(token)
                        prefsRepository.setPlusActive(true)
                        prefsRepository.checkAndApplyPlusDailyGrant(LocalDate.now().toString())
                    }
                }
            }
        }
    }

    private fun consume(token: String) {
        val params = ConsumeParams.newBuilder().setPurchaseToken(token).build()
        billingClient.consumeAsync(params) { _, _ -> }
    }

    private fun acknowledge(token: String) {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(token).build()
        billingClient.acknowledgePurchase(params) { _ -> }
    }

    fun launch(activity: Activity, productId: String, basePlanId: String? = null) {
        val productDetails = _productDetailsMap.value[productId]
        if (productDetails == null || !isConnected) {
            showSnackbar("Store unavailable. Try again later")
            return
        }

        val productDetailsParamsList = when (productDetails.productType) {
            BillingClient.ProductType.SUBS -> {
                val offers = productDetails.subscriptionOfferDetails.orEmpty()
                // Find matching base plan with trial offer if available
                val matchedOffer = offers.firstOrNull {
                    it.basePlanId == basePlanId && it.offerId == Products.OFFER_TRIAL3
                } ?: offers.firstOrNull {
                    it.basePlanId == basePlanId
                } ?: offers.firstOrNull()

                val offerToken = matchedOffer?.offerToken ?: ""
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            }
            else -> {
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            }
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }
}
