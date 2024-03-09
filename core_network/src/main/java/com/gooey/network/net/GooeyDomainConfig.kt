package com.gooey.network.net

import com.blankj.utilcode.util.AppUtils


/**
 *@author lishihui01
 *@Date 2023/8/31
 *@Describe:
 */
object GooeyDomainConfig {
    /**
     * 正式地址线上版本
     */
    var releaseBaseUrl = "https://api.gooeychat.com/" //正式wss

//    private val debugBaseUrl = "https://dev.gooeychat.com:30443/" //国内测试
    private val debugBaseUrl = "http://192.168.3.14:18888/" //国内测试

    fun apiBaseUrl(): String {
        return if (AppUtils.isAppDebug()) {
            debugBaseUrl
        } else {
            releaseBaseUrl
        }
    }
}