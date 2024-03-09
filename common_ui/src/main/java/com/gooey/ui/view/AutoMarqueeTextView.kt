package com.gooey.ui.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView

/**
 *@author lishihui01
 *@Date 2023/10/13
 *@Describe: 自动跑马灯
 */
class AutoMarqueeTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {
    private var mAggregatedVisible = false

    override fun onFinishInflate() {
        super.onFinishInflate()
        onVisibilityAggregated(true)
        setSingleLine()
        marqueeRepeatLimit = -1
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isSelected = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isSelected = false
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (isVisible == mAggregatedVisible) return
        mAggregatedVisible = isVisible
        ellipsize = if (mAggregatedVisible) {
            TextUtils.TruncateAt.MARQUEE
        } else {
            TextUtils.TruncateAt.END
        }
    }
}