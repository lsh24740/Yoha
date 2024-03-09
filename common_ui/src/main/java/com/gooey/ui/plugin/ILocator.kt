package com.gooey.ui.plugin

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
/**
 * @author lishihui
 */
interface ILocator {
    fun getParent(): ViewGroup
    fun locate(view: View) {}
}

open class Locator<T : ViewGroup>(@JvmField protected val p: T) : ILocator {
    override fun getParent(): ViewGroup = p

    override fun locate(view: View) {
        p.addView(view)
    }
}

abstract class FakeLocator<T : ViewGroup>(p: T) : Locator<T>(p) {
    abstract fun fake(): T
}

open class ConstraintLocator(parent: ConstraintLayout) : FakeLocator<ConstraintLayout>(parent) {
    override fun fake(): ConstraintLayout = ConstraintLayout(p.context)
}
