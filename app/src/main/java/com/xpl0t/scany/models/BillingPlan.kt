package com.xpl0t.scany.models

import com.android.billingclient.api.ProductDetails

data class BillingPlan (
    val id: String,
    val variantNameResId: Int,
    val costOverviewResId: Int,
    val formattedPrice: String = "",
    val productDetails: ProductDetails? = null,
    val offerToken: String? = null
)
