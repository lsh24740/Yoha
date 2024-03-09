package com.gooey.network.net.interceptor

import android.text.TextUtils
import com.blankj.utilcode.util.SPUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 *@author lishihui01
 *@Date 2023/8/31
 *@Describe:
 */
class HeaderInterceptor(private var headers: HashMap<String, String>? = null) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request()
                .newBuilder()
        if (null == headers) {
            headers = HashMap()
        }
        val token: String = SPUtils.getInstance().getString("token", "");
        if (!TextUtils.isEmpty(token)) {
            headers!!["Authorization"] = "Bearer $token"
        }
        val keys: Set<String> = headers!!.keys
        for (headerKey in keys) {
            builder.addHeader(headerKey, headers!![headerKey]!!).build()
        }
        return chain.proceed(builder.build())
    }
}