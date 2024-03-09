package com.gooey.network.datasource

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 *@author lishihui01
 *@Date 2023/9/23
 *@Describe:
 */
fun <P, R> LiveData<ParamResource<P, R>>.observerOnce(observer: Observer<ParamResource<P, R>>) {
    this.observeForever(object : Observer<ParamResource<P, R>> {
        override fun onChanged(p: ParamResource<P, R>) {
            observer.onChanged(p)
            if (p.isSuccess() || p.isError()) {
                removeObserver(this)
            }
        }
    })
}