package com.xpl0t.scany.ui.scanlist

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.showFragment
import com.xpl0t.scany.progressbar.ProgressBarService
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.scan.ScanFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

@AndroidEntryPoint
class ScanListFragment : BaseFragment(R.layout.scan_list_fragment) {
    @Inject
    lateinit var progressBarService: ProgressBarService

    private val disposables: MutableList<Disposable> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

       /* val disposable = getArpEntries().subscribeBy(
            onNext = { updateArpEntryList(it) },
            onError = {
                Snackbar.make(requireView(), R.string.scan_error, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.retry) { refreshTrigger.onNext(0) }
                    .show() // TODO(): Better error handling! Logging, debugMode...
            }
        )
        disposables.add(disposable)*/
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.forEach { it.dispose() }
    }

    private fun initViews() {
        val addScanBtn = requireView().findViewById<MaterialButton>(R.id.addScan)
        addScanBtn.setOnClickListener {
            parentFragmentManager.showFragment(ScanFragment())
        }
    }

    /*private fun getArpEntries(): Observable<List<ArpEntry>> {
        val arpEntries = refreshTrigger.switchMap {
            Observable.just(0)
                .doOnNext { swipeRefresh.isRefreshing = true }
                .concatMap { arp.getEntries("wlan0", 5, 2000) }
                .doOnNext { swipeRefresh.isRefreshing = false }
        }

        return arpEntries
    }*/
}