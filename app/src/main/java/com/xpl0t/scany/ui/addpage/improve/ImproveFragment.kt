package com.xpl0t.scany.ui.addpage.improve

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.finish
import com.xpl0t.scany.extensions.runOnUiThread
import com.xpl0t.scany.extensions.toJpg
import com.xpl0t.scany.filter.Filter
import com.xpl0t.scany.filter.FilterList
import com.xpl0t.scany.models.Page
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.ui.addpage.camera.CameraService
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.scan.ScanFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.opencv.core.Mat
import javax.inject.Inject

@AndroidEntryPoint
class ImproveFragment : BaseFragment(R.layout.improve_fragment) {

    private val args: ImproveFragmentArgs by navArgs()

    @Inject()
    lateinit var repo: Repository

    @Inject()
    lateinit var cameraService: CameraService

    @Inject() lateinit var filters: FilterList

    private var actionDisposable: Disposable? = null
    private var filterDisposable: Disposable? = null

    private lateinit var originalMat: Mat
    private var filteredMat: Mat? = null
    private var curFilter: Filter? = null

    private lateinit var bitmapPreview: PhotoView
    private lateinit var filterList: RecyclerView
    private lateinit var filterAdapter: FilterItemAdapter
    private lateinit var applyBtn: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val curFilterId = savedInstanceState?.getString(CUR_FILTER)
        curFilter = filters.find { it.id == curFilterId }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (cameraService.page == null) {
            Log.e(TAG, "Document null")
            finish()
            return
        }

        originalMat = cameraService.page!!
        filterAdapter = FilterItemAdapter(requireContext(), originalMat, filters)

        initViews()

        if (curFilter != null) {
            filteredMat = curFilter!!.apply(originalMat)
            filterAdapter.setCurrentFilter(curFilter)
            setDocPreview(filteredMat!!)
        } else {
            setDocPreview(originalMat)
        }
    }

    override fun onResume() {
        super.onResume()

        filterDisposable = filterAdapter.filterSelected.subscribe {
            curFilter = if (it.isEmpty) null else it.value

            if (it.isEmpty) {
                filteredMat = null
                setDocPreview(originalMat)
                return@subscribe
            }

            filteredMat = it.value.apply(originalMat)
            setDocPreview(filteredMat!!)
        }
    }

    override fun onPause() {
        super.onPause()
        actionDisposable?.dispose()
        filterDisposable?.dispose()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CUR_FILTER, curFilter?.id ?: return)
    }

    private fun initViews() {
        bitmapPreview = requireView().findViewById(R.id.bitmapPreview)
        filterList = requireView().findViewById(R.id.filterList)
        applyBtn = requireView().findViewById(R.id.applyImprove)

        filterList.adapter = filterAdapter

        applyBtn.setOnClickListener {
            Log.d(TAG, "Apply improve btn clicked")
            addPage(filteredMat ?: originalMat)
        }
    }

    private fun setDocPreview(mat: Mat) {
        Glide.with(requireView())
            .load(mat)
            .into(bitmapPreview)
    }

    private fun addPage(mat: Mat) {
        Log.d(ScanFragment.TAG, "Add scan image")

        if (actionDisposable?.isDisposed == false) return

        val jpg = mat.toJpg(95)
        val page = Page(image = jpg, next = null)

        actionDisposable = repo.addPage(args.scanId, page).subscribeBy(
            onNext = {
                Log.i(ScanFragment.TAG, "Added scan image")
                runOnUiThread {
                    returnToScanListFragment()
                }
            },
            onError = {
                Log.e(ScanFragment.TAG, "Could not add scan image", it)
                Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
            }
        )
    }

    private fun returnToScanListFragment() {
        findNavController().popBackStack(R.id.scanListFragment, false)
    }

    companion object {
        const val TAG = "ImproveFragment"
        private const val CUR_FILTER = "CUR_FILTER"
    }
}