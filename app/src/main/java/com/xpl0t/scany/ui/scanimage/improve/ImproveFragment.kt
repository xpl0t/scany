package com.xpl0t.scany.ui.scanimage.improve

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.finish
import com.xpl0t.scany.extensions.toBitmap
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.scanimage.ScanBitmaps
import com.xpl0t.scany.ui.scanimage.camera.CameraService
import dagger.hilt.android.AndroidEntryPoint
import org.opencv.core.Mat
import javax.inject.Inject

@AndroidEntryPoint
class ImproveFragment : BaseFragment(R.layout.improve_fragment) {

    @Inject() lateinit var cameraService: CameraService
    @Inject() lateinit var improveService: ImproveService

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

    private fun initViews() {
        bitmapPreview = requireView().findViewById(R.id.bitmapPreview)
        applyCropBtn = requireView().findViewById(R.id.applyImprove)

        applyCropBtn.setOnClickListener {
            Log.d(TAG, "Apply improve btn clicked")
            finishWithImprovedImage(bitmap)
        }
    }

    private fun setDocPreview(bitmap: Bitmap) {
        bitmapPreview.setImageBitmap(bitmap)
    }

    private fun finishWithImprovedImage(improved: Bitmap) {
        improveService.documentSubject.onNext(improved)
        findNavController().popBackStack(R.id.scanFragment, false)
    }

    companion object {
        const val TAG = "ImproveFragment"
        const val SCAN_BITMAPS = "SCAN_BITMAPS"
    }
}