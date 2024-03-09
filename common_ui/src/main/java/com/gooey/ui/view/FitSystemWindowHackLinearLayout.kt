package com.gooey.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.LinearLayout

/**
 *@author lishihui01
 *@Date 2023/9/21
 *@Describe:
 */
class FitSystemWindowHackLinearLayout : LinearLayout {
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?)
            : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int)
            : this(context, attrs, defStyle, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes) {
        fitsSystemWindows = true
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        return super.onApplyWindowInsets(insets.replaceSystemWindowInsets(
                0, 0, 0, insets.systemWindowInsetBottom))
    }
}