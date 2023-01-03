package com.xpl0t.scany.ui

import android.os.Bundle
import com.xpl0t.scany.R
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        initViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.forEach { it.dispose() }
    }

    private fun initViews() {
    }

    override fun onBackPressed() {
        val handler = backPressHandlerService.getHandler()
        if (handler == null)
            super.onBackPressed()
        else
            handler.onBackPressed()
    }
}