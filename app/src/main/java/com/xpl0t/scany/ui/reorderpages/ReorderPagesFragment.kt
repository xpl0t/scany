package com.xpl0t.scany.ui.reorderpages

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.add
import com.xpl0t.scany.extensions.finish
import com.xpl0t.scany.models.Scan
import com.xpl0t.scany.models.ScanImage
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.util.Optional
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

@AndroidEntryPoint
class ReorderPagesFragment : BaseFragment(R.layout.reorder_pages_fragment) {

    private val args: ReorderPagesFragmentArgs by navArgs()

    @Inject lateinit var repo: Repository

    private val disposables: MutableList<Disposable> = mutableListOf()
    private var actionDisposable: Disposable? = null

    private val scanSubject = BehaviorSubject.createDefault<Optional<Scan>>(Optional.empty())
    private var scan: Scan? = null

    private lateinit var toolbar: MaterialToolbar
    private lateinit var pages: RecyclerView
    private lateinit var pageItemAdapter: PageItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageItemAdapter = PageItemAdapter()
    }

    override fun onResume() {
        super.onResume()

        disposables.add {
            scanSubject.subscribe {
                val scan = if (it.isEmpty) null else it.value

                Log.d(TAG, "Scan subject next value (id: ${scan?.id})")
                this.scan = scan
                updateUI(scan)
            }
        }

        disposables.add {
            pageItemAdapter.pageOrderChanged.subscribe {
                Log.i(TAG, "Page order changed")
                updatePageOrder(it)
            }
        }

        getScan(args.scanId)
    }

    override fun onPause() {
        super.onPause()
        disposables.forEach { it.dispose() }
        actionDisposable?.dispose()
    }
    
    private fun initViews() {
        toolbar = requireView().findViewById(R.id.toolbar)
        pages = requireView().findViewById(R.id.pages)

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.apply -> {
                    if (actionDisposable?.isDisposed != false) {
                        finish()
                    }

                    true
                }
                else -> false
            }
        }

        val callback = PageMoveCallback(pageItemAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(pages)
        pages.adapter = pageItemAdapter
        pages.layoutManager = GridLayoutManager(requireContext(), 3)
        pages.setHasFixedSize(true)
        pages.setItemViewCacheSize(20)
    }

    private fun getScan(id: Int?) {
        Log.d(TAG, "get scan $id")

        if (id == null) {
            scanSubject.onNext(Optional.empty())
            return
        }

        disposables.add {
            repo.getScan(id).take(1).subscribeBy(
                onNext = {
                    Log.i(TAG, "Got scan (id: ${it.id})")
                    scanSubject.onNext(Optional(it))
                },
                onError = {
                    Log.e(TAG, "Could not get scan", it)
                    Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
                    finish()
                }
            )
        }

    }

    private fun updateUI(scan: Scan?) {
        if (scan == null) {
            pages.visibility = View.GONE
            return
        }

        pages.visibility = View.VISIBLE
        pageItemAdapter.updateItems(scan.images)
    }

    private fun updatePageOrder(pages: List<ScanImage>) {
        Log.d(TAG, "Update page order")

        if (scan == null) return

        actionDisposable?.dispose()

        val updatedScan = scan!!.copy(images = pages)

        actionDisposable = repo.updateScan(updatedScan).subscribeBy(
            onNext = {
                Log.i(TAG, "Updated scan images")
                scanSubject.onNext(Optional(it))
            },
            onError = {
                Log.e(TAG, "Could not update scan", it)
                Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
            }
        )
    }

    companion object {
        const val TAG = "ReorderPagesFragment"
    }
}
