package com.gooey.yoha

import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.sankuai.waimai.router.Router
import com.sankuai.waimai.router.common.DefaultRootUriHandler
import com.sankuai.waimai.router.components.DefaultLogger
import com.sankuai.waimai.router.core.Debugger
import com.tencent.mmkv.MMKV


/**
 *@author lishihui01
 *@Date 2024/3/2
 *@Describe:
 */
class YohaApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        Utils.init(this)
        LogUtils.getConfig().setLogSwitch(AppUtils.isAppDebug())
        MMKV.initialize(this as Context)
        initRouter()
    }
    private fun initRouter() {
        Debugger.setLogger(DefaultLogger())
        Debugger.setEnableLog(true)
        Debugger.setEnableDebug(true)
        Router.init(DefaultRootUriHandler(this as Context, "gooey", "yoha"))
    }
}