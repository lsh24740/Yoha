package com.gooey.network.net.decoder

import okhttp3.ResponseBody

/**
 *@author lishihui01
 *@Date 2023/8/31
 *@Describe:
 */
class NoEncryptDecoder : IEncryptDecoder {

    override fun decodeData(encryptData: ByteArray): Int {
        return encryptData.size
    }

    override fun decode(body: ResponseBody): String {
        return body.string()
    }
}