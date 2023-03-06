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
import com.xpl0t.scany.models.Document
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

    @Inject
    lateinit var repo: Repository

    private val disposables: MutableList<Disposable> = mutableListOf()
    private var actionDisposable: Disposable? = null

    private val documentSubject = BehaviorSubject.createDefault<Optional<Document>>(Optional.empty())
    private var document: Document? = null

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
            documentSubject.subscribe {
                val document = if (it.isEmpty) null else it.value

                Log.d(TAG, "Document subject next value (id: ${document?.id})")
                this.document = document
                pages = document?.pages
                updateUI(document)
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

        getDocument(args.documentId)
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

    private fun getDocument(id: Int?) {
        Log.d(TAG, "get document $id")

        if (id == null) {
            documentSubject.onNext(Optional.empty())
            return
        }

        disposables.add {
            repo.getDocument(id).take(1).subscribeBy(
                onNext = {
                    Log.i(TAG, "Got document (id: ${it.id})")
                    runOnUiThread {
                        documentSubject.onNext(Optional(it))
                    }
                },
                onError = {
                    Log.e(TAG, "Could not get document", it)
                    Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
                    runOnUiThread {
                        finish()
                    }
                }
            )
        }

    }

    private fun updateUI(document: Document?) {
        if (document == null) {
            pageList.visibility = View.GONE
            return
        }

        pageList.visibility = View.VISIBLE
        pageItemAdapter.updateItems(document.pages)
    }

    private fun updatePageOrder(pages: List<Page>) {
        Log.d(TAG, "Update page order")

        if (document == null) return

        if (actionDisposable?.isDisposed == false) {
            Log.w(TAG, "Can not update page order, because previous query did not finish yet")
            return
        }

        // Ensuring the pages from the list adapter are congruent, with those from the database.
        val congruentPages = pages.map { p ->
            val dbEquivalent = this.pages?.find { it.id == p.id }!!
            p.copy(order = dbEquivalent.order)
        }

        actionDisposable = repo.reorderPages(document!!.id, congruentPages).subscribeBy(
            onNext = {
                Log.i(TAG, "Updated document images")
                this.pages = it
            },
            onError = {
                Log.e(TAG, "Could not update document", it)
                Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
            }
        )
    }

    companion object {
        const val TAG = "ReorderPagesFragment"
    }
}
