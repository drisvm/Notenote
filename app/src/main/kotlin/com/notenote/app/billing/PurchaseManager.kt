package com.notenote.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.notenote.app.data.preferences.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

const val PRODUCT_REMOVE_ADS = "remove_ads"

sealed interface PurchaseState {
    data object Loading : PurchaseState
    data class Available(val price: String) : PurchaseState
    data object Owned : PurchaseState
    data object Unavailable : PurchaseState
    data class Error(val message: String) : PurchaseState
}

class PurchaseManager(
    context: Context,
    private val settingsRepository: SettingsRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow<PurchaseState>(PurchaseState.Loading)
    val state: StateFlow<PurchaseState> = _state.asStateFlow()

    private var cachedProductDetails: ProductDetails? = null

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(
            PurchasesUpdatedListener { result, purchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    processPurchases(purchases.orEmpty())
                }
            },
        )
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .build()

    suspend fun refresh() {
        try {
            val connectResult = connect()
            if (connectResult.responseCode != BillingClient.BillingResponseCode.OK) {
                if (_state.value is PurchaseState.Loading) _state.value = PurchaseState.Unavailable
                return
            }
            queryPurchases()
            queryProduct()
            if (_state.value is PurchaseState.Loading) _state.value = PurchaseState.Unavailable
        } catch (t: Throwable) {
            _state.value = PurchaseState.Error(t.message ?: t.javaClass.simpleName)
        }
    }

    suspend fun launchPurchase(activity: Activity): Boolean = withContext(Dispatchers.Main.immediate) {
        val details = cachedProductDetails ?: return@withContext false
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build(),
                ),
            )
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
            .responseCode == BillingClient.BillingResponseCode.OK
    }

    private suspend fun connect(): BillingResult {
        if (billingClient.isReady) {
            return BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build()
        }
        return suspendCancellableCoroutine { cont ->
            var resumed = false
            fun finish(result: BillingResult) {
                if (!resumed) {
                    resumed = true
                    cont.resume(result)
                }
            }
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    finish(billingResult)
                }

                override fun onBillingServiceDisconnected() {
                    finish(
                        BillingResult.newBuilder()
                            .setResponseCode(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
                            .build(),
                    )
                }
            })
        }
    }

    private suspend fun queryProduct() = suspendCancellableCoroutine { cont ->
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_REMOVE_ADS)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()
        billingClient.queryProductDetailsAsync(params) { result, productDetailsList ->
            val details = productDetailsList.firstOrNull()
            cachedProductDetails = details
            val price = details?.oneTimePurchaseOfferDetails?.formattedPrice
            if (result.responseCode == BillingClient.BillingResponseCode.OK &&
                details != null && price != null && _state.value !is PurchaseState.Owned
            ) {
                _state.value = PurchaseState.Available(price)
            }
            cont.resume(details)
        }
    }

    private suspend fun queryPurchases() = suspendCancellableCoroutine { cont ->
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases)
            }
            cont.resume(purchases)
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        val owned = purchases.any {
            it.products.contains(PRODUCT_REMOVE_ADS) &&
                it.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        if (owned) {
            purchases
                .filter { it.products.contains(PRODUCT_REMOVE_ADS) && !it.isAcknowledged }
                .forEach {
                    billingClient.acknowledgePurchase(
                        AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(it.purchaseToken)
                            .build(),
                    ) { result ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            grantAdsRemoved()
                        }
                    }
                    grantAdsRemoved()
                }
        } else if (_state.value is PurchaseState.Owned) {
            _state.value = PurchaseState.Unavailable
        }
    }

    private fun grantAdsRemoved() {
        if (_state.value !is PurchaseState.Owned) _state.value = PurchaseState.Owned
        scope.launch { settingsRepository.setAdsRemoved(true) }
    }
}
