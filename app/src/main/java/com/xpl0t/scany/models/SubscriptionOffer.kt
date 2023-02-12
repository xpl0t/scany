package com.xpl0t.scany.models

import com.android.billingclient.api.ProductDetails

data class SubscriptionOffer (
    val id: String,
    val variantNameResId: Int,
    val costOverviewResId: Int,
    val chipId: Int,
    val formattedPrice: String = "",
    val productDetails: ProductDetails? = null,
    val offerToken: String? = null
)
