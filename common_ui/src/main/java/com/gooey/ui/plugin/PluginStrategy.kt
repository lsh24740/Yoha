package com.gooey.ui.plugin

import android.os.SystemClock

/**
 * @author lishihui
 */
abstract class PluginStrategy<META, PLUG : IPlugin<META>> : PluginGroup<META, PLUG>() {
    var currentHolder: PLUG? = null
    protected var currentState = -1
    protected var forcePlugin = true // 兼容老逻辑，原来的PluginStrategy不是一个PluginGroup
    private var delay: DelayJob? = null

    open fun updateStatus(status: Int, data: META? = null) {
        val target = statusToType(status)
        if (currentState == target && currentHolder != null) {
            switchTo(currentHolder, obtainInfo(status) ?: data)
        } else {
            currentState = target
            switchTo(initViewHolder(target), obtainInfo(status) ?: data)
        }
    }

    protected open fun statusToType(status: Int): Int {
        return status
    }

    protected abstract fun obtainInfo(status: Int): META?

    protected abstract fun initViewHolder(holderType: Int): PLUG

    protected open fun beforeSwitchingTo(holder: PLUG?, info: META?) {}

    protected open fun afterSwitchingTo(holder: PLUG?, info: META?) {}

    protected fun switchTo(holder: PLUG?, info: META?) {
        beforeSwitchingTo(holder, info)
        if (currentHolder !== holder) {
            if (holder is BindingPlugin<*, *> && holder.async && currentHolder != null) {
                val id = SystemClock.elapsedRealtime()
                val listener = object : IPluginListener {
                    override fun onPlugChanged(plugin: Boolean) {
                        if (plugin) {
                            if (delay?.id == id) {
                                delay?.run()
                                delay = null
                            }
                        }
                        holder.listener = null
                    }
                }
                delay = object : DelayJob(id) {
                    override fun run() {
                        val local = currentHolder
                        if (local != null) {
                            removeChild(local)
                        }
                        if (forcePlugin && !isPlugin) {
                            currentHolder?.plugout(info)
                        }
                        currentHolder = holder
                    }
                }
                holder.listener = listener
                addChild(holder)
                if (forcePlugin && !isPlugin) {
                    holder.plugin(info)
                }
            } else {
                val local = currentHolder
                if (local != null) {
                    removeChild(local)
                }
                if (forcePlugin && !isPlugin) {
                    currentHolder?.plugout(info)
                }
                currentHolder = holder
                if (holder != null) {
                    addChild(holder)
                }
                if (forcePlugin && !isPlugin) {
                    currentHolder?.plugin(info)
                }
            }
        } else {
            if (info != null) {
                if (forcePlugin || isPlugin) {
                    currentHolder?.plugin(info)
                }
            }
        }
        afterSwitchingTo(holder, info)
    }

    open fun destroy() {
        currentHolder?.plugout(null)
        currentHolder = null
    }


}

abstract class DelayJob(val id: Long) : Runnable
