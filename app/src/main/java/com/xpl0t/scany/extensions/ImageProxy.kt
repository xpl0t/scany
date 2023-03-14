package com.xpl0t.scany.extensions

import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import org.opencv.core.Core.ROTATE_90_CLOCKWISE
import org.opencv.core.Core.rotate
import org.opencv.core.CvType.CV_8UC1
import org.opencv.core.CvType.CV_8UC3
import org.opencv.core.Mat
import org.opencv.core.Range
import java.nio.ByteBuffer


fun ImageProxy.toGrayscaleMat(): Mat {
    assert(format == ImageFormat.YUV_420_888)
    assert(planes[0].rowStride >= width)

    return Mat( // row stride might be bigger than width. Overlapping pixels have to be cut.
        Mat(height, planes[0].rowStride, CV_8UC1, planes[0].buffer),
        Range(0, height),
        Range(0, width)
    )
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
        planes[0].rowStride,
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
    yRowStride: Int,
    uvRowStride: Int,
    uvPixelStride: Int,
    y: ByteBuffer,
    u: ByteBuffer,
    v: ByteBuffer,
    rgb: ByteBuffer
)
