package com.xpl0t.scany.ui.documentlist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.forEach
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.add
import com.xpl0t.scany.extensions.runOnUiThread
import com.xpl0t.scany.models.Document
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.services.AuthorizationService
import com.xpl0t.scany.services.BillingService
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.document.DocumentFragment
import com.xpl0t.scany.ui.document.DocumentFragmentListener
import com.xpl0t.scany.ui.documentlist.documentnamegenerator.DocumentNameGenerator
import com.xpl0t.scany.util.Optional
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject


@AndroidEntryPoint
class DocumentListFragment : BaseFragment(R.layout.document_list_fragment) {

    @Inject
    lateinit var repo: Repository

    @Inject
    lateinit var billingService: BillingService

    @Inject
    lateinit var documentNameGenerator: DocumentNameGenerator

    @Inject
    lateinit var authorizationService: AuthorizationService

    private val getDocumentsTrigger = BehaviorSubject.createDefault(0)

    private lateinit var themedCtx: Context

    private lateinit var toolbar: MaterialToolbar
    private lateinit var documentFragmentContainer: FragmentContainerView
    private lateinit var list: RecyclerView
    private var listAdapter: DocumentListItemAdapter? = null

    // private lateinit var failedCard: FailedCard

    private val disposables: MutableList<Disposable> = mutableListOf()
    private var actionDisposable: Disposable? = null

    private var currentDocumentSubject = BehaviorSubject.createDefault<Optional<Int>>(Optional.empty())

    private val documentFragment: DocumentFragmentListener get() = documentFragmentContainer.getFragment()
    private val currentDocument: Int?
        get() =
            if (currentDocumentSubject.value!!.isEmpty) null else currentDocumentSubject.value!!.value

    private var documents: List<Document>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listAdapter = DocumentListItemAdapter(requireContext())

        val curDocument = savedInstanceState?.getInt(CUR_DOCUMENT_ID) ?: 0
        val curDocumentOpt = if (curDocument > 0) Optional(curDocument) else Optional.empty()
        currentDocumentSubject.onNext(curDocumentOpt)
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val inflater = super.onGetLayoutInflater(savedInstanceState)
        themedCtx = ContextThemeWrapper(requireContext(), R.style.Theme_Scany_DocumentList)
        return inflater.cloneInContext(themedCtx)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onResume() {
        super.onResume()

        disposables.add {
            currentDocumentSubject
                .distinctUntilChanged { v1, v2 ->
                    if (v1.isEmpty == v2.isEmpty) v1.isEmpty || v1.value == v2.value
                    else false
                }
                .subscribe {
                    Log.d(TAG, "Current document subject emitted ${if (it.isEmpty) null else it.value}")
                    updateToolbar(it)

                    listAdapter?.selectItem(if (it.isEmpty) null else it.value)

                    if (it.isEmpty) {
                        documentFragment.showDocument(null)
                        documentFragment.hide()
                        return@subscribe
                    }

                    documentFragment.showDocument(it.value)
                    documentFragment.expand()
                }
        }

        disposables.add {
            getDocuments().subscribe {
                Log.i(TAG, "Got documents")
                // failedCard.visibility = View.GONE
                // documentRadioGroup.visibility = View.VISIBLE
                runOnUiThread {
                    updateDocumentList(it)
                }
            }
        }

        disposables.add {
            billingService.isSubscribedObs.subscribe {
                runOnUiThread {
                    var title = resources.getText(R.string.app_name).toString()
                    if (it) title += " ${resources.getString(R.string.pro_tag_inverse)}"
                    toolbar.title = title
                }
            }
        }

        disposables.add {
            listAdapter!!.documentClicked.subscribe {
                currentDocumentSubject.onNext(Optional(it.id))
            }
        }

        /*disposables.add {
            listAdapter.documentClicked.subscribe {
                Log.i(TAG, "Document card clicked (id: ${it.id})")
                showDocumentView(it.id)
            }
        }*/
    }

