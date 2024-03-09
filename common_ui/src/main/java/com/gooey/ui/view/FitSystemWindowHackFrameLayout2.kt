package com.gooey.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.FrameLayout

/**
 *@author lishihui01
 *@Date 2023/9/21
 *@Describe:
 */
class FitSystemWindowHackFrameLayout2 : FrameLayout {
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

    override fun addView(child: View?) {
        super.addView(child)
    }

    override fun addView(child: View?, index: Int) {
        super.addView(child, index)
    }

    override fun addView(child: View?, width: Int, height: Int) {
        super.addView(child, width, height)
    }

    override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
        super.addView(child, params)
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
    }
}