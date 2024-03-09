package com.gooey.ui.dialog

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.gooey.ui.R
import com.gooey.ui.view.FitSystemWindowHackFrameLayout2

/**
 *@author lishihui01
 *@Date 2023/9/21
 *@Describe:
 */
class KeyboardHelper(private val host: CommonDialogFragment) {
    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        val bottom = target?.paddingBottom ?: 0
        val current = root?.paddingBottom ?: 0
        val local = root
        if (bottom != current && local != null && target != null) {
            local.setPadding(local.paddingLeft, local.paddingTop, local.paddingRight, bottom)
            false
        } else {
            true
        }
    }

    private var target: View? = null
    private var root: View? = null

    fun attach(attach: Boolean, input: View? = null) {
        this.root = input
        if (attach) {
            doAttach()
        } else {
            doDetach()
        }
    }

    private fun doAttach() {
        val activity = host.activity ?: return
        val activityRoot = activity.window?.decorView ?: return
        var local = activityRoot.findViewById<ViewGroup>(R.id.keyboardBackground)
        if (local == null && activityRoot is ViewGroup) {
            local = FitSystemWindowHackFrameLayout2(activity)
            activityRoot.addView(local, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }

        val current = (local.getTag(R.id.keyboardBackgroundDData) as? Int ?: 0) + 1
        local.setTag(R.id.keyboardBackgroundDData, current)
        target = local
        if (local.viewTreeObserver.isAlive) {
            local.viewTreeObserver.addOnPreDrawListener(preDrawListener)
        }
    }

    private fun doDetach() {
        val local = target ?: return
        val current = (local.getTag(R.id.keyboardBackgroundDData) as? Int ?: 1) - 1
        local.setTag(R.id.keyboardBackgroundDData, current)
        if (current == 0) {
            val activityRoot = host.activity?.window?.decorView
            if (activityRoot is ViewGroup) {
                activityRoot.removeView(local)
            }
        }
        if (local.viewTreeObserver.isAlive) {
            local.viewTreeObserver.removeOnPreDrawListener(preDrawListener)
        }
    }
}