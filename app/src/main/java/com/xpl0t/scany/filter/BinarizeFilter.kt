package com.xpl0t.scany.filter

import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.blur
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY
import org.opencv.photo.Photo
import javax.inject.Inject

class BinarizeFilter @Inject constructor() : Filter {

    override val id: String
        get() = "binarize"

    override val displayNameId: Int
        get() = R.string.binarize_filter

    override fun apply(src: Mat): Mat {
        if (src.channels() > 1)
            Imgproc.cvtColor(src, src, COLOR_BGR2GRAY)

        Photo.fastNlMeansDenoising(src, src, 10f, 7, 21)
        Imgproc.adaptiveThreshold(src, src, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 3.0)

        return src
    }

}