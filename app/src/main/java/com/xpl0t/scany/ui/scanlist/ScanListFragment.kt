package com.xpl0t.scany.ui.scanlist

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.core.view.allViews
import androidx.core.view.forEach
import androidx.fragment.app.FragmentContainerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.snackbar.Snackbar
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.add
import com.xpl0t.scany.extensions.getThemeColor
import com.xpl0t.scany.models.Scan
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.scan.ScanFragment
import com.xpl0t.scany.ui.scan.ScanFragmentListener
import com.xpl0t.scany.ui.scanlist.scannamegenerator.ScanNameGenerator
import com.xpl0t.scany.util.Optional
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject


@AndroidEntryPoint
class ScanListFragment : BaseFragment(R.layout.scan_list_fragment) {

    @Inject lateinit var repo: Repository
    @Inject lateinit var scanNameGenerator: ScanNameGenerator

    private val getScansTrigger = BehaviorSubject.createDefault(0)

    private lateinit var toolbar: MaterialToolbar
    private lateinit var scanRadioGroup: RadioGroup
    private lateinit var scanFragmentContainer: FragmentContainerView
    // private lateinit var list: RecyclerView
    // private val listAdapter = ScanItemAdapter()

    // private lateinit var failedCard: FailedCard

    private val disposables: MutableList<Disposable> = mutableListOf()
    private var actionDisposable: Disposable? = null

    private var currentScanSubject = BehaviorSubject.createDefault<Optional<Int>>(Optional.empty())

    private val scanFragment: ScanFragmentListener get() = scanFragmentContainer.getFragment()
    private val currentScan: Int? get() =
        if (currentScanSubject.value!!.isEmpty) null else currentScanSubject.value!!.value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val curScan = savedInstanceState?.getInt(CUR_SCAN_ID) ?: 0
        val curScanOpt = if (curScan > 0) Optional(curScan) else Optional.empty()
        currentScanSubject.onNext(curScanOpt)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onResume() {
        super.onResume()

        disposables.add {
            currentScanSubject
                .distinctUntilChanged { v1, v2 ->
                    if (v1.isEmpty == v2.isEmpty) v1.isEmpty || v1.value == v2.value
                    else false
                }
                .subscribe {
                    Log.d(TAG, "Current scan subject emitted ${if (it.isEmpty) null else it.value}")
                    // bundle.putInt(CUR_SCAN_ID, currentScan ?: 0)
                    updateToolbar(it)

                    if (it.isEmpty) {
                        scanFragment.showScan(null)
                        scanFragment.hide()
                        scanRadioGroup.clearCheck()
                        return@subscribe
                    }

                    scanFragment.showScan(it.value)
                    scanFragment.expand()
                    scanRadioGroup.check(it.value)
                }
        }

        disposables.add {
            getScans().subscribe {
                Log.i(TAG, "Got scans")
                // failedCard.visibility = View.GONE
                // scanRadioGroup.visibility = View.VISIBLE
                updateScanList(it)
            }
        }

        /*disposables.add {
            listAdapter.scanClicked.subscribe {
                Log.i(TAG, "Scan card clicked (id: ${it.id})")
                showScanView(it.id)
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
        outState.putInt(CUR_SCAN_ID, currentScan ?: 0)
    }

    private fun initViews() {
        val addScanBtn = requireView().findViewById<MaterialButton>(R.id.addScan)
        addScanBtn.setOnClickListener {
            Log.i(TAG, "Add scan btn clicked")
            addScan()
        }

        toolbar = requireView().findViewById(R.id.toolbar)
        scanFragmentContainer = requireView().findViewById(R.id.scanFragment)
        scanRadioGroup = requireView().findViewById(R.id.scanList)

        toolbar.inflateMenu(R.menu.scan_menu)
        toolbar.setOnMenuItemClickListener { handleMenuItem(it) }

        scanRadioGroup.setOnCheckedChangeListener { _, id ->
            if (id == -1) return@setOnCheckedChangeListener

            Log.d(TAG, "Selected scan $id")
            currentScanSubject.onNext(Optional(id))
        }

        // list = requireView().findViewById(R.id.scanList)
        // list.adapter = listAdapter
        // list.layoutManager = LinearLayoutManager(context)

        /*failedCard = requireView().findViewById(R.id.failed)
        failedCard.setOnClickListener {
            getScansTrigger.onNext(0)
        }*/
    }

