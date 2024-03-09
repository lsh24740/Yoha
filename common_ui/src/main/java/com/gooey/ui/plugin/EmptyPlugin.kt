package com.gooey.ui.plugin

/**
 * @author lishihui
 */
class EmptyPlugin<T> : IPlugin<T> {
    override fun plugin(info: T?) {}
    override fun plugout(info: T?) {}
}