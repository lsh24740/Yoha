package com.gooey.network.net.interceptor

import com.blankj.utilcode.util.LogUtils
import okhttp3.Connection
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.internal.http.promisesBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 *@author lishihui01
 *@Date 2023/8/31
 *@Describe:
 */
/**
 * An OkHttp interceptor which logs request and response information. Can be applied as an
 * [application interceptor][OkHttpClient.interceptors] or as a [ ][OkHttpClient.networkInterceptors].
 *
 * The format of the logs created by
 * this class should not be considered stable and may change slightly between releases. If you need
 * a stable logging format, use your own interceptor.
 */
class HttpLoggingInterceptor(tag: String?) : Interceptor {
    @Volatile
    private var printLevel = Level.NONE
    private var colorLevel = java.util.logging.Level.INFO
    private val logger: Logger

    enum class Level {
        NONE,  //不打印log
        BASIC,  //只打印 请求首行 和 响应首行
        HEADERS,  //打印请求和响应的所有 Header
        BODY //所有数据全部打印
    }

    init {
        logger = Logger.getLogger(tag)
    }

    fun setLevel(level: Level) {
        printLevel = level
    }

    fun setColorLevel(level: java.util.logging.Level) {
        colorLevel = level
    }

    fun log(message: String?) {
        logger.log(colorLevel, message)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        if (printLevel == Level.NONE) {
            return chain.proceed(request)
        }

        //请求日志拦截
        logForRequest(request, chain.connection())

        //执行请求，计算请求时间
        val startNs = System.nanoTime()
        val response: Response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            log("<-- HTTP FAILED: $e")
            throw e
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        //响应日志拦截
        return logForResponse(response, tookMs)
    }

    @Throws(IOException::class)
    private fun logForRequest(request: Request, connection: Connection?) {
        if (filterNoiseAPI(request.url)) {
            return
        }
        val logBody = printLevel == Level.BODY
        val logHeaders = printLevel == Level.BODY || printLevel == Level.HEADERS
        val requestBody = request.body
        val hasRequestBody = requestBody != null
        val protocol = connection?.protocol() ?: Protocol.HTTP_1_1
        try {
            val requestStartMessage = "--> " + request.method + ' ' + request.url + ' ' + protocol
            log(requestStartMessage)
            if (logHeaders) {
                val headers = request.headers
                var i = 0
                val count = headers.size
                while (i < count) {
                    log("\t" + headers.name(i) + ": " + headers.value(i))
                    i++
                }
                log(" ")
                if (logBody && hasRequestBody) {
                    if (isPlaintext(requestBody!!.contentType())) {
                        bodyToString(request)
                    } else {
                        log("\tbody: maybe [file part] , too large too print , ignored!")
                    }
                }
            }
        } catch (e: Exception) {
            log("<-- HTTP FAILED: $e")
            LogUtils.eTag(TAG, e)
            throw e
        } finally {
            log("--> END " + request.method)
        }
    }

    private fun filterNoiseAPI(url: HttpUrl): Boolean {
        val urlString = url.toString()
        if (urlString.contains("pl/count")) {
            return true
        } else if (urlString.contains("clientlog/upload") || urlString.contains("clientlog/mam")) {
            return true
        }
        return false
    }

    private fun logForResponse(response: Response, tookMs: Long): Response {
        if (filterNoiseAPI(response.request.url)) {
            return response
        }
        val builder: Response.Builder = response.newBuilder()
        val clone: Response = builder.build()
        var responseBody = clone.body
        val logBody = printLevel == Level.BODY
        val logHeaders = printLevel == Level.BODY || printLevel == Level.HEADERS
        try {
            log("<-- " + clone.code + ' ' + clone.message + ' ' + clone.request.url + " (" + tookMs + "ms）")
            if (logHeaders) {
                val headers = clone.headers
                var i = 0
                val count = headers.size
                while (i < count) {
                    log("\t" + headers.name(i) + ": " + headers.value(i))
                    i++
                }
                log(" ")
                if (logBody && clone.promisesBody()) {
                    if (isPlaintext(responseBody!!.contentType())) {
                        val body = responseBody.string()
                        log("\tbody:$body")
                        responseBody = ResponseBody.create(responseBody.contentType(), body)
                        return response.newBuilder().body(responseBody).build()
                    } else {
                        log("\tbody: maybe [file part] , too large too print , ignored!")
                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.eTag(TAG, e)
        } finally {
            log("<-- END HTTP")
        }
        return response
    }

    private fun bodyToString(request: Request) {
        try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            copy.body!!.writeTo(buffer)
            var charset = UTF8
            val contentType = copy.body!!.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)
            }
            log("\tbody:" + buffer.readString(charset!!))
        } catch (e: Exception) {
            LogUtils.eTag(TAG, e)
        }
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
        const val TAG = "Gooey_OKHttp"

        /**
         * Returns true if the body in question probably contains human readable text. Uses a small sample
         * of code points to detect unicode control characters commonly used in binary file signatures.
         */
        private fun isPlaintext(mediaType: MediaType?): Boolean {
            if (mediaType == null) return false
            if (mediaType.type == "text") {
                return true
            }
            var subtype = mediaType.subtype
            subtype = subtype.lowercase(Locale.getDefault())
            if (subtype.contains("x-www-form-urlencoded") ||
                subtype.contains("json") ||
                subtype.contains("xml") ||
                subtype.contains("html")
            ) //
                return true
            return false
        }
    }
}