    private fun handleMenuItem(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.deleteScan -> {
                Log.d(ScanFragment.TAG, "Delete scan menu item clicked")
                showDeleteScanDlg()
                true
            }
            R.id.export -> {
                Log.d(ScanFragment.TAG, "Export pdf menu item clicked")
                scanFragment.export()
                true
            }
            R.id.editTitle -> {
                Log.d(ScanFragment.TAG, "Edit title menu item clicked")
                scanFragment.renameTitle()
                true
            }
            R.id.reorder -> {
                Log.d(ScanFragment.TAG, "Reorder pages menu item clicked")
                scanFragment.reorderPages()
                true
            }
            else -> false
        }
    }

    private fun getScans(): Observable<List<Scan>> {
        return getScansTrigger.switchMap {
            repo.getScans().onErrorComplete {
                Log.e(TAG, "Get scans failed", it)
                // list.visibility = View.GONE
                // failedCard.visibility = View.VISIBLE

                true
            }
        }
    }

    private fun updateScanList(scans: List<Scan>) {
        scanRadioGroup.clearCheck()

        // Add & update radio buttons
        for (scan in scans) {
            var radioBtn = scanRadioGroup.allViews.find { it.id == scan.id } as MaterialRadioButton?
            if (radioBtn == null) {
                radioBtn = createRadioButton()
                scanRadioGroup.addView(radioBtn)
            }

            radioBtn.id = scan.id
            radioBtn.text = scan.name
        }

        // Remove radio buttons
        for (radioBtn in scanRadioGroup.allViews.toList()) {
            if (radioBtn.javaClass != MaterialRadioButton::class.java) {
                continue
            }

            if (!scans.any { it.id == radioBtn.id }) {
                scanRadioGroup.removeView(radioBtn)
            }
        }

        currentScanSubject.value!!.run {
            if (!isEmpty) scanRadioGroup.check(value)
        }
    }

    private fun createRadioButton(): MaterialRadioButton {
        val color = requireContext().getThemeColor(com.google.android.material.R.attr.colorOnPrimary)

        return MaterialRadioButton(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            setTextColor(color)
            buttonTintList = color
        }
    }

    private fun addScan() {
        Log.d(ScanFragment.TAG, "Add scan")
        if (actionDisposable?.isDisposed == false) return

        val scanName = scanNameGenerator.generate()
        val scan = Scan(name = scanName)

        actionDisposable = repo.addScan(scan).take(1).subscribeBy(
            onNext = {
                Log.i(ScanFragment.TAG, "Created scan (id: ${it.id})")
                currentScanSubject.onNext(Optional(it.id))
            },
            onError = {
                Log.e(ScanFragment.TAG, "Could not add scan", it)
                Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
            }
        )
    }

    private fun showDeleteScanDlg() {
        Log.d(ScanFragment.TAG, "Show delete scan dialog")

        if (currentScanSubject.value!!.isEmpty) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.delete_scan_dlg_title))
            .setNegativeButton(resources.getString(R.string.cancel_btn)) { _, _ ->
                Log.d(ScanFragment.TAG, "Delete scan canceled")
            }
            .setPositiveButton(resources.getString(R.string.delete_scan_dlg_delete)) { _, _ ->
                deleteScan()
            }
            .show()
    }

    private fun deleteScan() {
        Log.d(ScanFragment.TAG, "Delete scan")

        val curScan = currentScanSubject.value!!
        if (curScan.isEmpty || actionDisposable?.isDisposed == false) return

        repo.removeScan(curScan.value).subscribeBy(
            onNext = {
                Log.i(ScanFragment.TAG, "Deleted scan")
                currentScanSubject.onNext(Optional.empty())
            },
            onError = {
                Log.e(ScanFragment.TAG, "Could not delete scan", it)
                Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT)
                    .show()
            }
        )
    }

    private fun updateToolbar(currentScan: Optional<Int>) {
        val menuItemVisible = !currentScan.isEmpty

        toolbar.menu.forEach {
            it.isVisible = menuItemVisible
        }
    }

    companion object {
        const val TAG = "ScanListFragment"
        const val CUR_SCAN_ID = "CUR_SCAN_ID"
    }
}