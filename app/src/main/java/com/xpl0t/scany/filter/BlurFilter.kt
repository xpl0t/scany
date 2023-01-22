package com.xpl0t.scany.filter

import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.blur
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY
import javax.inject.Inject

class BlurFilter @Inject constructor() : Filter {

    override val id: String
        get() = "blur"

    override val displayNameId: Int
        get() = R.string.blur_filter

    override fun apply(src: Mat): Mat {
        Imgproc.GaussianBlur(src, src, Size(5.0, 5.0), 0.0)
        return src
    }

}