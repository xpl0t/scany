package com.xpl0t.scany.ui

import android.os.Bundle
import com.xpl0t.scany.R
import com.xpl0t.scany.ui.common.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val disposables: MutableList<Disposable> = mutableListOf()

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
}