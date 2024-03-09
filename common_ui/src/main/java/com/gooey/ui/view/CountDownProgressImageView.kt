package com.gooey.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.AttributeSet
import android.widget.ImageView

/**
 *@author lishihui01
 *@Date 2023/9/23
 *@Describe:
 */
class CountDownProgressImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {
    private var imageSize = 0
    private var borderColor = Color.parseColor("#7B2CFF")
    private var borderWidth = 10f
    private var countDownDuration = 0L
    private var countDownTimer: CountDownTimer? = null
    private var timeOver: Long = 0
    private var startTime: Long = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isDither = true
        style = Paint.Style.STROKE
        color = borderColor
        strokeWidth = borderWidth
        strokeCap = Paint.Cap.ROUND
    }
    var onCountDownCompleteListener: OnCountDownCompleteListener? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        imageSize = w.coerceAtMost(h)
        val padding = (borderWidth / 2).toInt()
        setPadding(padding, padding, padding, padding)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (countDownDuration > 0) {
            canvas?.drawArc(
                    width / 2f - imageSize / 2f + borderWidth / 2,
                    height / 2f - imageSize / 2f + borderWidth / 2,
                    width / 2f + imageSize / 2f - borderWidth / 2,
                    height / 2f + imageSize / 2f - borderWidth / 2,
                    270f,
                    timeOver * 360f / countDownDuration,
                    false,
                    paint
            )
        }
    }

    fun setBorderColor(color: Int) {
        borderColor = color
        paint.color = color
    }

    fun setBorderWidth(width: Float) {
        borderWidth = width
        paint.strokeWidth = width
        val padding = (borderWidth / 2).toInt()
        setPadding(padding, padding, padding, padding)
    }

    fun setCountDownDuration(duration: Long) {
        countDownDuration = duration
    }

    fun start() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 50) {
            override fun onTick(millisUntilFinished: Long) {
                timeOver = SystemClock.elapsedRealtime() - startTime
                if (timeOver >= countDownDuration) {
                    if (timeOver > countDownDuration) {
                        timeOver = countDownDuration
                        invalidate()
                    }
                    stop()
                    onCountDownCompleteListener?.onComplete()
                } else {
                    invalidate()
                }
            }

            override fun onFinish() {
            }
        }
        countDownTimer?.start()
        startTime = SystemClock.elapsedRealtime()
    }


    fun stop() {
        countDownDuration = 0
        countDownTimer?.cancel()
        countDownTimer = null
        invalidate()
    }
}

interface OnCountDownCompleteListener {
    fun onComplete()
}