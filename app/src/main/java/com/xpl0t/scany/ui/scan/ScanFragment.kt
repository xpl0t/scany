package com.xpl0t.scany.ui.scan

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.add
import com.xpl0t.scany.extensions.runOnUiThread
import com.xpl0t.scany.models.Scan
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.services.AuthorizationService
import com.xpl0t.scany.services.pdf.PdfService
import com.xpl0t.scany.services.ShareService
import com.xpl0t.scany.services.backpress.BackPressHandler
import com.xpl0t.scany.services.backpress.BackPressHandlerService
import com.xpl0t.scany.services.pdf.ScaleType
import com.xpl0t.scany.ui.scanlist.ScanListFragmentDirections
import com.xpl0t.scany.util.Optional
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject


@AndroidEntryPoint
class ScanFragment : BottomSheetDialogFragment(), ScanFragmentListener, BackPressHandler {

    @Inject
    lateinit var repo: Repository

    @Inject
    lateinit var backPressHandlerService: BackPressHandlerService

    @Inject
    lateinit var pdfService: PdfService

    @Inject
    lateinit var shareService: ShareService

    @Inject
    lateinit var authorizationService: AuthorizationService

    private val disposables: MutableList<Disposable> = mutableListOf()
    private var scanDisposable: Disposable? = null
    private var actionDisposable: Disposable? = null

    private val scanSubject = BehaviorSubject.createDefault<Optional<Scan>>(Optional.empty())
    private var scan: Scan? = null

    private lateinit var bottomSheetHeader: ConstraintLayout
    private lateinit var titleTextView: MaterialTextView
    private lateinit var bottomSheetToggle: ImageView
    private lateinit var noPageCard: MaterialCardView
    private lateinit var pageList: RecyclerView
    private lateinit var pageAdapter: PageItemAdapter
    private lateinit var addPageHeaderBtn: MaterialButton
    private lateinit var addPageBtn: MaterialButton

    private lateinit var bs: BottomSheetBehavior<View>

