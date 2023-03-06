package com.xpl0t.scany.ui.addpage.improve

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.*
import com.xpl0t.scany.filter.Filter
import com.xpl0t.scany.filter.FilterList
import com.xpl0t.scany.models.Page
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.ui.addpage.camera.CameraService
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.document.DocumentFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import org.opencv.core.Mat
import javax.inject.Inject

@AndroidEntryPoint
class ImproveFragment : BaseFragment(R.layout.improve_fragment), DialogInterface.OnMultiChoiceClickListener {

    private val args: ImproveFragmentArgs by navArgs()

    @Inject()
    lateinit var repo: Repository

    @Inject()
    lateinit var cameraService: CameraService

    @Inject() lateinit var filters: FilterList

    private var actionDisposable: Disposable? = null

    private var filterDialog: AlertDialog? = null

    private lateinit var originalMat: Mat
    private var transformedMat: Mat? = null
    private var transformedAndFilteredMat: Mat? = null

    private val filtersApplied = mutableListOf<Filter>()

    private lateinit var bitmapPreview: PhotoView
    private lateinit var rotateLeftBtn: FloatingActionButton
    private lateinit var rotateRightBtn: FloatingActionButton
    private lateinit var flipVerticalBtn: FloatingActionButton
    private lateinit var flipHorizontalBtn: FloatingActionButton
    private lateinit var chooseFiltersBtn: FloatingActionButton
    private lateinit var applyBtn: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onResume() {
        super.onResume()

        if (cameraService.page == null) {
            Log.e(TAG, "Document null")
            finish()
            return
        }

        originalMat = cameraService.page!!
        transformedMat = cameraService.page!!.clone()
        transformedAndFilteredMat = cameraService.page!!.clone()

        setDocPreview(originalMat)
    }

    override fun onPause() {
        super.onPause()
        actionDisposable?.dispose()
    }

    private fun initViews() {
        bitmapPreview = requireView().findViewById(R.id.page_preview)
        rotateLeftBtn = requireView().findViewById(R.id.rotate_left)
        rotateLeftBtn.setOnClickListener {
            transformedMat = transformedMat?.rotate(90)
            transformedAndFilteredMat = transformedAndFilteredMat?.rotate(90)

            setDocPreview(transformedAndFilteredMat!!)
        }

        rotateRightBtn = requireView().findViewById(R.id.rotate_right)
        rotateRightBtn.setOnClickListener {
            transformedMat = transformedMat?.rotate(270)
            transformedAndFilteredMat = transformedAndFilteredMat?.rotate(270)

            setDocPreview(transformedAndFilteredMat!!)
        }

        flipHorizontalBtn = requireView().findViewById(R.id.flip_horizontal)
        flipHorizontalBtn.setOnClickListener {
            transformedMat = transformedMat?.flip(1)
            transformedAndFilteredMat = transformedAndFilteredMat?.flip(1)

            setDocPreview(transformedAndFilteredMat!!)
        }

        flipVerticalBtn = requireView().findViewById(R.id.flip_vertical)
        flipVerticalBtn.setOnClickListener {
            transformedMat = transformedMat?.flip(0)
            transformedAndFilteredMat = transformedAndFilteredMat?.flip(0)

            setDocPreview(transformedAndFilteredMat!!)
        }

        chooseFiltersBtn = requireView().findViewById(R.id.choose_filters)
        chooseFiltersBtn.setOnClickListener {
            showFilterPickerDialog()
        }

        applyBtn = requireView().findViewById(R.id.applyImprove)
        applyBtn.setOnClickListener {
            Log.d(TAG, "Apply improve btn clicked")
            addPage(transformedAndFilteredMat ?: originalMat)
        }
    }

    private fun applyFilters(mat: Mat): Mat {
        var filteredMat = mat.clone()

        for (filter in filtersApplied)
            filteredMat = filter.apply(filteredMat)

        return filteredMat
    }

    private fun showFilterPickerDialog() {
        val filterNames = filters.map { resources.getString(it.displayNameId) }
        val filtersActive = filters.map { filtersApplied.contains(it) }

        filterDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.choose_filters)
            .setMultiChoiceItems(filterNames.toTypedArray(), filtersActive.toBooleanArray(), this)
            .setPositiveButton(R.string.done_btn) { _, _ -> }
            .setCancelable(false)
            .show()
    }

    override fun onClick(dialog: DialogInterface?, which: Int, isChecked: Boolean) {
        val adapter = filterDialog!!.listView.adapter
        val headerView = ProgressBar(requireContext())
        filterDialog!!.listView.addHeaderView(headerView)
        filterDialog!!.listView.adapter = null
        filterDialog!!.getButton(DialogInterface.BUTTON_POSITIVE).visibility = View.GONE

        val hideProgressBar = {
            runOnUiThread {
                filterDialog!!.listView.removeHeaderView(headerView)
                filterDialog!!.listView.adapter = adapter
                filterDialog!!.getButton(DialogInterface.BUTTON_POSITIVE).visibility = View.VISIBLE
            }
        }

        Schedulers.computation().scheduleDirect {
            try {
                val filter = filters[which]

                if (filtersApplied.contains(filter)) {
                    filtersApplied.remove(filter)
                    transformedAndFilteredMat = applyFilters(transformedMat!!)
                    runOnUiThread { setDocPreview(transformedAndFilteredMat!!) }

                    return@scheduleDirect
                }

                transformedAndFilteredMat = filter.apply(transformedAndFilteredMat!!)
                runOnUiThread { setDocPreview(transformedAndFilteredMat!!) }
                filtersApplied.add(filter)
            } catch (e: Error) {
                Log.e(TAG, "Apply filters failed", e)
            } finally {
                hideProgressBar()
            }
        }
    }

    private fun setDocPreview(mat: Mat) {
        Glide.with(requireView())
            .load(mat)
            .skipMemoryCache(true)
            .into(bitmapPreview)
    }

    private fun addPage(mat: Mat) {
        Log.d(DocumentFragment.TAG, "Add document image")

        if (actionDisposable?.isDisposed == false) return

        val imageBytes = mat.toPng()
        val page = Page(image = imageBytes, order = 0)

        actionDisposable = repo.addPage(args.documentId, page).subscribeBy(
            onNext = {
                Log.i(DocumentFragment.TAG, "Added document image")
                runOnUiThread {
                    returnToDocumentListFragment()
                }
            },
            onError = {
                Log.e(DocumentFragment.TAG, "Could not add document image", it)
                Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
            }
        )
    }

    private fun returnToDocumentListFragment() {
        findNavController().popBackStack(R.id.documentListFragment, false)
    }

    companion object {
        const val TAG = "ImproveFragment"
    }
}