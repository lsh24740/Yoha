package com.gooey.ui.dialog

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isGone
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.Utils
import com.gooey.common.ui.dp
import com.gooey.common.ui.dpF
import com.gooey.ui.R
import com.gooey.ui.databinding.DialogTipsLayoutBinding

/**
 *@author lishihui01
 *@Date 2023/9/16
 *@Describe:通用提醒对话框
 */
class TipsDialog private constructor(private val builder: Builder) : CommonDialogFragment() {


    override fun getDialogConfig(): BottomDialogConfig {
        val config = super.getDialogConfig()
        config.width = ScreenUtils.getScreenWidth() - 62.dp()
        config.gravity = Gravity.CENTER
        config.dimBehind = true
        config.windowAnim = 0
        config.canceledOnTouchOutside = false
        config.cancelable = false
        return config
    }

    override fun onCreateViewInner(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val binding = DialogTipsLayoutBinding.inflate(inflater)
        render(binding)
        return binding.root
    }

    private fun render(binding: DialogTipsLayoutBinding) {
        binding.run {
            title.isGone = builder.title.isNullOrBlank()
            closed.isGone = !builder.closeButton
            yes.isGone = builder.singleButton
            no.isGone = builder.singleButton
            root.background = builder.backgroundDrawable
            single.isGone = !builder.singleButton
            title.text = builder.title ?: ""
            content.text = builder.content ?: ""
            title.setTextColor(builder.titleColor)
            content.setTextColor(builder.contentColor)
            title.textSize = builder.titleSize.toFloat()
            content.textSize = builder.contentSize.toFloat()
            yes.text = builder.yesButtonText
            no.text = builder.noButtonText
            yes.setTextColor(builder.yesButtonTextColor)
            no.setTextColor(builder.noButtonTextColor)
            yes.textSize = builder.yesButtonTextSize.toFloat()
            no.textSize = builder.noButtonTextSize.toFloat()
            yes.background = builder.yesButtonDrawable
            no.background = builder.noButtonDrawable
            closed.setImageDrawable(builder.closeButtonDrawable)
            if (builder.titleBold) {
                title.typeface = Typeface.DEFAULT_BOLD
            }
            yes.setOnClickListener { v -> builder.yesClickListener?.onClick(this@TipsDialog, v) }
            no.setOnClickListener { v -> builder.noClickListener?.onClick(this@TipsDialog, v) }
            single.setOnClickListener { v -> builder.singleClickListener?.onClick(this@TipsDialog, v) }
            closed.setOnClickListener { v ->
                if (builder.closeClickListener == null) {
                    dismissAllowingStateLoss()
                } else {
                    builder.closeClickListener?.onClick(this@TipsDialog, v)
                }
            }
        }
    }

    class Builder {

        var backgroundDrawable: Drawable = GradientDrawable().apply {
            cornerRadius = 16.dpF()
            setColor(Color.WHITE)
        }
            private set

        var title: String? = null
            private set

        var content: String? = null
            private set

        var titleColor = Color.parseColor("#781FFF")
            private set

        var titleSize = 19
            private set

        var titleBold = true
            private set

        var contentColor = Color.parseColor("#666666")
            private set
        var contentSize = 16
            private set

        var yesButtonText = "Yes"
            private set

        var noButtonText = "NO"
            private set

        var yesButtonTextColor = Color.parseColor("#7E2FFF")
            private set

        var noButtonTextColor = Color.WHITE
            private set

        var yesButtonTextSize = 16
            private set

        var noButtonTextSize = 16
            private set


        var yesButtonDrawable: Drawable = Utils.getApp().getDrawable(R.drawable.tip_ripple_background_yes)?:GradientDrawable().apply {
            cornerRadius = 19.dpF()
            setColor(Color.parseColor("#a086c8"))
        }
            private set

        var noButtonDrawable: Drawable = Utils.getApp().getDrawable(R.drawable.default_ripple_background_normal)?:GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.parseColor("#9342FF"), Color.parseColor("#7B2CFF"))).apply {
            cornerRadius = 19.dpF()
        }
            private set

        var closeButtonDrawable = AppCompatResources.getDrawable(Utils.getApp(), R.drawable.dialog_closed)
            private set
        var closeButton = true
            private set
        var singleButton = false
            private set
        var yesClickListener: TipsDialogOnClickListener? = null
            private set
        var noClickListener: TipsDialogOnClickListener? = null
            private set
        var singleClickListener: TipsDialogOnClickListener? = null
            private set

        var closeClickListener: TipsDialogOnClickListener? = null
            private set

        fun backgroundDrawable(drawable: Drawable) = apply {
            backgroundDrawable = drawable
        }

        fun title(title: String) = apply {
            this.title = title
        }

        fun content(content: String) = apply {
            this.content = content
        }

        fun titleColor(titleColor: Int) = apply {
            this.titleColor = titleColor
        }

        fun titleSize(titleSize: Int) = apply {
            this.titleSize = titleSize
        }

        fun titleBold(titleBold: Boolean) = apply {
            this.titleBold = titleBold
        }

        fun contentColor(contentColor: Int) = apply {
            this.contentColor = contentColor
        }

        fun contentSize(contentSize: Int) = apply {
            this.contentSize = contentSize
        }

        fun yesButtonText(yesButtonText: String) = apply {
            this.yesButtonText = yesButtonText
        }

        fun noButtonText(noButtonText: String) = apply {
            this.noButtonText = noButtonText
        }

        fun yesButtonTextColor(yesButtonTextColor: Int) = apply {
            this.yesButtonTextColor = yesButtonTextColor
        }

        fun yesButtonTextSize(yesButtonTextSize: Int) = apply {
            this.yesButtonTextSize = yesButtonTextSize
        }

        fun noButtonTextColor(noButtonTextColor: Int) = apply {
            this.noButtonTextColor = noButtonTextColor
        }

        fun noButtonTextSize(noButtonTextSize: Int) = apply {
            this.noButtonTextSize = noButtonTextSize
        }

        fun yesButtonDrawable(yesButtonDrawable: Drawable) = apply {
            this.yesButtonDrawable = yesButtonDrawable
        }

        fun noButtonDrawable(noButtonDrawable: Drawable) = apply {
            this.noButtonDrawable = noButtonDrawable
        }

        fun closeButtonDrawable(closeButtonDrawable: Drawable) = apply {
            this.closeButtonDrawable = closeButtonDrawable
        }

        fun singleButton(singleButton: Boolean) = apply {
            this.singleButton = singleButton
        }

        fun closeButton(closeButton: Boolean) = apply {
            this.closeButton = closeButton
        }

        fun yesClick(yesClickListener: TipsDialogOnClickListener) = apply {
            this.yesClickListener = yesClickListener
        }

        fun noClick(noClickListener: TipsDialogOnClickListener) = apply {
            this.noClickListener = noClickListener
        }

        fun singleClick(singleClickListener: TipsDialogOnClickListener) = apply {
            this.singleClickListener = singleClickListener
        }

        fun closeClick(closeClickListener: TipsDialogOnClickListener) = apply {
            this.closeClickListener = closeClickListener
        }

        fun build(): TipsDialog {
            return TipsDialog(this)
        }

        companion object {
            @JvmStatic
            fun with(): Builder {
                return Builder()
            }
        }
    }

    interface TipsDialogOnClickListener {
        fun onClick(dialog: TipsDialog, view: View)
    }
}

