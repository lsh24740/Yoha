package com.gooey.network.net

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.gooey.common.KThreadPool
import com.gooey.network.net.cookie.GooeyCookieJarImpl
import com.gooey.network.net.cookie.GooeyPersistentCookieStore
import com.gooey.network.net.interceptor.GooeyFuckingExceptionInterceptor
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.internal.threadFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 *@author lishihui01
 *@Date 2023/8/31
 *@Describe:
 */
object NetworkConfig {

    private const val TAG = "NetworkConfig"
    private const val CACHE_SIZE = 10 * 1024 * 1024L

    val okHttpClient by lazy {
        configOKHttpClient(configBuilder())
    }


    private fun configBuilder(): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
        builder.dispatcher(
                Dispatcher(
                        KThreadPool.okHttpExecutor(
                                threadFactory(
                                        "OkHttp Dispatcher",
                                        false
                                )
                        )
                )
        )
        builder.addInterceptor(GooeyFuckingExceptionInterceptor())
        builder.readTimeout(HttpConst.TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(HttpConst.TIME_OUT, TimeUnit.MILLISECONDS)
                .connectTimeout(HttpConst.TIME_OUT, TimeUnit.MILLISECONDS)
                .callTimeout(HttpConst.TIME_OUT, TimeUnit.MILLISECONDS)
        builder.cache(
                Cache(
                        File(Utils.getApp().cacheDir, "http_cache"),
                        CACHE_SIZE
                )
        )
        SSLSocketClient.getSslSocketFactory()?.let {
            builder.sslSocketFactory(it.first, it.second)
        }
        builder.cookieJar(
                GooeyCookieJarImpl(
                        GooeyPersistentCookieStore(
                                Utils.getApp()
                        )
                )
        )
        builder.connectionSpecs(
                listOf(
                        ConnectionSpec.MODERN_TLS,
                        ConnectionSpec.COMPATIBLE_TLS,
                        ConnectionSpec.CLEARTEXT
                )
        )
        return builder
    }

    private fun configOKHttpClient(builder: OkHttpClient.Builder): OkHttpClient {
        LogUtils.dTag(
                TAG,
                "init customOKHttpClient------------------->>>>"
        )
        val okHttpClient: OkHttpClient = builder.build()
        okHttpClient.dispatcher.maxRequestsPerHost = 20
        LogUtils.dTag(
                TAG,
                "init customOKHttpClient-------------------<<<<"
        )
        return okHttpClient
    }

}