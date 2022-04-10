package com.xpl0t.scany.extensions

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.CvType.CV_32F
import org.opencv.core.CvType.CV_8U
import org.opencv.imgproc.Imgproc
import java.lang.Math.pow
import kotlin.math.pow
import kotlin.math.sqrt

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
 * Find the largest quadrangle contour and return the contour.
 * If no quadrangle contour was found null is returned.
 */
fun Mat.getLargestQuadrangle(): MatOfPoint2f? {
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
            approx.reshape(4, 2)

            return approx
        }
    }

    return null // No quadrangle found
}

fun Mat.perspectiveTransform(quadrangleEdges: List<Point>): Mat {
    val (tl, tr, br, bl) = quadrangleEdges.toQuadrangleMatrix()

    val widthA = sqrt((br.x - bl.x).pow(2) + (br.y - bl.y).pow(2))
    val widthB = sqrt((tr.x - tl.x).pow(2) + (tr.y - tl.y).pow(2))
    val width = if (widthA > widthB) widthA else widthB

    val heightA = sqrt((tl.x - bl.x).pow(2) + (tl.y - bl.y).pow(2))
    val heightB = sqrt((tr.x - br.x).pow(2) + (tr.y - br.y).pow(2))
    val height = if (heightA > heightB) heightA else heightB

    val srcQuadrangle = MatOfPoint2f(tl, tr, br, bl)
    val dstQuadrangle = MatOfPoint2f(
        Point(0.0, 0.0), Point(width - 1.0, 0.0),
        Point(width - 1.0, height - 1.0), Point(0.0, height - 1.0)
    )
    val matrix = Imgproc.getPerspectiveTransform(srcQuadrangle, dstQuadrangle)
    val warpedMat = Mat(width.toInt(), height.toInt(), CV_8U)
    Imgproc.warpPerspective(this, warpedMat, matrix, Size(width, height))

    return warpedMat
}