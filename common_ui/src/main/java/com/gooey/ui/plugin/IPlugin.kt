
package com.gooey.ui.plugin

import android.view.View

/**
 * @author lishihui
 */
interface IPlugin<T> {
    fun plugin(info : T?)

    fun plugout(info : T?)

    fun getRootView(): View? = null
}

fun <T> IPlugin<T>.toggle(show: Boolean, info: T? = null) {
    if (show) {
        plugin(info)
    } else {
        plugout(info)
    }
}