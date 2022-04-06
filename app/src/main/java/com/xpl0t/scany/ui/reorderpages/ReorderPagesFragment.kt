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
import com.xpl0t.scany.extensions.runOnUiThread
import com.xpl0t.scany.models.Page
import com.xpl0t.scany.models.Scan
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.util.Optional
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
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
    private lateinit var pageList: RecyclerView
    private lateinit var pageItemAdapter: PageItemAdapter

    private var pages: List<Page>? = null

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
                pages = scan?.pages
                updateUI(scan)
            }
        }

        disposables.add {
            pageItemAdapter.pageOrderChanged
                .debounce(400, TimeUnit.MILLISECONDS)
                .subscribe {
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
        pageList = requireView().findViewById(R.id.pages)

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
        touchHelper.attachToRecyclerView(pageList)
        pageList.adapter = pageItemAdapter
        pageList.layoutManager = GridLayoutManager(requireContext(), 3)
        pageList.setHasFixedSize(true)
        pageList.setItemViewCacheSize(20)
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
                    runOnUiThread {
                        scanSubject.onNext(Optional(it))
                    }
                },
                onError = {
                    Log.e(TAG, "Could not get scan", it)
                    Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
                    runOnUiThread {
                        finish()
                    }
                }
            )
        }

    }

    private fun updateUI(scan: Scan?) {
        if (scan == null) {
            pageList.visibility = View.GONE
            return
        }

        pageList.visibility = View.VISIBLE
        pageItemAdapter.updateItems(scan.pages)
    }

    private fun updatePageOrder(pages: List<Page>) {
        Log.d(TAG, "Update page order")

        if (scan == null) return

        if (actionDisposable?.isDisposed == false) {
            Log.w(TAG, "Can not update page order, because previous query did not finish yet")
            return
        }

        // Ensuring the pages from the list adapter are congruent, with those from the database.
        val congruentPages = pages.map { p ->
            val dbEquivalent = this.pages?.find { it.id == p.id }
            p.copy(next = dbEquivalent?.next)
        }

        actionDisposable = repo.reorderPages(scan!!.id, congruentPages).subscribeBy(
            onNext = {
                Log.i(TAG, "Updated scan images")
                this.pages = it
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
