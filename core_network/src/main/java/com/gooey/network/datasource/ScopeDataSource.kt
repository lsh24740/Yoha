package com.gooey.network.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

/**
 *@author lishihui01
 *@Date 2023/8/29
 *@Describe:
 */
open class ScopeDataSource(protected val scope: CoroutineScope) : SimpleDataSource() {
    suspend fun <T> loadAsync(remote: suspend () -> ApiResult<T>): Deferred<ApiResult<T>> {
        return scope.async(Dispatchers.IO) {
            val caught = kotlin.runCatching {
                remote.invoke()
            }
            caught.getOrElse {
                it.printStackTrace()
                if (it is CMNetworkIOException) {
                    ApiResult.exception(it)
                } else {
                    ApiResult.exception(CMNetworkIOException(it))
                }
            }
        }
    }
}