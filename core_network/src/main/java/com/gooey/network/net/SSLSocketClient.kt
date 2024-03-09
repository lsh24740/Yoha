package com.gooey.network.net

import java.io.IOException
import java.io.InputStream
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 *@author lishihui01
 *@Date 2023/8/31
 *@Describe:
 */
object SSLSocketClient {
    fun getSslSocketFactory(): Pair<SSLSocketFactory, X509TrustManager>? {
        return getSslSocketFactoryBase(null, null, null)
    }

    private fun getSslSocketFactoryBase(
        trustManager: X509TrustManager?,
        bksFile: InputStream?,
        password: String?,
        vararg certificates: InputStream
    ): Pair<SSLSocketFactory, X509TrustManager>? {
        return try {
            val keyManagers: Array<KeyManager>? = prepareKeyManager(bksFile, password)
            val trustManagers: Array<TrustManager>? = prepareTrustManager(*certificates)
            val manager: X509TrustManager = trustManager
                ?: if (trustManagers != null) {
                    //然后使用默认的TrustManager
                    chooseTrustManager(trustManagers) ?: UnSafeTrustManager
                } else {
                    //否则使用不安全的TrustManager
                    UnSafeTrustManager
                }
            // 创建TLS类型的SSLContext对象， that uses our TrustManager
            val sslContext = SSLContext.getInstance("TLS")
            // 用上面得到的trustManagers初始化SSLContext，这样sslContext就会信任keyStore中的证书
            // 第一个参数是授权的密钥管理器，用来授权验证，比如授权自签名的证书验证。第二个是被授权的证书管理器，用来验证服务器端的证书
            sslContext.init(keyManagers, arrayOf<TrustManager>(manager), null)
            Pair(sslContext.socketFactory, manager)
        } catch (e: NoSuchAlgorithmException) {
            null
        } catch (e: KeyManagementException) {
            null
        }
    }

    private fun prepareKeyManager(bksFile: InputStream?, password: String?): Array<KeyManager>? {
        try {
            if (bksFile == null || password == null) return null
            val clientKeyStore = KeyStore.getInstance("BKS")
            clientKeyStore.load(bksFile, password.toCharArray())
            val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(clientKeyStore, password.toCharArray())
            return kmf.keyManagers
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun prepareTrustManager(vararg certificates: InputStream): Array<TrustManager>? {
        if (certificates.isEmpty()) return null
        try {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            // 创建一个默认类型的KeyStore，存储我们信任的证书
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null)
            for ((index, certStream) in certificates.withIndex()) {
                val certificateAlias = index.toString()
                // 证书工厂根据证书文件的流生成证书 cert
                val cert = certificateFactory.generateCertificate(certStream)
                // 将 cert 作为可信证书放入到keyStore中
                keyStore.setCertificateEntry(certificateAlias, cert)
                try {
                    certStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            //我们创建一个默认类型的TrustManagerFactory
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            //用我们之前的keyStore实例初始化TrustManagerFactory，这样tmf就会信任keyStore中的证书
            tmf.init(keyStore)
            //通过tmf获取TrustManager数组，TrustManager也会信任keyStore中的证书
            return tmf.trustManagers
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun chooseTrustManager(trustManagers: Array<TrustManager>): X509TrustManager? {
        for (trustManager in trustManagers) {
            if (trustManager is X509TrustManager) {
                return trustManager
            }
        }
        return null
    }

    /**
     * 为了解决客户端不信任服务器数字证书的问题，网络上大部分的解决方案都是让客户端不对证书做任何检查，
     * 这是一种有很大安全漏洞的办法
     */
    private var UnSafeTrustManager: X509TrustManager = object : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }
}