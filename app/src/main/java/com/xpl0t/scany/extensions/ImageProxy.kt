package com.xpl0t.scany.extensions

import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import org.opencv.core.Core.ROTATE_90_CLOCKWISE
import org.opencv.core.Core.rotate
import org.opencv.core.CvType.CV_8UC1
import org.opencv.core.CvType.CV_8UC3
import org.opencv.core.Mat
import java.nio.ByteBuffer


fun ImageProxy.toGrayscaleMat(): Mat {
    assert(format == ImageFormat.YUV_420_888)
    return Mat(height, width, CV_8UC1, planes[0].buffer)
}

fun ImageProxy.toMat(): Mat {
    assert(format == ImageFormat.YUV_420_888)

    val rgbBuf = ByteBuffer.allocateDirect(width * height * 3)
    val yPlane = planes[0].buffer
    val uPlane = planes[1].buffer
    val vPlane = planes[2].buffer

    yuvToRgb(
        width,
        height,
        planes[1].rowStride,
        planes[1].pixelStride,
        yPlane,
        uPlane,
        vPlane,
        rgbBuf
    )

    val mat = Mat(height, width, CV_8UC3, rgbBuf)

    if (imageInfo.rotationDegrees == 90) {
        rotate(mat, mat, ROTATE_90_CLOCKWISE)
    }

    return mat
}

private external fun yuvToRgb(
    width: Int,
    height: Int,
    uvRowStride: Int,
    uvPixelStride: Int,
    y: ByteBuffer,
    u: ByteBuffer,
    v: ByteBuffer,
    rgb: ByteBuffer
)