    override fun onPause() {
        super.onPause()
        disposables.forEach { it.dispose() }
        actionDisposable?.dispose()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CUR_DOCUMENT_ID, currentDocument ?: 0)
    }

    private fun initViews() {
        val addDocumentBtn = requireView().findViewById<MaterialButton>(R.id.addDocument)
        addDocumentBtn.setOnClickListener {
            Log.i(TAG, "Add document btn clicked")
            addDocument()
        }

        toolbar = requireView().findViewById(R.id.toolbar)
        documentFragmentContainer = requireView().findViewById(R.id.documentFragment)

        toolbar.setOnMenuItemClickListener { handleMenuItem(it) }

        list = requireView().findViewById(R.id.document_list)
        list.adapter = listAdapter

        /*failedCard = requireView().findViewById(R.id.failed)
        failedCard.setOnClickListener {
            getDocumentsTrigger.onNext(0)
        }*/
    }

    private fun handleMenuItem(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.deleteDocument -> {
                Log.d(DocumentFragment.TAG, "Delete document menu item clicked")
                showDeleteDocumentDlg()
                true
            }
            R.id.export -> {
                Log.d(DocumentFragment.TAG, "Export pdf menu item clicked")
                documentFragment.export()
                true
            }
            R.id.editTitle -> {
                Log.d(DocumentFragment.TAG, "Edit title menu item clicked")
                documentFragment.renameTitle()
                true
            }
            R.id.reorder -> {
                Log.d(DocumentFragment.TAG, "Reorder pages menu item clicked")
                documentFragment.reorderPages()
                true
            }
            else -> false
        }
    }

    private fun getDocuments(): Observable<List<Document>> {
        return getDocumentsTrigger.switchMap {
            repo.getDocuments().onErrorComplete {
                Log.e(TAG, "Get documents failed", it)
                // list.visibility = View.GONE
                // failedCard.visibility = View.VISIBLE

                true
            }
        }
    }

    private fun updateDocumentList(documents: List<Document>) {
        val documentNamesStr = documents.map { it.name }.joinToString()
        Log.i(TAG, "Update radio buttons $documentNamesStr")

        this.documents = documents
        listAdapter?.updateItems(documents)
    }

    private fun addDocument() {
        Log.d(DocumentFragment.TAG, "Add document")
        if (actionDisposable?.isDisposed == false) return

        if (!authorizationService.canAddDocument(documents?.size ?: 0)) {
            val reason = resources.getString(
                R.string.view_sub_reason_doc_limit,
                AuthorizationService.FREE_TIER_MAX_DOCUMENTS
            )
            val action = DocumentListFragmentDirections
                .actionDocumentListFragmentToViewSubscriptionFragment(reason)
            findNavController().navigate(action)

            return
        }

        actionDisposable = documentNameGenerator.generate()
            .concatMap { repo.addDocument(Document(name = it)) }
            .take(1)
            .subscribeBy(
                onNext = {
                    Log.i(DocumentFragment.TAG, "Created document (id: ${it.id})")
                    runOnUiThread {
                        currentDocumentSubject.onNext(Optional(it.id))
                    }
                },
                onError = {
                    Log.e(DocumentFragment.TAG, "Could not add document", it)
                    Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
                }
            )
    }

    private fun showDeleteDocumentDlg() {
        Log.d(DocumentFragment.TAG, "Show delete document dialog")

        if (currentDocumentSubject.value!!.isEmpty) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.delete_document_dlg_title))
            .setNegativeButton(resources.getString(R.string.cancel_btn)) { _, _ ->
                Log.d(DocumentFragment.TAG, "Delete document canceled")
            }
            .setPositiveButton(resources.getString(R.string.delete_document_dlg_delete)) { _, _ ->
                deleteDocument()
            }
            .show()
    }

    private fun deleteDocument() {
        Log.d(DocumentFragment.TAG, "Delete document")

        val curDocument = currentDocumentSubject.value!!
        if (curDocument.isEmpty || actionDisposable?.isDisposed == false) return

        actionDisposable = repo.removeDocument(curDocument.value).take(1).subscribeBy(
            onNext = {
                Log.i(DocumentFragment.TAG, "Deleted document")
                runOnUiThread {
                    currentDocumentSubject.onNext(Optional.empty())
                }
            },
            onError = {
                Log.e(DocumentFragment.TAG, "Could not delete document", it)
                Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT)
                    .show()
            }
        )
    }

    private fun updateToolbar(currentDocument: Optional<Int>) {
        val menuItemVisible = !currentDocument.isEmpty

        toolbar.menu.forEach {
            it.isVisible = menuItemVisible
        }
    }

    companion object {
        const val TAG = "DocumentListFragment"
        const val CUR_DOCUMENT_ID = "CUR_DOCUMENT_ID"
    }
}