package com.xpl0t.scany.extensions

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
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
