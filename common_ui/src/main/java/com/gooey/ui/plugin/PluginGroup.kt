package com.gooey.ui.plugin

import androidx.annotation.CallSuper
import java.util.*

/**
 * @author lishihui
 */
abstract class PluginGroup<META, CHILD : IPlugin<META>> : SimplePlugin<META>() {
    protected val children: MutableList<CHILD> = ArrayList()
    protected var currentMeta: META? = null

    private var looping = false
    @CallSuper
    override fun onPlugChanged(isPlugin: Boolean, meta: META?) {
        super.onPlugChanged(isPlugin, meta)
        currentMeta = meta
        looping = true
        for (child in children) {
            dispatchPlugEvent(child, isPlugin, meta)
        }
        looping = false
    }

    @CallSuper
    protected fun dispatchPlugEvent(child: CHILD, `in`: Boolean, meta: META?) {
        if (`in`) {
            child.plugin(meta)
        } else {
            child.plugout(meta)
        }
    }

    fun addChild(child: CHILD) {
        if (children.contains(child)) {
            return
        }
        children.add(child)
        if (isPlugin) {
            dispatchPlugEvent(child, true, currentMeta)
        }
    }

    fun removeChild(child: CHILD) {
        if (!children.contains(child)) {
            return
        }
        children.remove(child)
        if (isPlugin) {
            dispatchPlugEvent(child, false, currentMeta)
        }
    }

    fun forEach(action: (CHILD) -> Unit) {
        children.forEach {
            action(it)
        }
    }
}