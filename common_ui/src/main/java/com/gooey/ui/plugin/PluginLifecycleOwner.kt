package com.gooey.ui.plugin

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
/**
 * @author lishihui
 */
class PluginLifecycleOwner(var cached: Boolean,
                           val owner: LifecycleOwner)
    : LifecycleOwner, IPlugin<Any> {
    private val ob = LifecycleEventObserver { _, event ->
        lifecycleRegistry.handleLifecycleEvent(event)
    }

    private val lifecycleRegistry = LifecycleRegistry(this)

    override fun plugin(info: Any?) {
        owner.lifecycle.addObserver(ob)
    }

    override fun plugout(info: Any?) {
        owner.lifecycle.removeObserver(ob)
        lifecycleRegistry.handleLifecycleEvent(
                if (cached) Lifecycle.Event.ON_STOP else Lifecycle.Event.ON_DESTROY)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

}