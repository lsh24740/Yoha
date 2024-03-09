package com.gooey.network.net.cookie

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 *@author lishihui01
 *@Date 2023/8/31
 *@Describe:
 */
class GooeyCookieJarImpl(cookieStore: GooeyCookieStore?) :
    CookieJar {
    private val cookieStore: GooeyCookieStore

    init {
        requireNotNull(cookieStore) { "cookieStore can not be null!" }
        this.cookieStore = cookieStore
    }

    fun getCookieStore(): GooeyCookieStore {
        return cookieStore
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore.loadCookie(url)
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore.saveCookie(url, cookies)
    }
}