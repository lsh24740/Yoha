package com.gooey.ui.dialog

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.gooey.ui.databinding.LayoutCommonDialogFragmentBinding
import com.gooey.ui.view.ClosableSlidingLayout

/**
 *@author lishihui01
 *@Date 2023/9/21
 *@Describe:
 */
class BottomDialogHelper(val callback: BottomDialogCallback,
                         private val customView: View,
                         val binding: LayoutCommonDialogFragmentBinding,
                         config: BottomDialogConfig) {
    private val closeable = config.swipeable
    private var hiding = false
    private var dismissing = false
    private var finishing = false

    init {
        binding.slideContainer.addView(customView)
        val lp = binding.slideContainer.layoutParams
        lp.width = config.width
        lp.height = config.height
        binding.rootContainer.gravity = config.gravity

        val swipe = closeable && config.gravity != Gravity.CENTER
        binding.slideContainer.swipeable = swipe
        if (!swipe) {
            binding.slideContainer.isClickable = true
            binding.slideContainer.isFocusable = true
        }
        val vertical = (config.gravity and Gravity.TOP) == Gravity.TOP
                || (config.gravity and Gravity.BOTTOM) == Gravity.BOTTOM
                || config.gravity == Gravity.CENTER
        val start = (config.gravity and Gravity.TOP) == Gravity.TOP
                || (config.gravity and Gravity.LEFT) == Gravity.LEFT
        binding.slideContainer.setVertical(vertical, start)
        binding.slideContainer.setSlideListener(object : ClosableSlidingLayout.SlideListener {
            override fun onClosed() {
                if (!callback.canDismiss()) {
                    return
                }
                if (hiding) {
                    return
                }
                dismiss(true)
            }

            override fun onOpened() {}
            override fun onSlided(rate: Float) {}
        })
        binding.rootContainer.setOnClickListener {
            if (callback.canDismiss() && config.canceledOnTouchOutside && !dismissing) {
                dismissing = true
                dismiss()
            }
        }
        if (vertical && config.closeButton != null) {
            binding.closeBtn.setImageDrawable(config.closeButton)
            binding.closeBtn.visibility = View.VISIBLE
            if (config.closeDistance >= 0) {
                (binding.closeBtn.layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin =
                        config.closeDistance
            }
            binding.closeBtn.setOnClickListener {
                callback.closeBtnClick()
                dismiss(true)
            }
        }
        binding.rootContainer.fitsSystemWindows = false
        binding.rootContainer.requestFocus()
    }

    fun hide() {
        if (finishing) {
            return
        }
        hiding = true
        binding.slideContainer.dismiss(customView, 0f)
        binding.slideContainer.swipeable = false
        binding.slideContainer.setOnClickListener(View.OnClickListener {
            if (!callback.canDismiss()) {
                return@OnClickListener
            }
            if (dismissing) {
                return@OnClickListener
            }
            dismissing = true
            dismiss(true)
        })
    }

    fun show() {
        if (hiding) {
            hiding = false
            binding.slideContainer.show(customView)
            binding.slideContainer.swipeable = closeable
            binding.slideContainer.setOnClickListener(null)
            callback.onReShow()
        }
    }

    fun dismiss(fade: Boolean = false) {
        callback.dismiss(fade || hiding)
        dismissing = true
        finishing = true
        hiding = false
    }
}