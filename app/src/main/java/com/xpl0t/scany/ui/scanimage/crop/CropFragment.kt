package com.xpl0t.scany.ui.scanimage.crop

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.xpl0t.scany.R
import com.xpl0t.scany.ui.common.BaseFragment

class CropFragment : BaseFragment(R.layout.crop_fragment) {

    private val args: CropFragmentArgs by navArgs()

    private lateinit var bitmapPreview: ImageView
    private lateinit var applyCropBtn: FloatingActionButton

    private lateinit var sourceBitmap: Bitmap

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        setSourceBitmap(args.sourceImg)
    }

    private fun initViews() {
        bitmapPreview = requireView().findViewById(R.id.bitmapPreview)
        applyCropBtn = requireView().findViewById(R.id.applyCrop)

        applyCropBtn.setOnClickListener {
            Log.d(TAG, "Apply crop btn clicked")
            showImproveFragment(sourceBitmap)
        }
    }

    private fun setSourceBitmap(bitmap: Bitmap) {
        sourceBitmap = bitmap
        bitmapPreview.setImageBitmap(bitmap)
    }

    private fun showImproveFragment(bitmap: Bitmap) {
        val action = CropFragmentDirections.actionCropFragmentToImproveFragment(sourceBitmap, bitmap)
        findNavController().navigate(action)
    }

    companion object {
        const val TAG = "CropFragment"
    }
}