    private var currentState: Int = BottomSheetBehavior.STATE_COLLAPSED

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.scan_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        bs = BottomSheetBehavior.from(requireView().parent as View)
        bs.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                this@ScanFragment.onStateChanged(bottomSheet, newState)
            }
        })

        bs.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageAdapter = PageItemAdapter(requireContext())
    }

    override fun onResume() {
        super.onResume()
        backPressHandlerService.setHandler(this)

        disposables.add {
            scanSubject.subscribe {
                val scan = if (it.isEmpty) null else it.value

                Log.d(TAG, "Scan subject next value (id: ${scan?.id})")
                this.scan = scan
                updateUI(scan)
            }
        }

        disposables.add {
            pageAdapter.pageClicked.subscribe {
                val action = ScanListFragmentDirections
                    .actionScanListFragmentToViewPageFragment(it.id)
                findNavController().navigate(action)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        backPressHandlerService.resetHandler()

        disposables.forEach { it.dispose() }
        scanDisposable?.dispose()
        actionDisposable?.dispose()
    }

    override fun onBackPressed() {
        if (currentState == BottomSheetBehavior.STATE_COLLAPSED)
            activity?.finish()
        else
            hide()
    }

    private fun initViews() {
        bottomSheetHeader = requireView().findViewById(R.id.backDropHeader)
        titleTextView = requireView().findViewById(R.id.title)
        bottomSheetToggle = requireView().findViewById(R.id.toggleBottomSheet)
        noPageCard = requireView().findViewById(R.id.noPageCard)
        pageList = requireView().findViewById(R.id.pageList)
        addPageHeaderBtn = requireView().findViewById(R.id.addPageHeader)
        addPageBtn = requireView().findViewById(R.id.addPage)

        pageList.adapter = pageAdapter
        pageList.setHasFixedSize(true)
        pageList.setItemViewCacheSize(5)

        titleTextView.setOnClickListener {
            if (currentState == BottomSheetBehavior.STATE_COLLAPSED) {
                expand()
                return@setOnClickListener
            }

            if (scan != null)
                renameTitle()
        }

        addPageHeaderBtn.setOnClickListener {
            showCameraFragment()
        }

        addPageBtn.setOnClickListener {
            showCameraFragment()
        }

        bottomSheetHeader.setOnClickListener {
            expand()
        }

        bottomSheetToggle.setOnClickListener {
            Log.d(TAG, "Toggle bottom sheet clicked")

            if (bs.state == BottomSheetBehavior.STATE_EXPANDED)
                hide()
            else
                expand()
        }
    }

    fun onStateChanged(bottomSheet: View, newState: Int) {
        currentState = newState
    }

    override fun showScan(id: Int?) {
        Log.d(TAG, "Show scan $id")

        if (id == null) {
            scanSubject.onNext(Optional.empty())
            return
        }

        // If the scan changed reset the list animation
        if (id != scan?.id) {
            pageAdapter.resetAnimation()
        }

        scanDisposable?.dispose()

        scanDisposable = repo.getScan(id).take(1).subscribeBy(
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
                    scanSubject.onNext(Optional.empty())
                }
            }
        )
    }

    private fun renameTitleBase(name: String) {
        if (actionDisposable?.isDisposed == false) return

        val newScan = scan!!.copy(name = name)
        actionDisposable = repo.updateScan(newScan).subscribeBy(
            onNext = {
                Log.i(TAG, "Updated scan name successfully (id: ${it.id})")
                runOnUiThread {
                    scanSubject.onNext(Optional(it))
                }
            },
            onError = {
                Log.e(TAG, "Could not set scan name", it)
                Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
            }
        )
    }

    override fun renameTitle() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            val dlgView = LayoutInflater.from(context).inflate(R.layout.edit_name_dlg, null)
            val inputLayout = dlgView.findViewById<TextInputLayout>(R.id.nameInputLayout)
            val editText = dlgView.findViewById<TextInputEditText>(R.id.nameEditText)
            editText.setText(scan?.name ?: "")
            editText.requestFocus()

            setTitle(resources.getString(R.string.edit_name_dlg_title))
            setIcon(R.drawable.edit)
            setView(dlgView)

            setPositiveButton(resources.getString(R.string.apply_btn), null)
            setNegativeButton(resources.getString(R.string.cancel_btn), null)

            val dlg = show()
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val scanName = editText.text.toString()
                val err = validateScanName(scanName)
                if (err != null) {
                    inputLayout.error = err
                } else {
                    renameTitleBase(scanName)
                    dlg.dismiss()
                }
            }
        }
    }

    override fun export() {
        Log.d(TAG, "Export PDF")

        val pageCount = scan?.pages?.count()
        if (pageCount == 0) {
            Snackbar.make(requireView(), R.string.export_no_page, Snackbar.LENGTH_SHORT).show()
            return
        }

        val action = ScanListFragmentDirections
            .actionScanListFragmentToExportFragment(scan?.id ?: return)
        findNavController().navigate(action)
    }

    override fun reorderPages() {
        Log.d(TAG, "Reorder pages")

        val pageCount = scan?.pages?.count()
        if (pageCount != null && pageCount < 2) {
            Snackbar.make(requireView(), R.string.reorder_min_2_pages, Snackbar.LENGTH_SHORT).show()
            return
        }

        val action = ScanListFragmentDirections
            .actionScanListFragmentToReorderPagesFragment(scan?.id ?: return)
        findNavController().navigate(action)
    }

    override fun expand() {
        bs.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetToggle.setImageResource(R.drawable.clear)
    }

    override fun hide() {
        bs.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetToggle.setImageResource(R.drawable.arrow_up)
    }

    private fun updateUI(scan: Scan?) {
        if (scan == null) {
            titleTextView.text = ""
            addPageHeaderBtn.visibility = View.GONE
            pageList.visibility = View.GONE
            noPageCard.visibility = View.GONE
            return
        }

        addPageHeaderBtn.visibility = View.VISIBLE

        noPageCard.visibility = if (scan.pages.isEmpty()) View.VISIBLE else View.GONE
        pageList.visibility = if (scan.pages.isNotEmpty()) View.VISIBLE else View.GONE

        titleTextView.text = scan.name
        pageAdapter.updateItems(scan.pages)
    }

    /**
     * Validate a scan name.
     *
     * @return Null if name is valid and a error text otherwise.
     */
    private fun validateScanName(name: String): String? {
        return if (name.isEmpty()) resources.getString(R.string.name_to_short_err) else null
    }

    private fun showCameraFragment() {
        Log.i(TAG, "Show scan fragment")

        if (!authorizationService.canAddPage(scan?.pages?.size ?: 0)) {
            val reason = resources.getString(
                R.string.view_sub_reason_page_limit,
                AuthorizationService.FREE_TIER_MAX_PAGES
            )
            val action = ScanListFragmentDirections
                .actionScanListFragmentToViewSubscriptionFragment(reason)
            findNavController().navigate(action)

            return
        }

        val action = ScanListFragmentDirections
            .actionScanListFragmentToCameraFragment(scan?.id ?: return)
        findNavController().navigate(action)
    }

    companion object {
        const val TAG = "ScanFragment"
    }
}
