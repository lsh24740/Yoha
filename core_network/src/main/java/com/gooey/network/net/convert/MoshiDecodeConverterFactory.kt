package com.gooey.network.net.convert

import android.text.TextUtils
import com.gooey.network.datasource.CMNetworkIOException
import com.gooey.network.net.IErrorCodeDispatcher
import com.gooey.network.net.decoder.IEncryptDecoder
import com.gooey.network.net.decoder.NoEncryptDecoder
import com.gooey.network.network.retrofit.getBaseMoshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.internal.Util
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.ByteString.Companion.decodeHex
import okio.buffer
import okio.source
import org.json.JSONObject
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.ByteArrayInputStream
import java.io.IOException
import java.lang.reflect.Type

/**
 *@author lishihui01
 *@Date 2023/8/31
 *@Describe:后面数据支持解密
 */
class MoshiDecodeConverterFactory(
        private var decoder: IEncryptDecoder = NoEncryptDecoder(),
        private val moshi: Moshi = getBaseMoshi(),
        private val errorCodeDispatcher: IErrorCodeDispatcher? = null
) : Converter.Factory() {

    private val factory: MoshiConverterFactory = MoshiConverterFactory.create(moshi)

    override fun responseBodyConverter(
            type: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        return MoshiDecoderConverter(moshi.adapter(type, Util.jsonAnnotations(annotations)), decoder)
    }


    override fun requestBodyConverter(
            type: Type,
            parameterAnnotations: Array<Annotation>,
            methodAnnotations: Array<Annotation>,
            retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        return factory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
    }

    internal inner class MoshiDecoderConverter(
            private val adapter: JsonAdapter<Any>,
            private val decoder: IEncryptDecoder
    ) :
            Converter<ResponseBody, Any> {
        private val UTF8_BOM = "EFBBBF".decodeHex()

        @Throws(IOException::class)
        override fun convert(value: ResponseBody): Any {
            val result = decoder.decode(value)
            if (errorCodeDispatcher != null) {
                if (!TextUtils.isEmpty(result)) {
                    try {
                        val jsonObject = JSONObject(result)
                        if (jsonObject.optInt("code") != 1) {

                        }
                    } catch (e: Exception) {

                    }
                }
            }
            return convertResponse(result, value)
        }

        private fun convertResponse(json: String, value: ResponseBody): Any {
            val source = ByteArrayInputStream(json.toByteArray()).source().buffer()
            return value.use {
                // Moshi has no document-level API so the responsibility of BOM skipping falls to whatever
                // is delegating to it. Since it's a UTF-8-only library as well we only honor the UTF-8 BOM.
                if (source.rangeEquals(0, UTF8_BOM)) {
                    source.skip(UTF8_BOM.size.toLong())
                }
                val reader = JsonReader.of(source)
                val result: Any? = adapter.fromJson(reader)
                if (reader.peek() != JsonReader.Token.END_DOCUMENT) {
                    throw JsonDataException("JSON document was not fully consumed.")
                }
                if (result == null) {
                    throw CMNetworkIOException(JsonDataException("JSON DATA NULL"))
                }
                result
            }

        }

    }
}