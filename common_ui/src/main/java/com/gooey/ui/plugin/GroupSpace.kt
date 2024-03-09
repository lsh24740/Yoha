package com.gooey.ui.plugin

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.text.TextUtils
import android.util.AttributeSet
import android.util.LayoutDirection
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author lishihui
 */

class GroupSpace : View {
    private val children = ArrayList<View>()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?)
            : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int)
            : this(context, attrs, defStyle, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes) {
        if (visibility == VISIBLE) {
            visibility = INVISIBLE
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun draw(canvas: Canvas?) {
        if (background != null) {
            super.draw(canvas)
        }
    }

    private fun getDefaultSize2(size: Int, measureSpec: Int): Int {
        var result = size
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        when (specMode) {
            MeasureSpec.UNSPECIFIED -> result = size
            MeasureSpec.AT_MOST -> result = Math.min(size, specSize)
            MeasureSpec.EXACTLY -> result = specSize
        }
        return result
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            getDefaultSize2(suggestedMinimumWidth, widthMeasureSpec),
            getDefaultSize2(suggestedMinimumHeight, heightMeasureSpec)
        )
    }

    fun addViews(input: List<View>, relocate: Boolean = true) {
        val p = parent as? ViewGroup ?: return
        var index = p.indexOfChild(this)
        input.forEach {
            val lp = it.layoutParams as ConstraintLayout.LayoutParams
            p.addView(it.apply {
                if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {
                    layoutDirection = LAYOUT_DIRECTION_RTL
                }
            }, index, if (relocate) checkLp(lp) else lp)

            index++
        }
        children.addAll(input)
    }

    fun addAllViews(relocate: Boolean = true) {
        val p = parent as? ViewGroup ?: return
        var index = p.indexOfChild(this)
        children.forEach {
            val lp = it.layoutParams as ConstraintLayout.LayoutParams
            p.addView(it.apply {
                if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {
                    layoutDirection = LAYOUT_DIRECTION_RTL
                }
            }, index, if (relocate) checkLp(lp) else lp)

            index++
        }
    }

    fun removeAllViews() {
        val p = parent as? ViewGroup
        children.forEach {
            p?.removeView(it)
        }
    }

    override fun setAlpha(alpha: Float) {
        children.forEach {
            it.alpha = alpha
        }
    }

    override fun setTranslationY(translationY: Float) {
        children.forEach {
            it.translationY = translationY
        }
    }

    private fun checkLp(lp: ConstraintLayout.LayoutParams): ConstraintLayout.LayoutParams {
        val spaceId = id
        if (lp.topToTop == 0) {
            lp.topToTop = spaceId
        }
        if (lp.topToBottom == 0) {
            lp.topToBottom = spaceId
        }
        if (lp.bottomToBottom == 0) {
            lp.bottomToBottom = spaceId
        }
        if (lp.bottomToTop == 0) {
            lp.bottomToTop = spaceId
        }
        if (lp.startToStart == 0) {
            lp.startToStart = spaceId
        }
        if (lp.startToEnd == 0) {
            lp.startToEnd = spaceId
        }
        if (lp.endToEnd == 0) {
            lp.endToEnd = spaceId
        }
        if (lp.endToStart == 0) {
            lp.endToStart = spaceId
        }
        if (lp.rightToLeft == 0) {
            lp.rightToLeft = spaceId
        }
        if (lp.rightToRight == 0) {
            lp.rightToRight = spaceId
        }
        if (lp.leftToLeft == 0) {
            lp.leftToLeft = spaceId
        }
        if (lp.leftToRight == 0) {
            lp.leftToRight = spaceId
        }
        return lp
    }
}