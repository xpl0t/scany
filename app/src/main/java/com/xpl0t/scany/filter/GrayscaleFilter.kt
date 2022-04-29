package com.xpl0t.scany.filter

import com.xpl0t.scany.R
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY
import javax.inject.Inject

class GrayscaleFilter @Inject constructor() : Filter {

    override val id: String
        get() = "grayscale"

    override val displayNameId: Int
        get() = R.string.grayscale_filter

    override fun apply(src: Mat): Mat {
        val filtered = Mat()
        Imgproc.cvtColor(src, filtered, COLOR_BGR2GRAY)

        return filtered
    }

}