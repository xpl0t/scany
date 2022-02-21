package com.xpl0t.scany.ui.scan

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.add
import com.xpl0t.scany.extensions.finish
import com.xpl0t.scany.models.Scan
import com.xpl0t.scany.models.ScanImage
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.scan.scannamegenerator.ScanNameGenerator
import com.xpl0t.scany.ui.scanimage.ScanBitmaps
import com.xpl0t.scany.ui.scanimage.improve.ImproveFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject


@AndroidEntryPoint
class ScanFragment : BaseFragment(R.layout.scan_fragment) {

    private val args: ScanFragmentArgs by navArgs()

    @Inject lateinit var repo: Repository
    @Inject lateinit var scanNameGenerator: ScanNameGenerator

    private val disposables: MutableList<Disposable> = mutableListOf()
    private var actionDisposable: Disposable? = null

    private val scanSubject = PublishSubject.create<Scan>()
    private var scan: Scan? = null

    private lateinit var contentGroup: ConstraintLayout
    private lateinit var nameTextView: TextView
    private lateinit var editNameBtn: MaterialButton
    private lateinit var imageList: RecyclerView
    private lateinit var imageListAdapter: ScanImageItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageListAdapter = ScanImageItemAdapter(requireContext())

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<ScanBitmaps>(ImproveFragment.SCAN_BITMAPS)?.observe(this) {
            Log.d(TAG, "Got scan bitmaps")
            addScanImage(it)
        }
    }



    override fun onResume() {
        super.onResume()

        disposables.add {
            scanSubject.subscribe {
                Log.d(TAG, "Scan subject next value (id: ${it.id})")
                scan = it
                updateUI(it)
            }
        }

        disposables.add {
            imageListAdapter.scanImagesOrderChanged.subscribe {
                Log.i(TAG, "Scan image order changed")
                updateScanImages(it)
            }
        }

        val id = scan?.id ?: args.scanId
        val firstScanObs = if (id > 0) repo.getScan(id).take(1)
            else createScan()

        if (actionDisposable?.isDisposed == false) return

        actionDisposable = firstScanObs.subscribeBy(
            onNext = {
                Log.i(TAG, "Got first scan (id: ${it.id})")
                scanSubject.onNext(it)
            },
            onError = {
                Log.e(TAG, "Could not get scan", it)
                Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
                finish()
            }
        )
    }

    override fun onPause() {
        super.onPause()
        disposables.forEach { it.dispose() }
        actionDisposable?.dispose()
    }

    private fun initViews() {
        contentGroup = requireView().findViewById(R.id.contentGroup)
        nameTextView = requireView().findViewById(R.id.scanName)
        editNameBtn = requireView().findViewById(R.id.editScanName)
        imageList = requireView().findViewById(R.id.scanImageList)

        val callback = ScanImageMoveCallback(imageListAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(imageList)
        imageList.adapter = imageListAdapter
        imageList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        imageList.setHasFixedSize(true)
        imageList.setItemViewCacheSize(20)

        nameTextView.setOnClickListener {
            val action = ScanFragmentDirections.actionScanFragmentToCameraFragment()
            findNavController().navigate(action)
        }

        editNameBtn.setOnClickListener {
            Log.i(TAG, "Clicked edit name button")
            showUpdateNameDlg()
        }
    }

    private fun createScan(): Observable<Scan> {
        val scanName = scanNameGenerator.generate()
        val scan = Scan(name = scanName)

        return repo.addScan(scan)
    }

    private fun updateUI(scan: Scan) {
        contentGroup.visibility = View.VISIBLE

        nameTextView.text = scan.name
        imageListAdapter.updateItems(scan.images)
    }

    private fun updateScanName(name: String) {
        if (actionDisposable?.isDisposed == false) return

        val newScan = scan!!.copy(name = name)
        actionDisposable = repo.updateScan(newScan).subscribeBy(
            onNext = {
                Log.i(TAG, "Updated scan name successfully (id: ${it.id})")
                scanSubject.onNext(it)
            },
            onError = {
                Log.e(TAG, "Could not set scan name", it)
                Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
            }
        )
    }

    private fun showUpdateNameDlg() {
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
                    updateScanName(scanName)
                    dlg.dismiss()
                }
            }
        }
    }

    // TODO(): Find better validation method.
    /**
     * Validate a scan name.
     *
     * @return Null if name is valid and a error text otherwise.
     */
    private fun validateScanName(name: String): String? {
        return if (name.isEmpty()) resources.getString(R.string.name_to_short_err) else null
    }

    private fun addScanImage(bitmaps: ScanBitmaps) {
        Log.d(TAG, "Add scan image")

        if (scan == null) return

        val images = scan!!.images.toMutableList().apply {
            val id = if (scan!!.images.isEmpty()) 1
            else scan!!.images.maxOf { it.id } + 1

            val scanImage = ScanImage(id, bitmaps.source, bitmaps.crop, bitmaps.improved)
            add(scanImage)
        }

        val updatedScan = scan!!.copy(images = images)

        repo.updateScan(updatedScan).subscribeBy(
            onNext = {
                Log.i(TAG, "Updated scan")
                scanSubject.onNext(it)
            },
            onError = {
                Log.e(TAG, "Could not update scan", it)
                Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
            }
        )
    }

    private fun updateScanImages(scanImages: List<ScanImage>) {
        Log.d(TAG, "Update scan images")

        if (scan == null) return

        val updatedScan = scan!!.copy(images = scanImages)

        repo.updateScan(updatedScan).subscribeBy(
            onNext = {
                Log.i(TAG, "Updated scan images")
                scanSubject.onNext(it)
            },
            onError = {
                Log.e(TAG, "Could not update scan", it)
                Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
            }
        )
    }

    companion object {
        const val TAG = "ScanFragment"
    }
}
