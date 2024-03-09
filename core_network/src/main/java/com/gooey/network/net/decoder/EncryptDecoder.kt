package com.gooey.network.net.decoder

import android.util.Base64
import okhttp3.ResponseBody
import java.nio.charset.Charset
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

/**
 *@author lishihui01
 *@Date 2023/10/28
 *@Describe:
 */
class EncryptDecoder : IEncryptDecoder {
    companion object {
        /*秘钥*/
        val SECRET_KEY = "fiMRKj78"
    }

    override fun decodeData(encryptData: ByteArray): Int {
        return encryptData.size
    }

    override fun decode(body: ResponseBody): String {
        return decrypt(body.string())
    }

    /**
     * DES解密
     *
     * @param ciphertext 密文
     * @return 明文
     */
    private fun decrypt(ciphertext: String): String {
        return try {
            val byteContent = Base64.decode(ciphertext, Base64.DEFAULT)
            val desKey = DESKeySpec(SECRET_KEY.toByteArray())
            val keyFactory = SecretKeyFactory.getInstance("DES")
            val secureKey = keyFactory.generateSecret(desKey)
            val cipher = Cipher.getInstance("DES/ECB/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, secureKey, SecureRandom())
            String(cipher.doFinal(byteContent), Charset.forName("UTF-8"))
        } catch (e: Exception) {
            e.printStackTrace()
            ciphertext
        }
    }
}