package com.xpl0t.scany.ui.page.improve

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.finish
import com.xpl0t.scany.extensions.toBitmap
import com.xpl0t.scany.models.Page
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.scan.ScanFragment
import com.xpl0t.scany.ui.page.camera.CameraService
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.opencv.core.Mat
import javax.inject.Inject

@AndroidEntryPoint
class ImproveFragment : BaseFragment(R.layout.improve_fragment) {

    private val args: ImproveFragmentArgs by navArgs()

    @Inject() lateinit var repo: Repository
    @Inject() lateinit var cameraService: CameraService

    private var actionDisposable: Disposable? = null

    private lateinit var mat: Mat
    private lateinit var bitmap: Bitmap

    private lateinit var bitmapPreview: ImageView
    private lateinit var applyCropBtn: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        if (cameraService.page == null) {
            Log.e(TAG, "Document null")
            finish()
            return
        }

        mat = cameraService.page!!
        bitmap = mat.toBitmap()
        setDocPreview(bitmap)
    }

    override fun onPause() {
        super.onPause()
        actionDisposable?.dispose()
    }

    private fun initViews() {
        bitmapPreview = requireView().findViewById(R.id.bitmapPreview)
        applyCropBtn = requireView().findViewById(R.id.applyImprove)

        applyCropBtn.setOnClickListener {
            Log.d(TAG, "Apply improve btn clicked")
            addPage(bitmap)
        }
    }

    private fun setDocPreview(bitmap: Bitmap) {
        bitmapPreview.setImageBitmap(bitmap)
    }

    private fun addPage(bitmap: Bitmap) {
        Log.d(ScanFragment.TAG, "Add scan image")

        if (actionDisposable?.isDisposed == false) return

        val page = Page(image = bitmap)

        actionDisposable = repo.addPage(args.scanId, page).subscribeBy(
            onNext = {
                Log.i(ScanFragment.TAG, "Added scan image")
                returnToScanListFragment()
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
    }
}