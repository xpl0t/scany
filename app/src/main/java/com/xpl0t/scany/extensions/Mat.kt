package com.xpl0t.scany.extensions

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.CvType.CV_32F
import org.opencv.core.CvType.CV_8U
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import kotlin.math.pow
import kotlin.math.sqrt

fun Mat.toBitmap(): Bitmap {
    val bmp = Bitmap.createBitmap(cols(), rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(this, bmp)

    return bmp
}

fun Mat.scale(height: Double, copy: Boolean = false): Mat {
    val scaledWidth = size().width * height / size().height
    val scaledSize = Size(scaledWidth, height)
    val dstMat = if (copy) Mat() else this

    Imgproc.resize(this, dstMat, scaledSize)

    return dstMat
}

fun Mat.scale(width: Double, height: Double, copy: Boolean = false): Mat {
    val scaledSize = Size(width, height)
    val dstMat = if (copy) Mat() else this

    Imgproc.resize(this, dstMat, scaledSize)

    return dstMat
}

fun Mat.grayscale(copy: Boolean = false): Mat {
    val dstMat = if (copy) Mat() else this
    Imgproc.cvtColor(this, dstMat, Imgproc.COLOR_BGR2GRAY)

    return dstMat
}

fun Mat.blur(copy: Boolean = false): Mat {
    val dstMat = if (copy) Mat() else this
    Imgproc.GaussianBlur(this, dstMat, Size(5.0, 5.0), 0.0)

    return dstMat
}

fun Mat.canny(copy: Boolean = false): Mat {
    val dstMat = if (copy) Mat() else this
    Imgproc.Canny(this, dstMat, 75.0, 200.0)

    return dstMat
}

fun Mat.blend(kernel: Mat): Mat {
    val morphed = Mat()
    Imgproc.morphologyEx(this, morphed, Imgproc.MORPH_CLOSE, kernel, Point(-1.0, -1.0), 3)
    return morphed
}

fun Mat.toJpg(quality: Int): ByteArray {
    val correctedMat = Mat()
    Imgproc.cvtColor(this, correctedMat, Imgproc.COLOR_BGR2RGB)

    val params = MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality)
    val buf = MatOfByte()
    Imgcodecs.imencode(".jpg", correctedMat, buf, params)
    return buf.toArray()
}

/**
 * Find the largest quadrangle contour and return the contour.
 * If no quadrangle contour was found null is returned.
 */
fun Mat.getLargestQuadrangle(minDistX: Int, minDistY: Int): MatOfPoint2f? {
    val contours = mutableListOf<MatOfPoint>()
    Imgproc.findContours(this, contours, Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

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

            val points = approx.toList().toMutableList()
            points.sortBy { it.x + it.y }
            val bottomRight = points.removeAt(3)
            val topLeft = points.removeAt(0)

            points.sortBy { it.x + (height() - it.y) }
            val bottomLeft = points[0]
            val topRight = points[1]

            if (
                (topRight.x - topLeft.x) > minDistX
                && (bottomRight.x - bottomLeft.x) > minDistX
                && (bottomLeft.y - topLeft.y) > minDistY
                && (bottomRight.y - topRight.y) > minDistY
            ) {
                return approx
            }
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