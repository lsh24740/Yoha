package com.gooey.network.datasource

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers

/**
 *@author lishihui01
 *@Date 2023/8/29
 *@Describe:
 */
open class SimpleDataSource {
    open suspend fun <P, R, M> loadDataConvert(param: P?,
                                               remote: suspend (P?) -> ApiResult<M>,
                                               converter: suspend (M?) -> R = { it as R }): ParamResource<P, R> {
        return try {
            val response = remote(param)
            if (response.isSuccess()) {
                val data = response.data
                ParamResource.success(param, converter(data))
            } else {
                ParamResource.error(param, null, response.exception,
                    response.code, response.msg)
            }
        } catch (e: Throwable) {
            ParamResource.error(param, error = e)
        }
    }

    open suspend fun <P, R> loadData(param: P?,
                                     remote: suspend (P?) -> ApiResult<R>): ParamResource<P, R> {
        return loadDataConvert(param, remote)
    }
}

fun <P, T> simpleLiveData(param: P, remote: suspend (P) -> ParamResource<P, T>): LiveData<ParamResource<P, T>> =
    liveData(Dispatchers.IO) {
        emit(ParamResource.loading(param))
        try {
            val ret = remote.invoke(param)
            emit(ret)
        } catch (e: Throwable) {
            e.printStackTrace()
            emit(ParamResource.error(param, error = e))
        }
    }