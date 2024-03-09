package com.gooey.network.datasource

import androidx.annotation.Keep
import com.gooey.common.INoProguard
import java.net.HttpURLConnection

/**
 *@author lishihui01
 *@Date 2023/8/29
 *@Describe:API的请求返回封装成这个类
 */
@Keep
open class ApiResult<Data>(var code: Int = HttpURLConnection.HTTP_OK,
                           var msg: String? = null,
                           var data: Data? = null) : INoProguard {
    @Transient
    var exception: CMNetworkIOException? = null

    @Transient
    var httpCode: Int = HttpURLConnection.HTTP_OK

    companion object {

        fun <Data> success(data: Data): ApiResult<Data> {
            return ApiResult(data = data)
        }

        fun <Data> exception(exception: CMNetworkIOException): ApiResult<Data> {
            return ApiResult<Data>(0).apply {
                this.exception = exception
            }
        }
    }

    fun isSuccess(): Boolean {
        return exception == null && code == 1
    }

    override fun toString(): String {
        return "ApiResult(code=$code, message=$msg, exception=$exception, httpCode=$httpCode, data=$data)"
    }
}