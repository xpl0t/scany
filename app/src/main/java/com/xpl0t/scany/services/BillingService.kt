package com.xpl0t.scany.services

import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.xpl0t.scany.R
import com.xpl0t.scany.models.BillingPlan
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingService @Inject() constructor(
    @ApplicationContext private val context: Context
) {

    private val isSubscribedSubject = BehaviorSubject.createDefault(
        getIsSubscribedFromPref()
    )

    val isSubscribed: Boolean
        get() = isSubscribedSubject.value!!

    val isSubscribedObs: Observable<Boolean>
        get() = isSubscribedSubject.distinctUntilChanged()

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            Log.i(TAG, "Purchases updated (billingResult: $billingResult)")
            if (billingResult.responseCode != BillingResponseCode.OK) {
                return@PurchasesUpdatedListener
            }

            handlePurchases(purchases)
        }

    private val purchasesResponseListener =
        PurchasesResponseListener { billingResult, purchases ->
            Log.i(TAG, "Purchases response (billingResult: $billingResult)")
            if (billingResult.responseCode != BillingResponseCode.OK) {
                return@PurchasesResponseListener
            }

            handlePurchases(purchases)
        }

    private var billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    fun init() {
        val stateListener = object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.i(TAG, "Billing setup finished (responseCode: $billingResult)")

                if (billingResult.responseCode == BillingResponseCode.OK) {
                    Log.i(TAG, "Billing client ready!")
                    checkPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.i(TAG, "Billing service disconnected, initializing reconnection")
                billingClient.startConnection(this)
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        }

        billingClient.startConnection(stateListener)

        isSubscribedObs.subscribe {
            Log.i(TAG, "Set is subscribed to $it in shared preferences")
            setIsSubscribedInPref(it)
        }
    }

    fun checkPurchases() {
        Log.i(TAG, "Check purchases")

        if (!billingClient.isReady) {
            Log.w(TAG, "Billing client not ready, skipping checkPurchases")
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)

        PurchasesResponseListener { billingResult, purchases ->
            Log.i(TAG, "Purchases updated (billingResult: $billingResult)")
            // To be implemented in a later section.
            Purchase.PurchaseState.PURCHASED
        }

        billingClient.queryPurchasesAsync(params.build(), purchasesResponseListener)
    }

    fun getSubscriptionOffers(subscriptionId: String): Observable<List<BillingPlan>> {
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(SUB_ID)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                )
                .build()

        return Observable.create {
            billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    it.onNext(mapOffers(subscriptionId, productDetailsList))
                    it.onComplete()
                    return@queryProductDetailsAsync
                }

                it.onError(Throwable("Could not fetch offers. Response code: $billingResult"))
            }
        }
    }

    private fun mapOffers(subscriptionId: String, productDetailsList: List<ProductDetails>): List<BillingPlan> {
        val basicSub = productDetailsList.find { it.productId == subscriptionId }
        val priceMap = basicSub?.subscriptionOfferDetails!!
            .associateBy(
                { it.basePlanId },
                { it.pricingPhases.pricingPhaseList.first().formattedPrice }
            )
        val tokenMap = basicSub.subscriptionOfferDetails!!
            .associateBy(
                { it.basePlanId },
                { it.offerToken }
            )

        val offers = listOf(
            BillingPlan("basic", R.string.view_sub_monthly, R.string.view_sub_cost_overview_monthly),
            BillingPlan("basic-yearly", R.string.view_sub_yearly, R.string.view_sub_cost_overview_yearly)
        )

        return offers.map {
            it.copy(
                formattedPrice = priceMap[it.id]!!,
                productDetails = basicSub,
                offerToken = tokenMap[it.id]
            )
        }
    }

    fun getBillingClient(): BillingClient {
        return billingClient
    }

    private fun handlePurchases(purchases: List<Purchase>?) {
        val subPurchase = purchases?.find { it.products.contains(SUB_ID) }
        if (subPurchase != null) {
            if (!subPurchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(subPurchase.purchaseToken)
                    .build()

                Log.i(TAG, "Acknowledging purchase")
                billingClient.acknowledgePurchase(params) {
                    Log.i(TAG, "Acknowledge purchase responded with result $it")
                    if (it.responseCode == BillingResponseCode.OK)
                        isSubscribedSubject.onNext(true)
                }
                return
            }

            isSubscribedSubject.onNext(true)
            return
        }

        isSubscribedSubject.onNext(false)
    }

    private fun getIsSubscribedFromPref(): Boolean {
        val pref = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        return pref.getBoolean(IS_SUBSCRIBED, false)
    }

    private fun setIsSubscribedInPref(isSubscribed: Boolean) {
        val pref = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        val prefEditor = pref.edit()
        prefEditor.putBoolean(IS_SUBSCRIBED, isSubscribed)
        prefEditor.apply()
    }

    companion object {
        private const val TAG = "BillingService"
        private const val SUB_ID = "basic"
        const val PREFERENCES_KEY = "BILLING_SERVICE"
        const val IS_SUBSCRIBED = "IS_SUBSCRIBED"
    }

}