package com.gooey.network.datasource

import com.gooey.common.INoProguard
import java.io.IOException

/**
 *@author lishihui01
 *@Date 2023/8/29
 *@Describe:网络层的异常都会被捕获，然后抛出这个异常
 */

class CMNetworkIOException : IOException, INoProguard {
    constructor(cause: Throwable?) : super(cause)
    constructor(cause: String?) : super(cause)

    override fun printStackTrace() {
        if (cause != null) {
            cause!!.printStackTrace()
        } else {
            super.printStackTrace()
        }
    }

    companion object {
        private const val serialVersionUID = 4108883799571801101L
    }
}