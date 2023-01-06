package com.xpl0t.scany.services.pdf.scalecalculator

import android.graphics.Point
import android.graphics.Rect
import com.xpl0t.scany.services.pdf.ScaleType
import javax.inject.Inject

class CenterInsideScaleCalculator @Inject() constructor() : ScaleCalculator {

    override val scaleType = ScaleType.CenterInside

    override fun getSize(frame: Rect, image: Rect): Rect {
        val aspectRatioFrame = frame.width().toFloat() / frame.height().toFloat()
        val aspectRatioImg = image.width().toFloat() / image.height().toFloat()
        val scaleByWidth = aspectRatioImg > aspectRatioFrame

        val scaledWidth = if (scaleByWidth) frame.width()
            else (frame.height() * aspectRatioImg).toInt()

        val scaledHeight = if (!scaleByWidth) frame.height().toInt()
            else (frame.width() / aspectRatioImg).toInt()

        return Rect(0, 0, scaledWidth, scaledHeight)
    }

    override fun getOffset(frame: Rect, image: Rect): Point {
        val scaledImg = getSize(frame, image)

        return Point(
            (frame.width() - scaledImg.width()) / 2,
            (frame.height() - scaledImg.height()) / 2
        )
    }

}