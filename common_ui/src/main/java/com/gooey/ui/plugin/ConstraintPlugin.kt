package com.gooey.ui.plugin

import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner

/**
 * @author lishihui
 * 专门提供给 constraint layout使用
 * 生成一个 space，locator 来制定这个 space 和 直播间最外层约束布局的关系
 * 然后把 plugin 里面 inflate 出来的内容里面的parent 都替换成 space
 *
 * 这样做的目的是
 *  1. xml 里面ConstraintLayout 的约束可以直接写成 parent，不需要关心在外层的 xml 文件里面的一些位置约束
 */

abstract class ConstraintPlugin<BINDING : ViewDataBinding, T>(val locator: ConstraintLocator,
                                                              input: LifecycleOwner,
                                                              timeout: Long = 0) :
        IdlePlugin<T>(ViewWrapper(locator.getParent()), timeout, input) {
    protected val owner = getPluginLifecycleOwner(input)
    protected var binding: BINDING? = null
    var group: GroupSpace? = null

    override fun render(meta: T, plugin: Boolean) { }

    override fun initViewIfNeeded() {
        if (binding != null) {
            val p = locator.getParent()
            p.addView(group)
            group?.addAllViews()
            return
        }

        val context = locator.getParent().context
        val space = GroupSpace(context)
        val id = View.generateViewId()
        space.id = id
        locator.locate(space) // 实际上位置指定给了space

        // 替换space的 id
        //  1. 获取binding （？如何获取
        val fakeContainer = locator.fake()
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), inflateLayout(), fakeContainer, true)
        //  2. inflate layout到一个fake的 ConstraintLayout
        //  3. fake 里面 view 的 layoutparams，约束关系id是0的全部改成 space 的id
        //  4， 取出fake里面的view，放到外层的 ConstraintLayout 里
        val count = fakeContainer.childCount
        val list = mutableListOf<View>()
        var index = 0
        while (index < count) {
            val child = fakeContainer.getChildAt(index)
            list.add(child)
            index++
        }
        fakeContainer.removeAllViews()
        space.addViews(list)
        group = space
        onViewCreated(binding!!)
    }

    override fun destroyViewIfNeeded() {
        if (binding == null) {
            return
        }
        val p = locator.getParent()
        group?.removeAllViews()
        p.removeView(group)
        if (!cached) {
            onViewDestroyed(binding!!)
            group = null
            binding = null
        }
    }

    override fun toggleOldView(show: Boolean) {}

    override fun toggleNewView(show: Boolean) {}

    open fun onViewCreated(binding: BINDING) {}

    open fun onViewDestroyed(binding: BINDING) {}

    @LayoutRes
    abstract fun inflateLayout(): Int
}