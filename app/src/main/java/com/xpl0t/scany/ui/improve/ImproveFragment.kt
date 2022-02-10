package com.xpl0t.scany.ui.improve

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.finish
import com.xpl0t.scany.ui.camera.CameraFragment
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.crop.CropFragment

class ImproveFragment : BaseFragment(R.layout.improve_fragment) {

    private lateinit var bitmapPreview: ImageView
    private lateinit var applyCropBtn: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        val sourceBitmap = arguments?.getParcelable<Bitmap>(CameraFragment.SOURCE_BITMAP)
        val cropBitmap = arguments?.getParcelable<Bitmap>(CropFragment.CROP_BITMAP)
        if (sourceBitmap == null || cropBitmap == null) {
            Log.e(TAG, "No source/crop bitmap supplied")
            Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
            finish()
            return
        }

        setCropBitmap(cropBitmap)
    }

    private fun initViews() {
        bitmapPreview = requireView().findViewById(R.id.bitmapPreview)
        applyCropBtn = requireView().findViewById(R.id.applyImprove)

        applyCropBtn.setOnClickListener {
            Log.d(TAG, "Apply improve btn clicked")
        }
    }

    private fun setCropBitmap(bitmap: Bitmap) {
        bitmapPreview.setImageBitmap(bitmap)
    }

    private fun finishWithImprovedImage(improved: Bitmap) {
        finish()
    }

    companion object {
        const val TAG = "ImproveFragment"
        const val IMPROVED_BITMAP = "IMPROVED_BITMAP"
    }
}