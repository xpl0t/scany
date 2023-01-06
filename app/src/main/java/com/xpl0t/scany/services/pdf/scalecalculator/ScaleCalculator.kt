package com.xpl0t.scany.services.pdf.scalecalculator

import android.graphics.Point
import android.graphics.Rect
import com.xpl0t.scany.services.pdf.ScaleType

interface ScaleCalculator {

    /**
     * Scale type that is supported by the calculator.
     */
    val scaleType: ScaleType

    fun getSize(frame: Rect, image: Rect): Rect

    fun getOffset(frame: Rect, image: Rect): Point

}