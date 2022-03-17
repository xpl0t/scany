package com.xpl0t.scany.ui.scanimage.improve

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
import com.xpl0t.scany.models.ScanImage
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.scan.ScanFragment
import com.xpl0t.scany.ui.scanimage.camera.CameraService
import com.xpl0t.scany.util.Optional
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

        if (cameraService.document == null) {
            Log.e(TAG, "Document null")
            finish()
            return
        }

        mat = cameraService.document!!
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
            addScanImage(bitmap)
        }
    }

    private fun setDocPreview(bitmap: Bitmap) {
        bitmapPreview.setImageBitmap(bitmap)
    }

    private fun addScanImage(bitmap: Bitmap) {
        Log.d(ScanFragment.TAG, "Add scan image")

        if (actionDisposable?.isDisposed == false) return

        val scanImg = ScanImage(image = bitmap)

        actionDisposable = repo.addScanImg(args.scanId, scanImg).subscribeBy(
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