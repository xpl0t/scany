package com.xpl0t.scany.ui.export

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.add
import com.xpl0t.scany.extensions.finish
import com.xpl0t.scany.extensions.runOnUiThread
import com.xpl0t.scany.models.Page
import com.xpl0t.scany.models.Scan
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.services.PageSizeService
import com.xpl0t.scany.services.pdf.PdfService
import com.xpl0t.scany.services.ShareService
import com.xpl0t.scany.services.pdf.ScaleType
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.scan.ScanFragment
import com.xpl0t.scany.util.Optional
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

@AndroidEntryPoint
class ExportFragment : BaseFragment(R.layout.export_fragment) {

    private val args: ExportFragmentArgs by navArgs()

    @Inject
    lateinit var repo: Repository

    @Inject
    lateinit var pageSelectionService: PageSelectionService

    @Inject
    lateinit var pageSizeService: PageSizeService

    @Inject
    lateinit var pdfService: PdfService

    @Inject
    lateinit var shareService: ShareService

    private val disposables: MutableList<Disposable> = mutableListOf()
    private var actionDisposable: Disposable? = null

    private val scanSubject = BehaviorSubject.createDefault<Optional<Scan>>(Optional.empty())
    private var scan: Scan? = null

    private lateinit var toolbar: MaterialToolbar
    private lateinit var pageList: RecyclerView
    private lateinit var pageItemAdapter: PageItemAdapter
    private lateinit var selectAllChip: Chip
    private lateinit var clearSelectionChip: Chip
    private lateinit var pageSizeDropDown: MaterialAutoCompleteTextView
    private lateinit var pageSizeAdapter: ArrayAdapter<String>
    private lateinit var documentTypeDropDown: MaterialAutoCompleteTextView
    private lateinit var documentTypeAdapter: ArrayAdapter<String>
    private lateinit var shareBtn: MaterialButton
    private lateinit var scaleTypeChipGroup: ChipGroup

    private var pages: List<Page>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageItemAdapter = PageItemAdapter(pageSelectionService)
        pageSizeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            pageSizeService.getPageSizeStrings()
        )
        documentTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            getDocumentTypes()
        )
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
        scaleTypeChipGroup = requireView().findViewById(R.id.img_scale_type_chip_group)

        pageSizeDropDown = requireView().findViewById(R.id.page_size)
        pageSizeDropDown.setAdapter(pageSizeAdapter)
        pageSizeDropDown.setText(pageSizeService.getDefaultPageSizeString(), false)
        pageSizeDropDown.setOnDismissListener {
            pageSizeDropDown.clearFocus()
        }

        documentTypeDropDown = requireView().findViewById(R.id.document_type)
        documentTypeDropDown.setAdapter(documentTypeAdapter)
        documentTypeDropDown.setText(getDocumentTypes().first(), false)
        documentTypeDropDown.setOnDismissListener {
            documentTypeDropDown.clearFocus()
        }

        selectAllChip = requireView().findViewById(R.id.select_all)
        selectAllChip.setOnClickListener { pageItemAdapter.selectAll() }

        clearSelectionChip = requireView().findViewById(R.id.clear_selection)
        clearSelectionChip.setOnClickListener { pageItemAdapter.clearSelection() }

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.share -> {
                    if (actionDisposable?.isDisposed != false) {
                        finish()
                    }

                    true
                }
                else -> false
            }
        }

        pageList.adapter = pageItemAdapter
        pageList.layoutManager = GridLayoutManager(requireContext(), 3)
        pageList.setHasFixedSize(true)
        pageList.setItemViewCacheSize(20)

        shareBtn = requireView().findViewById(R.id.share)
        shareBtn.setOnClickListener {
            genPdfAndShare()
        }
    }

    private fun getDocumentTypes(): List<String> {
        return resources.getStringArray(R.array.document_types).toList()
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

    private fun genPdfAndShare() {
        Log.i(TAG, "Generate PDF and share")
        if (actionDisposable?.isDisposed == false || scan == null)
            return

        shareBtn.isEnabled = false

        val deselectedPages = pageSelectionService.getDeselectedPages()
        val pageIds = scan!!.pages
            .filter { !deselectedPages.contains(it.id) }
            .map { it.id }

        val imageObservables = pageIds.map {
            repo.getPageImage(it).toObservable()
        }
        Observable.combineLatestArray(imageObservables.toTypedArray()) { it.toList() as List<ByteArray> }.subscribe(
            {
                val mediaSize = pageSizeService.getMediaSizeForPageSizeStr(pageSizeDropDown.text.toString())
                val scaleType = when (scaleTypeChipGroup.checkedChipId) {
                    R.id.fit_img -> ScaleType.Fit
                    R.id.center_img_inside -> ScaleType.CenterInside
                    else -> ScaleType.Fit
                }
                val pdf = pdfService.getPdfFromImages(it, mediaSize!!, scaleType)
                shareService.share(context!!, pdf, "application/pdf")
                runOnUiThread {
                    shareBtn.isEnabled = true
                }
            },
            {
                Log.e(ScanFragment.TAG, "Could not get page images and generate pdf", it)
                Snackbar.make(requireView(), R.string.export_pdf_error, Snackbar.LENGTH_SHORT).show()
                runOnUiThread {
                    shareBtn.isEnabled = true
                }
            }
        )
    }

    companion object {
        const val TAG = "ExportFragment"
    }
}
