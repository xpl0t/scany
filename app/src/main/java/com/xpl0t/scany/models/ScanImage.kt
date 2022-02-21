package com.xpl0t.scany.models

import android.graphics.Bitmap

data class ScanImage(
    val id: Int = 0,
    val raw: Bitmap,
    val improved: Bitmap
)