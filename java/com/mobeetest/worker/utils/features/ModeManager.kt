package com.mobeetest.worker.utils.features

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.mobeetest.worker.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsResult

object ModeManager : PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient
    private lateinit var appContext: Context

    var onPurchaseCompleted: (() -> Unit)? = null

    private const val PREFS_NAME = "secure_feature_store"
    private const val KEY_PAID = "is_paid"
    private const val KEY_CAMPAIGN = "is_campaign"
    private const val PRODUCT_ID_UNLOCK_PAID = "unlock_paid"
    private const val PRODUCT_ID_UNLOCK_CAMPAIGN = "unlock_campaign"

    private var _isPaidUser = false
    private var _isWebCampaign = false

    val isPaidUser: Boolean get() = _isPaidUser
    val isWebCampaign: Boolean get() = _isWebCampaign

    val paidUserFlow = MutableStateFlow(false)
    val campaignFlow = MutableStateFlow(false)

    /**
     * String flavor used in UI (free / paid / web_campaign).
     */
    val currentFlavor: String
        get() = when {
            _isWebCampaign -> "web_campaign"
            _isPaidUser -> "paid"
            else -> "free"
        }

    fun initialize(context: Context) {
        appContext = context.applicationContext
        restoreFromPrefs()
        paidUserFlow.value = _isPaidUser
        campaignFlow.value = _isWebCampaign
        setupBilling()
    }

    override fun onPurchasesUpdated(
        result: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases?.forEach { purchase ->
                when {
                    purchase.products.contains(PRODUCT_ID_UNLOCK_PAID) -> {
                        _isPaidUser = true
                        acknowledgeIfNeeded(purchase)
                    }

                    purchase.products.contains(PRODUCT_ID_UNLOCK_CAMPAIGN) -> {
                        _isPaidUser = true
                        _isWebCampaign = true
                        acknowledgeIfNeeded(purchase)
                    }
                }
            }

            persistToPrefs()
            onPurchaseCompleted?.invoke()
            restartApp()
        }
    }

    private fun restoreFromPrefs() {
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isPaidUser = prefs.getBoolean(KEY_PAID, false)
        _isWebCampaign = prefs.getBoolean(KEY_CAMPAIGN, false)

        // In debug builds we can force everything unlocked if you want.
        if (BuildConfig.DEBUG) {
            _isPaidUser = true
            _isWebCampaign = true
        }
    }

    private fun persistToPrefs() {
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_PAID, _isPaidUser)
            putBoolean(KEY_CAMPAIGN, _isWebCampaign)
        }
        paidUserFlow.value = _isPaidUser
        campaignFlow.value = _isWebCampaign
    }

    private fun setupBilling() {
        @Suppress("DEPRECATION")
        billingClient = BillingClient.newBuilder(appContext)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()  // mandatory για pending one-time
                    // .enablePrepaidPlans()   // αν υποστηρίζεις prepaid subscriptions
                    .build()
            )
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Optional retry logic.
            }
        })
    }

    private fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val unlocked = purchases.any { purchase ->
                    purchase.products.contains(PRODUCT_ID_UNLOCK_PAID) &&
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                if (unlocked) {
                    _isPaidUser = true
                    _isWebCampaign = true // Optionally separate this if you want
                    persistToPrefs()
                }
            }
        }
    }

    fun launchCampaignPurchaseFlow(activity: Activity) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID_UNLOCK_CAMPAIGN)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(params) {
                billingResult: BillingResult,
                productDetailsResult: QueryProductDetailsResult ->

            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync

            val detailsList: List<ProductDetails> = productDetailsResult.productDetailsList
            if (detailsList.isEmpty()) return@queryProductDetailsAsync

            val productDetails = detailsList[0]

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                )
                .build()

            billingClient.launchBillingFlow(activity, flowParams)
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val queryParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID_UNLOCK_PAID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(queryParams) {
                billingResult: BillingResult,
                productDetailsResult: QueryProductDetailsResult ->

            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync

            val detailsList: List<ProductDetails> = productDetailsResult.productDetailsList
            if (detailsList.isEmpty()) return@queryProductDetailsAsync

            val productDetails = detailsList[0]

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                )
                .build()

            billingClient.launchBillingFlow(activity, flowParams)
        }
    }

    private fun acknowledgeIfNeeded(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(ackParams) {
                // Optional logging
            }
        }
    }

    private fun restartApp() {
        val intent =
            appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        appContext.startActivity(intent)
    }
}