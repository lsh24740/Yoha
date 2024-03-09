package com.gooey.network.datasource

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
/**
 *@author lishihui01
 *@Date 2023/8/29
 *@Describe:
 */
open class SequenceDataSource2<P, R>(scope: CoroutineScope) : ScopeDataSource2(scope) {
    val mediator: MediatorLiveData<ParamResource<P, R>> by lazy {
        MediatorLiveData<ParamResource<P, R>>()
    }
    private var lastRequest: LiveData<ParamResource<P, R>>? = null

    fun loadSequent(request: () -> LiveData<ParamResource<P, R>>): LiveData<ParamResource<P, R>> {
        lastRequest?.let {
            mediator.removeSource(it)
        }
        lastRequest = request.invoke()
        lastRequest?.let {
            mediator.addSource(it) { data ->
                mediator.value = data
            }
        }

        return lastRequest!!
    }

    fun load(
        param: P,
        log: String = "",
        remote: suspend (P) -> ApiResult<R>
    ): LiveData<ParamResource<P, R>> {
        return loadSequent {
            loadRemote(param, log, remote)
        }
    }

    fun load(
        param: P,
        log: String = "",
        shouldFetch: ((R?) -> Boolean)? = null,
        saver: (suspend (P, R) -> Unit)? = null,
        local: suspend (P) -> ApiResult<R>,
        remote: suspend (P) -> ApiResult<R>
    ): LiveData<ParamResource<P, R>> {
        return loadSequent {
            loadCached(param, log, shouldFetch, saver, local, remote)
        }
    }

    protected suspend fun call(
        param: P,
        remote: suspend (P) -> ApiResult<R>
    ): ApiResult<R> {

        val result = runCatching {
            remote.invoke(param)
        }.getOrElse {
            mediator.postValue(ParamResource.error(param, error = it))
            throw it
        }

        mediator.postValue(
            if (result.isSuccess()) {
                ParamResource.success(param, result.data)
            } else {
                ParamResource.error<P, R>(
                    param, result.data, result.exception,
                    result.code, result.msg
                )
            }
        )

        return result
    }

    fun observe(owner: LifecycleOwner, observer: Observer<ParamResource<P, R>>) {
        mediator.observe(owner, observer)
    }

    fun removeObserver(observer: Observer<ParamResource<P, R>>) {
        mediator.removeObserver(observer)
    }

    fun observeForever(observer: Observer<ParamResource<P, R>>) {
        mediator.observeForever(observer)
    }
}

fun <P, T> simpleLiveData2(
    param: P,
    scope: CoroutineScope,
    remote: suspend (P) -> ParamResource<P, T>
): LiveData<ParamResource<P, T>> =
    liveData(Dispatchers.IO + scope.coroutineContext) {
        emit(ParamResource.loading<P, T>(param))
        try {
            val ret = remote.invoke(param)
            emit(ret)
        } catch (e: Throwable) {
            e.printStackTrace()
            emit(ParamResource.error<P, T>(param, error = e))
        }
    }
