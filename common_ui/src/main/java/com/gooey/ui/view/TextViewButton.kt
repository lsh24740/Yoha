package com.gooey.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.gooey.ui.R

/**
 *@author lishihui01
 *@Date 2023/10/17
 *@Describe:
 */
class TextViewButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {
    init {
        isClickable = true
        setBackgroundResource(R.drawable.default_ripple_background_normal)
    }
}