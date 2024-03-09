package com.gooey.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.gooey.common.ui.dp
import com.gooey.common.utils.ReflectUtils

/**
 *@author lishihui01
 *@Date 2023/10/21
 *@Describe:
 */
class FadingRecyclerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr)  {
    private var mFadingEdgeLength = 0
    var mPaint: Paint? = null
    var mMatrix: Matrix? = null
    var mShader: Shader? = null

   init {
       mPaint = Paint()
       mMatrix = Matrix()
       mShader = LinearGradient(0f, 0f,
               0f, 1f, -0x34000000, 0, Shader.TileMode.CLAMP)
       mPaint?.shader = mShader
       mPaint?.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
   }

    override fun setFadingEdgeLength(length: Int) {
        if (length != mFadingEdgeLength) {
            mFadingEdgeLength = length
            val fadingEdge = verticalFadingEdgeLength
            val rate = 1 - fadingEdge.toFloat() / (fadingEdge + mFadingEdgeLength).toFloat()
            mShader = LinearGradient(0f, 0f,
                    0f, 1f, intArrayOf(-0x1000000, -0xe000000, 0), floatArrayOf(0f, rate, 1f), Shader.TileMode.CLAMP)
            mPaint!!.shader = mShader
            invalidate()
        }
    }

    override fun getVerticalFadingEdgeLength(): Int {
        return 30.dp()
    }

    protected fun getFadeTop(offsetRequired: Boolean): Int {
        var top = paddingTop
        if (offsetRequired) top += topPaddingOffset
        return top
    }

    protected fun getFadeHeight(offsetRequired: Boolean): Int {
        var padding = paddingTop
        if (offsetRequired) padding += topPaddingOffset
        return bottom - top - paddingBottom - padding
    }

    @SuppressLint("WrongConstant")
    override fun draw(canvas: Canvas) {
        if (mFadingEdgeLength <= 0) {
            super.draw(canvas)
            return
        }
        var paddingLeft = paddingLeft
        val offsetRequired = isPaddingOffsetRequired
        if (offsetRequired) {
            paddingLeft += leftPaddingOffset
        }
        val left = scrollX + paddingLeft
        var right = left + right - getLeft() - getPaddingLeft() - paddingLeft
        val top = scrollY + getFadeTop(offsetRequired)
        var bottom = top + getFadeHeight(offsetRequired)
        if (offsetRequired) {
            right += rightPaddingOffset
            bottom += bottomPaddingOffset
        }
        val length = verticalFadingEdgeLength + mFadingEdgeLength
        val saveCount = canvas.saveCount
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            try {
                ReflectUtils.invokeMethod(true, Canvas::class.java,
                        "saveUnclippedLayer", arrayOf<Class<*>?>(Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType), canvas,
                        left, top, right, top + length)
            } catch (e: RuntimeException) {
                canvas.saveLayer(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), null)
            }
        } else {
            canvas.saveLayer(left.toFloat(), top.toFloat(), right.toFloat(), (top + length).toFloat(), null, 0x04)
        }
        super.draw(canvas)
        val p = mPaint
        val matrix = mMatrix
        val fade = mShader
        matrix!!.setScale(1f, length.toFloat())
        matrix.postTranslate(left.toFloat(), top.toFloat())
        fade!!.setLocalMatrix(matrix)
        p!!.shader = fade
        canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), (top + length).toFloat(), p)
        canvas.restoreToCount(saveCount)
    }
}