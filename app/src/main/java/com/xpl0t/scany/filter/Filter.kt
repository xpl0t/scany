package com.xpl0t.scany.filter

import org.opencv.core.Mat

interface Filter {
    /**
     * Unique filter id.
     */
    val id: String

    /**
     * Resource id of the display name of the filter.
     */
    val displayNameId: Int

    /**
     * Applies the filter.
     * @return Mat with the filter applied.
     */
    fun apply(src: Mat): Mat
}