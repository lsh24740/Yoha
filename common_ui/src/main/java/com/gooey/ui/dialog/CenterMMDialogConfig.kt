package com.gooey.ui.dialog

import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.gooey.ui.R

/**
 *@author lishihui01
 *@Date 2023/9/16
 *@Describe:
 */
open class CenterMMDialogConfig {
    var root: View? = null
    var style = DialogFragment.STYLE_NO_TITLE
    var theme = R.style.CommonBottomDialogStyle
    var height = ViewGroup.LayoutParams.MATCH_PARENT
    var width = ViewGroup.LayoutParams.MATCH_PARENT
    var gravity = Gravity.CENTER
    var windowAnim = android.R.anim.fade_in
    var swipeable = true // 是否支持滑动关闭
    var closeButton: Drawable? = null // 关闭按钮，默认没有
    var closeDistance = -1
    var cancelable = true
    var canceledOnTouchOutside = true
    var dimBehind = false
    var dimAmount = -1f
    var transparentStatusBar = true
    var fullscreen = false
    var inTask = true // 是否要进Task栈管理
    var block = false // 是否要挡住其他弹框
    var hasEditor = false // 是否会弹出输入法
    var navigationBarColor = 0
    var customUiFlag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
}