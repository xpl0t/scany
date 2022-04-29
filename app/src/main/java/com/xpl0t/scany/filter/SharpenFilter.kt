package com.xpl0t.scany.filter

import com.xpl0t.scany.R
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import javax.inject.Inject


class SharpenFilter @Inject constructor() : Filter {

    override val id: String
        get() = "sharpen"

    override val displayNameId: Int
        get() = R.string.sharpen_filter

    override fun apply(src: Mat): Mat {
        val k = Mat(3, 3, CvType.CV_32F)
        k.put(0, 0, -1.0, -1.0, -1.0, -1.0, 9.0, -1.0, -1.0, -1.0, -1.0)

        val filtered = Mat()
        Imgproc.filter2D(src, filtered, -1, k)

        return filtered
    }

}