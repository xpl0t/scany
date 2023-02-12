package com.xpl0t.scany.services

import javax.inject.Inject

class AuthorizationService @Inject constructor(
    private val billingService: BillingService
) {

    fun canAddDocument(documentCount: Int): Boolean {
        return billingService.isSubscribed || documentCount < FREE_TIER_MAX_DOCUMENTS
    }

    fun canAddPage(pageCount: Int): Boolean {
        return billingService.isSubscribed || pageCount < FREE_TIER_MAX_PAGES
    }

    companion object {
        private const val TAG = "AuthorizationService"
        const val FREE_TIER_MAX_DOCUMENTS = 3
        const val FREE_TIER_MAX_PAGES = 2
    }

}