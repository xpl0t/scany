package com.xpl0t.scany.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

// Source: https://stackoverflow.com/a/63823500
/**
 *  Convert image proxy to bitmap.
 */
fun ImageProxy.toBitmap(): Bitmap {
    val planeProxy = planes[0]
    val buffer: ByteBuffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
