package com.xpl0t.scany.ui.scanlist

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.showFragment
import com.xpl0t.scany.models.Scan
import com.xpl0t.scany.progressbar.ProgressBarService
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.scan.ScanFragment
import com.xpl0t.scany.views.FailedCard
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

@AndroidEntryPoint
class ScanListFragment : BaseFragment(R.layout.scan_list_fragment) {

    private val logTag = "ScanListFragment"

    @Inject
    lateinit var repo: Repository

    private val getScansTrigger = BehaviorSubject.createDefault(0)

    private lateinit var list: RecyclerView
    private val listAdapter = ScanItemAdapter()

    private lateinit var failedCard: FailedCard

    private val disposables: MutableList<Disposable> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onResume() {
        super.onResume()

        val disposable = getScans().subscribe {
            Log.i(logTag, "Got scans")
            failedCard.visibility = View.GONE
            list.visibility = View.VISIBLE
            updateScanList(it)
        }
        disposables.add(disposable)
    }

    override fun onPause() {
        super.onPause()
        disposables.forEach { it.dispose() }
    }

    private fun initViews() {
        val addScanBtn = requireView().findViewById<MaterialButton>(R.id.addScan)
        addScanBtn.setOnClickListener {
            parentFragmentManager.showFragment(ScanFragment())
        }

        list = requireView().findViewById(R.id.scanList)
        list.adapter = listAdapter
        list.layoutManager = LinearLayoutManager(context)

        failedCard = requireView().findViewById(R.id.failed)
        failedCard.setOnClickListener {
            getScansTrigger.onNext(0)
        }
    }

    private fun getScans(): Observable<List<Scan>> {
        return getScansTrigger.switchMap {
            repo.getScans().onErrorComplete {
                Log.e(logTag, "Get scans failed", it)
                list.visibility = View.GONE
                failedCard.visibility = View.VISIBLE

                true
            }
        }
    }

    private fun updateScanList(scans: List<Scan>) {
        listAdapter.updateItems(scans)
    }
}