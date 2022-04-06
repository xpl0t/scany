package com.xpl0t.scany.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.xpl0t.scany.R

class FailedCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val title by lazy { findViewById<TextView>(R.id.failedTitle) }
    private val subtitle by lazy { findViewById<TextView>(R.id.failedSubtitle) }
    private val retry by lazy { findViewById<Button>(R.id.failedRetry) }

    init {
        View.inflate(context, R.layout.failed_card, this)
        val a = context.obtainStyledAttributes(attrs, R.styleable.FailedCard, 0, 0)
        val titleText = a.getText(R.styleable.FailedCard_title)
        val subtitleText = a.getText(R.styleable.FailedCard_subtitle)
        if (titleText != null)
            title.text = titleText
        subtitle.visibility = if (subtitleText == null) View.GONE else View.VISIBLE
        if (subtitleText != null)
            subtitle.text = subtitleText
        a.recycle()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        retry.setOnClickListener(l)
    }

}