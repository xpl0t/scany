package com.xpl0t.scany.services.pdf.scalecalculator

import android.graphics.Point
import android.graphics.Rect
import com.xpl0t.scany.services.pdf.ScaleType
import javax.inject.Inject

class FitScaleCalculator @Inject() constructor() : ScaleCalculator {

    override val scaleType = ScaleType.Fit

    override fun getSize(frame: Rect, image: Rect): Rect {
        return frame
    }

    override fun getOffset(frame: Rect, image: Rect): Point {
        return Point(0, 0)
    }

}