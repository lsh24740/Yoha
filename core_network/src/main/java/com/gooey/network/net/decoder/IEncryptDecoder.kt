package com.gooey.network.net.decoder

import com.gooey.network.datasource.CMNetworkIOException
import okhttp3.ResponseBody
import java.nio.charset.Charset

/**
 *@author lishihui01
 *@Date 2023/8/31
 *@Describe:
 */
interface IEncryptDecoder {
    @Throws(CMNetworkIOException::class)
    fun decode(body: ResponseBody): String {
        body.use { body ->
            val contentBytes = body.bytes()
            val contentStr: String
            val contentType = body.contentType()
            val charset: Charset = contentType?.charset() ?: Charset.defaultCharset()
            val length = decodeData(contentBytes)
            contentStr = if (length < 0) {
                throw CMNetworkIOException("deserialdata fail")
            } else {
                String(contentBytes, 0, length, charset)
            }
            return contentStr
        }
    }

    fun decodeData(encryptData: ByteArray): Int
}