package com.gooey.network.net.interceptor

import android.os.Build
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.RomUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.net.ssl.SSLException

/**
 *@author lishihui01
 *@Date 2023/8/31
 *@Describe:
 */
class GooeyFuckingExceptionInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        try {
            return chain.proceed(originalRequest)
        } catch (e: NullPointerException) {
            e.printStackTrace()
            if (AppUtils.isAppDebug()) {
                throw e
            }
            val msg = e.message
            if (msg != null && msg.contains(SSL_SESSION_NULL) && isStupidMeizuNPE) {
                LogUtils.dTag(
                    "GooeyFuckingExceptionInterceptor",
                    "GooeyFuckingExceptionInterceptor catch ssl_session npe and throw SSLException to GooeyHttpsInterceptor:" + originalRequest.url
                )
                throw SSLException(SSL_SESSION_NULL) //抛给GooeyHttpsInterceptor去处理
            }
            if (msg != null && msg.contains("longValue") && isStupidMeizuNPE) {
                //MUSIC-93809  [Android 云捕] [Crash] java.lang.NullPointerException - Attempt to invoke virtual method 'long java.lang.Long.longValue()' on a null object reference (5535a0e31a219bde5c42e38bc0137c49)
                throw SSLException("") //抛给GooeyHttpsInterceptor去处理
            } else {
                LogUtils.dTag("GooeyFuckingExceptionInterceptor", "throw npe")
                throw e
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
            if (AppUtils.isAppDebug()) {
                throw e
            }
            if (isStupidMeizuAndroidKitkat) {
                throw IOException("meizusb")
            } else {
                throw e
            }
        } catch (e: UnsatisfiedLinkError) {
            //某些网络底层native缺失会导致这类异常，只在部分手机上
            throw IOException(e)
        } catch (e: NoSuchElementException) {
            throw IOException(e)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            if (AppUtils.isAppDebug()) {
                throw e
            }
            val msg = e.message
            if (msg != null && msg.contains(VIVO_Index_0)) {
                LogUtils.dTag(
                    "GooeyFuckingExceptionInterceptor",
                    "GooeyFuckingExceptionInterceptor catch IndexOutOfBoundsException:" + originalRequest.url
                )
                throw IOException(VIVO_Index_0)
            } else {
                LogUtils.dTag(
                    "GooeyFuckingExceptionInterceptor",
                    "throw IndexOutOfBoundsException"
                )
                throw e
            }
        } catch (e: RuntimeException) {
            val msg = e.message
            val cause = e.cause
            if (msg != null && msg.contains(CRYPTO_SSL_NEW_MSG) && cause is SSLException) {
                LogUtils.dTag(
                    "GooeyFuckingExceptionInterceptor",
                    "GooeyFuckingExceptionInterceptor catch RuntimeException with SSLException for crypto create ssl:" + originalRequest.url
                )
                throw IOException(e)
            } else {
                throw e
            }
        }
    }

    companion object {
        const val SSL_SESSION_NULL = "ssl_session == null"
        private const val VIVO_Index_0 = "Index: 0"
        private const val CRYPTO_SSL_NEW_MSG = "Unable to create application data"
        val isStupidMeizuNPE: Boolean
            get() = RomUtils.isMeizu() && Build.VERSION.SDK_INT == Build.VERSION_CODES.M
        private val isStupidMeizuAndroidKitkat: Boolean
            private get() = RomUtils.isMeizu() && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT
    }
}