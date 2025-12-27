package com.example.mana.billing

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BillingUiState(
    val isProUser: Boolean = false,
    val proSubscriptionDetails: ProductDetails? = null,
    val error: String? = null
)

class BillingManager(application: Application) : AndroidViewModel(application), PurchasesUpdatedListener {

    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState = _uiState.asStateFlow()

    private val billingClient = BillingClient.newBuilder(application)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        connectToGooglePlay()
    }

    private fun connectToGooglePlay() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProSubscription()
                    checkProStatus()
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to Google Play
            }
        })
    }

    private fun queryProSubscription() {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("mana_pro_subscription") // This ID must match the one in Google Play Console
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()))
            .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { _, productDetailsList ->
            if (productDetailsList.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(proSubscriptionDetails = productDetailsList[0])
            }
        }
    }

    private fun checkProStatus() {
        val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
        billingClient.queryPurchasesAsync(params) { _, purchases ->
             _uiState.value = _uiState.value.copy(isProUser = purchases.any { it.products.contains("mana_pro_subscription") && it.isAcknowledged })
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val productDetails = _uiState.value.proSubscriptionDetails ?: return
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(productDetails.subscriptionOfferDetails?.get(0)?.offerToken ?: "")
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) { 
                        // After acknowledging, update the pro status
                        checkProStatus()
                    }
                }
            }
        } else {
             _uiState.value = _uiState.value.copy(error = "خرید ناموفق بود.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
