package com.xpl0t.scany.ui

import android.os.Bundle
import com.xpl0t.scany.R
import com.xpl0t.scany.services.BillingService
import com.xpl0t.scany.services.backpress.BackPressHandler
import com.xpl0t.scany.services.backpress.BackPressHandlerService
import com.xpl0t.scany.ui.common.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: BaseActivity() {

    private val disposables: MutableList<Disposable> = mutableListOf()
    @Inject lateinit var backPressHandlerService: BackPressHandlerService
    @Inject lateinit var billingService: BillingService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        billingService.checkPurchases()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.forEach { it.dispose() }
    }

    override fun onBackPressed() {
        val handler = backPressHandlerService.getHandler()
        if (handler == null)
            super.onBackPressed()
        else
            handler.onBackPressed()
    }
}