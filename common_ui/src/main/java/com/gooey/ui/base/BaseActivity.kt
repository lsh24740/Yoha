package com.gooey.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.Utils
import com.gooey.common.ui.dp
import com.gooey.ui.R


/**
 *@author lishihui01
 *@Date 2024/1/31
 *@Describe:
 */
open class BaseActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isImmersionMode()) {
            BarUtils.transparentStatusBar(this)
            BarUtils.transparentNavBar(this)
        }
        val isStatusBarLightMode = isStatusBarLightMode()
        BarUtils.setStatusBarLightMode(this, isStatusBarLightMode)
        setContentView(R.layout.activity_base)
        val layoutInflater = LayoutInflater.from(this)
        val frameLayout: FrameLayout = getContentContainer()
        val view = onCreateView(layoutInflater, frameLayout)
        if (view != null && view.parent == null) {
            frameLayout.addView(view)
        }
        var statusBarHeight = BarUtils.getStatusBarHeight()
        if (statusBarHeight == 0) {
            statusBarHeight = 33.dp()
        }
        if (needToolBar()) {
            val toolbar = getToolBar()
            toolbar.visibility = View.VISIBLE
            toolbar.setNavigationOnClickListener { finish() }
            if (isStatusBarLightMode) {
                toolbar.overflowIcon =
                    Utils.getApp().getDrawable(R.drawable.action_menu_black)
                toolbar.setNavigationIcon(R.drawable.actionbar_back_black)
                toolbar.setTitleTextColor(-16777216)
            } else {
                toolbar.overflowIcon =
                    Utils.getApp().getDrawable(R.drawable.action_menu_white)
                toolbar.setNavigationIcon(R.drawable.actionbar_back)
                toolbar.setTitleTextColor(-1)
            }
            val toolbarHeight = resources.getDimension(R.dimen.toolbarHeight)
            if (isImmersionMode()) {
                (toolbar.layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin = statusBarHeight
                view?.setPadding(
                    view.paddingLeft,
                    (view.paddingTop + statusBarHeight + toolbarHeight) as Int,
                    view.paddingRight,
                    view.paddingBottom
                )
            } else {
                view?.setPadding(
                    view.paddingLeft,
                    (view.paddingTop + toolbarHeight).toInt(),
                    view.paddingRight,
                    view.paddingBottom
                )
            }
        } else {
            view?.setPadding(
                view.paddingLeft,
                view.paddingTop + statusBarHeight,
                view.paddingRight,
                view.paddingBottom
            )
        }
        onViewCreated(view)
    }

    fun getContentContainer(): FrameLayout {
        return findViewById(R.id.rootContent)
    }

    open fun onCreateView(paramLayoutInflater: LayoutInflater, paramViewGroup: ViewGroup): View? {
        return null
    }

    open fun onViewCreated(paramView: View?) {

    }

    fun getToolBar(): Toolbar {
        return findViewById(R.id.toolbar)
    }

    fun setTitle(title: String) {
        getToolBar().title = title
    }

    open fun isImmersionMode(): Boolean {
        return true
    }

    open fun isStatusBarLightMode(): Boolean {
        return true
    }

    open fun needToolBar(): Boolean {
        return false
    }
}