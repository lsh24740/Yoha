package com.gooey.ui.plugin

/**
 * @author lishihui
 */
interface IPluginListener {
    fun onPlugChanged(plugin: Boolean) {}
}

interface IPluginBlocker<T> {
    fun onPlugin(plugin: Boolean, meta: T?) : Boolean
}