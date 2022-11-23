package com.xpl0t.scany.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.xpl0t.scany.R

class DocumentOutline @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private var polygon: Iterable<Point>? = null

    private val outlineColor: Paint
    private val outlineFillColor: Paint

    private val path = Path()

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DocumentOutline, 0, 0)
        val color = a.getColor(R.styleable.DocumentOutline_outlineColor, Color.WHITE)

        outlineColor = Paint().apply {
            isAntiAlias = true
            this.color = color
            this.strokeWidth = 10f
            style = Paint.Style.STROKE
        }

        outlineFillColor = Paint().apply {
            isAntiAlias = true
            this.color = color
            this.alpha = 100
            style = Paint.Style.FILL
        }

        a.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        polygon ?: return

        canvas.drawPath(path, outlineColor)
        canvas.drawPath(path, outlineFillColor)
    }

    fun setOutline(edges: Iterable<Point>) {
        polygon = edges
        updatePathFromEdges(edges)
        invalidate()
    }

    fun clear() {
        polygon = null
        invalidate()
    }

    private fun updatePathFromEdges(edges: Iterable<Point>) {
        path.reset()

        for (i in 0 until polygon!!.count() + 1) {
            val pointA = polygon!!.elementAt(i % polygon!!.count())

            if (i == 0)
                path.moveTo(pointA.x.toFloat(), pointA.y.toFloat())
            else
                path.lineTo(pointA.x.toFloat(), pointA.y.toFloat())
        }
    }

}