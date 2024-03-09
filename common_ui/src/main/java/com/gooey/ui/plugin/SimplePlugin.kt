package com.gooey.ui.plugin

import androidx.annotation.CallSuper
import androidx.lifecycle.LifecycleOwner
/**
 * @author lishihui
 */
@Deprecated(message = "use BetterPlugin")
abstract class SimplePlugin<META> : IPlugin<META> {
    var listener: IPluginListener? = null
    var blocker: IPluginBlocker<META>? = null
    var isPlugin = false
    var cached = false
        set(value) {
            if (field != value) {
                field = value
                lifecycle?.cached = value
                onCacheChanged(value)
            }
        }
    protected var lifecycle: PluginLifecycleOwner? = null
    override fun plugin(info: META?) {
        if (blocker?.onPlugin(true, info) == true) {
            return
        }
        if (isPlugin) {
            if (info != null) {
                render(info, false)
            }
            onPlugChanged(true, info)
            return
        }
        isPlugin = true
        initViewIfNeeded()
        toggleOldView(false)
        toggleNewView(true)
        if (info != null) {
            render(info, true)
        }
        onPlugChanged(true, info)
        lifecycle?.plugin(null)
    }

    override fun plugout(info: META?) {
        if (blocker?.onPlugin(false, info) == true) {
            return
        }
        isPlugin = false
        toggleOldView(true)
        toggleNewView(false)
        destroyViewIfNeeded()
        onPlugChanged(false, info)
        lifecycle?.plugout(null)
        if (!cached) {
            lifecycle = null
        }
    }

    @CallSuper
    protected open fun onPlugChanged(isPlugin: Boolean, meta: META?) {
        listener?.onPlugChanged(isPlugin)
    }

    open fun onCacheChanged(cached: Boolean) {}

    open fun render(meta: META, plugin: Boolean) {}

    protected open fun initViewIfNeeded() {}

    protected open fun destroyViewIfNeeded() {}

    protected open fun toggleOldView(show: Boolean) {}

    protected open fun toggleNewView(show: Boolean) {}

    fun getPluginLifecycleOwner(owner: LifecycleOwner): LifecycleOwner {
        if (lifecycle == null) {
            lifecycle = PluginLifecycleOwner(cached, owner)
            if (isPlugin) {
                lifecycle?.plugin(null)
            }
        }
        return lifecycle!!
    }
}