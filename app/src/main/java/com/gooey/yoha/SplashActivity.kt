package com.gooey.yoha

import android.os.Handler
import android.os.Looper
import android.view.View
import com.gooey.ui.base.BaseActivity


/**
 *@author lishihui01
 *@Date 2024/3/2
 *@Describe:
 */
class SplashActivity : BaseActivity() {
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    override fun onViewCreated(paramView: View?) {
        super.onViewCreated(paramView)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
    }
}