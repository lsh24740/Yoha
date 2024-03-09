package com.gooey.ui.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.TextView

/**
 *@author lishihui01
 *@Date 2023/10/13
 *@Describe:
 */
class DrawableRightTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    override fun onDraw(canvas: Canvas) {
        val drawables = compoundDrawables
        val drawablesRelative = compoundDrawablesRelative
        val drawableLeft = drawables[0] ?: drawablesRelative[0]
        if (drawableLeft != null) {
            val textWidth = paint.measureText(text.toString())
            val drawablePadding = compoundDrawablePadding
            val drawableWidth = drawableLeft.intrinsicWidth
            val bodyWidth =
                    drawableWidth + drawablePadding + textWidth + paddingRight
            Log.d("DrawableRightTextView", "bodyWidth:$bodyWidth  width:$width  paddingRight:$paddingRight   paddingEnd:$paddingEnd")
            if (width > bodyWidth) {
                canvas.translate(width - bodyWidth, 0f)
            }
        }
        super.onDraw(canvas)
    }

}