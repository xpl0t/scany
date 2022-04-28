package com.xpl0t.scany.filter

import com.xpl0t.scany.R
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY
import javax.inject.Inject

class NoFilter @Inject constructor() : Filter {

    override val id: String
        get() = "no-filter"

    override val displayNameId: Int
        get() = R.string.no_filter

    override fun apply(src: Mat): Mat {
        return src
    }

}