package com.gooey.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.widget.ImageButton
import com.gooey.ui.R

/**
 *@author lishihui01
 *@Date 2023/10/13
 *@Describe:
 */
class RippleImageButton : ImageButton {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        setBackgroundResource(R.drawable.default_ripple)
    }
}