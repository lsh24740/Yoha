package com.gooey.network.net.convert

import com.gooey.network.datasource.ApiResult
import com.gooey.network.net.decoder.IEncryptDecoder
import com.gooey.network.net.decoder.NoEncryptDecoder
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


/**
 *@author lishihui01
 *@Date 2023/8/31
 *@Describe:转换成json字符串
 */

class JsonConverterFactory(private var decoder: IEncryptDecoder = NoEncryptDecoder()) :
    Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        if (type is ParameterizedType) {
            val rawType = type.rawType
            if (rawType === ApiResult::class.java) {
                val actualTypes = type.actualTypeArguments
                if (actualTypes.size == 1 && actualTypes[0] === JSONObject::class.java) {
                    return JsonObjectConverter(decoder)
                }
            }
        }
        return null
    }
}

class JsonObjectConverter(private val decoder: IEncryptDecoder) :
    Converter<ResponseBody, ApiResult<JSONObject>> {
    @Throws(IOException::class, JSONException::class)
    override fun convert(value: ResponseBody): ApiResult<JSONObject>? {
        val jsonObject = JSONObject(decoder.decode(value))
        val code = jsonObject.optInt("code")
        val message = jsonObject.optString("msg")
        return ApiResult(code, message, data = jsonObject)
    }
}
