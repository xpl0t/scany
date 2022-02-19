package com.xpl0t.scany.extensions

import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import org.opencv.core.Core.ROTATE_90_CLOCKWISE
import org.opencv.core.Core.rotate
import org.opencv.core.CvType.*
import org.opencv.core.Mat
import java.nio.ByteBuffer


fun ImageProxy.toMat(): Mat {
    if (format != ImageFormat.YUV_420_888) {
        throw Error("Unsupported image format")
    }

    val rgbBuf = ByteBuffer.allocateDirect(width * height * 3)
    val yPlane = planes[0].buffer
    val uPlane = planes[1].buffer
    val vPlane = planes[2].buffer

    yuvToRgb(width, height, yPlane, uPlane, vPlane, rgbBuf)

    val mat = Mat(height, width, CV_8UC3, rgbBuf)

    if (imageInfo.rotationDegrees == 90) {
        rotate(mat, mat, ROTATE_90_CLOCKWISE)
    }

    return mat
}

private external fun yuvToRgb(
    width: Int, height: Int, y: ByteBuffer, u: ByteBuffer, v: ByteBuffer, rgb: ByteBuffer)
