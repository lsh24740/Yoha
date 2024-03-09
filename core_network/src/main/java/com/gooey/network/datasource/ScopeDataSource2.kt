package com.gooey.network.datasource

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.*
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
/**
 *@author lishihui01
 *@Date 2023/8/29
 *@Describe:
 */
open class ScopeDataSource2(scope: CoroutineScope) : ScopeDataSource(scope) {
    fun <P, R> loadFlow(
        param: P,
        log: String = "",
        block: suspend FlowCollector<ApiResult<R>>.(P) -> Unit
    ): Flow<ParamResource<P, R>> {
        var start = 0L
        if (log.isNotEmpty() && AppUtils.isAppDebug()) {
            LogUtils.dTag("DataSource", "---load\t $log, p=$param")
        }
        return flow {
            block.invoke(this, param)
        }.transform {
            emit(
                if (it.isSuccess()) {
                    ParamResource.success(param, it.data)
                } else {
                    ParamResource.error<P, R>(
                        param, it.data, it.exception,
                        it.code, it.msg
                    )
                }
            )
        }.catch {
            emit(ParamResource.error(param, error = it))
        }.onStart {
            if (log.isNotEmpty() && AppUtils.isAppDebug()) {
                start = SystemClock.elapsedRealtime()
                LogUtils.dTag("DataSource", ">>>start\t $log, p=$param, start=$start")
            }
            emit(ParamResource.loading(param))
        }.onEach {
            if (it.isError()) {
                it.error?.printStackTrace()
            }
        }.onCompletion {
            if (log.isNotEmpty() && AppUtils.isAppDebug()) {
                val now = SystemClock.elapsedRealtime()
                LogUtils.dTag("DataSource", "<<<end\t $log, p=$param, cost=${now - start}ms")
            }
            it?.printStackTrace()
        }
    }

    fun <P, R> loadRemote(
        param: P,
        log: String = "",
        remote: suspend (P) -> ApiResult<R>
    ): LiveData<ParamResource<P, R>> {
        return loadFlow(param, log) {
            emit(remote.invoke(it))
        }.asLiveData(scope.coroutineContext + Dispatchers.IO)
    }

    fun <P, R> loadCached(
        param: P,
        log: String = "",
        shouldFetch: ((R?) -> Boolean)? = null,
        saver: (suspend (P, R) -> Unit)? = null,
        local: suspend (P) -> ApiResult<R>,
        remote: suspend (P) -> ApiResult<R>
    ): LiveData<ParamResource<P, R>> {
        return loadFlow<P, R>(param, log) {
            val cached = local.invoke(param)
            if (cached.isSuccess()) {
                emit(cached)
            }
            if (shouldFetch?.invoke(cached.data) != false) {
                val fetched = remote.invoke(param)
                fetched.data?.let { data ->
                    scope.launch(Dispatchers.IO) {
                        saver?.invoke(param, data)
                    }
                }
                emit(fetched)
            }
        }.asLiveData(scope.coroutineContext + Dispatchers.IO)
    }
}

suspend fun <T> CoroutineScope.loadAsync(remote: suspend () -> ApiResult<T>): Deferred<ApiResult<T>> {
    return async(Dispatchers.IO) {
        val caught = kotlin.runCatching {
            remote.invoke()
        }
        caught.getOrElse {
            it.printStackTrace()
            val ret: ApiResult<T> = if (it is CMNetworkIOException) {
                ApiResult.exception(it)
            } else {
                ApiResult.exception(CMNetworkIOException(it))
            }
            ret
        }
    }
}