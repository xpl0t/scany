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
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.scanimage.ScanBitmaps

class ImproveFragment : BaseFragment(R.layout.improve_fragment) {

    private val args: ImproveFragmentArgs by navArgs()

    private lateinit var bitmapPreview: ImageView
    private lateinit var applyCropBtn: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        setCropBitmap(args.sourceImg)
    }

    private fun initViews() {
        bitmapPreview = requireView().findViewById(R.id.bitmapPreview)
        applyCropBtn = requireView().findViewById(R.id.applyImprove)

        applyCropBtn.setOnClickListener {
            Log.d(TAG, "Apply improve btn clicked")
            finishWithImprovedImage(args.sourceImg)
        }
    }

    private fun setCropBitmap(bitmap: Bitmap) {
        bitmapPreview.setImageBitmap(bitmap)
    }

    private fun finishWithImprovedImage(improved: Bitmap) {
        findNavController().getBackStackEntry(R.id.scanFragment).savedStateHandle.apply {
            set(SCAN_BITMAPS, ScanBitmaps(args.sourceImg, improved))
        }
        findNavController().popBackStack(R.id.scanFragment, false)
    }

    companion object {
        const val TAG = "ImproveFragment"
        const val SCAN_BITMAPS = "SCAN_BITMAPS"
    }
}