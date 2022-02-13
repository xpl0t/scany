package com.xpl0t.scany.ui

import android.os.Bundle
import android.view.View
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.xpl0t.scany.R
import com.xpl0t.scany.progressbar.ProgressBarService
import com.xpl0t.scany.ui.common.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Inject
    lateinit var progressBarService: ProgressBarService

    private val disposables: MutableList<Disposable> = mutableListOf()

    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        initViews()

        initProgressBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.forEach { it.dispose() }
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        toolbar = findViewById(R.id.toolbar)
    }

    private fun initProgressBar() {
        val disposable = progressBarService.loadingSubject.subscribe {
            runOnUiThread {
                progressBar.visibility = if (it) View.VISIBLE else View.GONE
            }
        }

        disposables.add(disposable)
    }
}