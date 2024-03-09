package com.gooey.network.datasource

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope

/**
 *@author lishihui01
 *@Date 2023/8/29
 *@Describe:
 */

open class SequenceDataSource<T>(scope: CoroutineScope) : ScopeDataSource(scope) {
    val mediator: MediatorLiveData<T> by lazy {
        MediatorLiveData<T>()
    }
    private var lastRequest: LiveData<T>? = null

    fun loadSequent(request: () -> LiveData<T>): LiveData<T> {
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

    fun observe(owner: LifecycleOwner, observer: Observer<T>) {
        mediator.observe(owner, observer)
    }

    fun removeObserver(observer: Observer<T>) {
        mediator.removeObserver(observer)
    }

    fun observeForever(observer: Observer<T>) {
        mediator.observeForever(observer)
    }
}

fun LiveData<ParamResource<*, *>>.isLoading() : Boolean {
    return value?.isLoading() ?: false
}