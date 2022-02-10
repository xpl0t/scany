package com.xpl0t.scany.ui.crop

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

class CropFragment : BaseFragment(R.layout.crop_fragment) {

    private lateinit var bitmapPreview: ImageView
    private lateinit var applyCropBtn: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        val bitmap = arguments?.getParcelable<Bitmap>(CameraFragment.SOURCE_BITMAP)
        if (bitmap == null) {
            Log.e(TAG, "No source bitmap supplied")
            Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
            finish()
            return
        }

        setSourceBitmap(bitmap)
    }

    private fun initViews() {
        bitmapPreview = requireView().findViewById(R.id.bitmapPreview)
        applyCropBtn = requireView().findViewById(R.id.applyCrop)

        applyCropBtn.setOnClickListener {
            Log.d(TAG, "Apply crop btn clicked")
        }
    }

    private fun setSourceBitmap(bitmap: Bitmap) {
        bitmapPreview.setImageBitmap(bitmap)
    }

    companion object {
        const val TAG = "CropFragment"
        const val CROP_BITMAP = "CROP_BITMAP"
    }
}