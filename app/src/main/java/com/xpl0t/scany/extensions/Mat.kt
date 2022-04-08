package com.xpl0t.scany.extensions

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.CvType.CV_32F
import org.opencv.imgproc.Imgproc

fun Mat.toBitmap(): Bitmap {
    val bmp = Bitmap.createBitmap(cols(), rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(this, bmp)

    return bmp
}

fun Mat.scale(scaledHeight: Double) {
    val scaledWidth = size().width * scaledHeight / size().height
    val scaledSize = Size(scaledWidth, scaledHeight)
    Imgproc.resize(this, this, scaledSize)
}

fun Mat.grayscale() {
    Imgproc.cvtColor(this, this, Imgproc.COLOR_BGR2GRAY)
}

fun Mat.blur() {
    Imgproc.GaussianBlur(this, this, Size(5.0, 5.0), 0.0)
}

fun Mat.canny() {
    Imgproc.Canny(this, this, 75.0, 200.0)
}

/**
 * Find the largest quadrilateral contour and return the contour.
 * If no quadrilateral contour was found null is returned.
 */
fun Mat.getLargestQuadrilateral(): MatOfPoint2f? {
    val mat = this.clone()

    val contours = mutableListOf<MatOfPoint>()
    Imgproc.findContours(mat, contours, mat, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

    val sortedContours = contours
        .sortedByDescending { Imgproc.contourArea(it) }
        .take(5)

    for (con in sortedContours) {
        val mat = MatOfPoint2f()
        con.convertTo(mat, CV_32F)

        val peri = Imgproc.arcLength(mat, true)
        val approx = MatOfPoint2f()
        Imgproc.approxPolyDP(mat, approx, 0.02 * peri, true)

        if (approx.total() == 4.toLong()) {
            return approx
        }
    }

    return null // No quadrilateral found
}
