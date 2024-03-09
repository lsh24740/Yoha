package com.gooey.network.net

import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.EncryptUtils
import com.gooey.network.net.convert.JsonConverterFactory
import com.gooey.network.net.convert.MoshiDecodeConverterFactory
import com.gooey.network.net.decoder.EncryptDecoder
import com.gooey.network.net.interceptor.HeaderInterceptor
import com.gooey.network.net.interceptor.HttpLoggingInterceptor
import com.gooey.network.network.retrofit.getBaseMoshi
import com.gooey.network.network.retrofit.toJson
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import java.util.Date
import java.util.Locale
import java.util.TreeMap

/**
 *@author lishihui01
 *@Date 2023/8/29
 *@Describe:
 */
object GooeyRetrofit {

    private var moshi: Moshi? = null
    private val DEBUG: Retrofit by lazy {
        val rawClient = NetworkConfig.okHttpClient
        val builder = rawClient.newBuilder()
        val loggingInterceptor = HttpLoggingInterceptor("Gooey_Http")
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        builder.addInterceptor(HeaderInterceptor())
        builder.addNetworkInterceptor(loggingInterceptor) //addNetworkInterceptor 可以打印出所有header
        val baseUrl: String = GooeyDomainConfig.apiBaseUrl()
        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JsonConverterFactory())
                .addConverterFactory(MoshiDecodeConverterFactory(errorCodeDispatcher = errorCodeDispatcher))
                .client(builder.build())
                .build()
        retrofit
    }
    private var _errorCodeDispatcher: IErrorCodeDispatcher? = null

    private val errorCodeDispatcher = object : IErrorCodeDispatcher {
        override fun dispatch(errorCode: Int, msg: String) {
            _errorCodeDispatcher?.dispatch(errorCode, msg)
        }
    }

    private val RELEASE: Retrofit by lazy {
        val rawClient = NetworkConfig.okHttpClient
        val builder = rawClient.newBuilder()
        builder.addInterceptor(HeaderInterceptor())
        val baseUrl: String = GooeyDomainConfig.apiBaseUrl()
        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JsonConverterFactory(EncryptDecoder()))
                .addConverterFactory(MoshiDecodeConverterFactory(EncryptDecoder(), errorCodeDispatcher = errorCodeDispatcher))
                .client(builder.build())
                .build()
        retrofit
    }

    fun setErrorCodeDispatcher(dispatcher: IErrorCodeDispatcher) {
        _errorCodeDispatcher = dispatcher
    }

    fun getRetrofit(): Retrofit {
        return if (AppUtils.isAppDebug()) {
            return DEBUG
        } else {
            RELEASE
        }
    }

    fun getBody(map: Map<String, Any>? = null): Map<String, Any> {
        val newMap: MutableMap<String, Any> = if (map.isNullOrEmpty()) {
            LinkedHashMap()
        } else {
            LinkedHashMap(map)
        }
        val date = Date()
        val secondTime: Number = (date.time / 1000).toInt()
        val versionName: String = AppUtils.getAppVersionName()
        newMap["app_channel"] = "100002"
        newMap["timestamp"] = secondTime
        newMap["platform"] = "android"
        newMap["app_version"] = versionName
        newMap["app_key"] = "100002"
        newMap["language"] = Locale.getDefault().language
        val stringBuffer = StringBuilder()
        val treeMap = TreeMap(newMap)
        treeMap.forEach {
            if (it.value is List<*>) {
                stringBuffer.append(it.key).append("=").append(toJson(getMoshi(), it.value))
            } else {
                stringBuffer.append(it.key).append("=").append(it.value)
            }
        }
        stringBuffer.append("xLJliqI4CRG1gmgytvm9dDXngS0NiWZ2")
        val md5: String = EncryptUtils.encryptMD5ToString(stringBuffer.toString()).lowercase()
        treeMap["sign"] = md5
        return treeMap
    }

    fun getMoshi(): Moshi {
        if (moshi == null) {
            moshi = getBaseMoshi()
        }
        return moshi!!
    }

    inline fun <reified S> Retrofit.serviceCreate(): S {
        return create(S::class.java)
    }


}