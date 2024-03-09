package com.gooey.common.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * Created by lishihui.
 */

class ForegroundLifecycleOwner(private val owner: LifecycleOwner) : LifecycleOwner {
    private val lifecycleRegistry by lazy {
        val local = LifecycleRegistry(this)
        owner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                        local.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                        local.handleLifecycleEvent(Lifecycle.Event.ON_START)
                    }
                    Lifecycle.Event.ON_RESUME, Lifecycle.Event.ON_PAUSE -> {
                        local.handleLifecycleEvent(event)
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        local.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                        local.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    }
                    else -> {
                    }
                }
            }
        })
        local
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}