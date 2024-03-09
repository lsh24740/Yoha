package com.gooey.ui.plugin

import android.content.Context
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.NMDataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * @author lishihui
 * 热拔插UI组件
 */
abstract class BetterPlugin<BINDING : ViewDataBinding, T>(
    locator: ILocator,
    input: LifecycleOwner,
    timeout: Long = 0,
    private val merge: Boolean = true
) : BindingPlugin<BINDING, T>(locator, input, timeout) {
    var group: GroupSpace? = null
    var fake: ViewGroup? = null
    protected var relocate = true
    override var changeConfig = CHANGE_VISIBLE

    override fun inflateView(): CachedResult<BINDING> {
        if (!merge) {
            return super.inflateView()
        }
        val context = locator.getParent().context
        var fakeContainer: ViewGroup? = null
        if (locator is FakeLocator<*>) {
            fakeContainer = (locator as FakeLocator<*>).fake()
        } else {
            val p = locator.getParent()
            try {
                val constructor = p.javaClass.getConstructor(Context::class.java, AttributeSet::class.java)
                fakeContainer = constructor.newInstance(p.context, null)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            if (fakeContainer == null) {
                try {
                    val constructor = p.javaClass.getConstructor(Context::class.java)
                    fakeContainer = constructor.newInstance(p.context)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
        return if (fakeContainer == null) {
            super.inflateView()
        } else {
            val localBinding: BINDING =
                    if (Looper.getMainLooper().thread == Thread.currentThread()) {
                        DataBindingUtil.inflate(
                                LayoutInflater.from(context), inflateLayout(), fakeContainer, true)
                    } else {
                        LayoutInflater.from(context).inflate(inflateLayout(), fakeContainer, true)
                        val count = fakeContainer.childCount
                        runBlocking(Dispatchers.Main) {
                            val b: BINDING = if (count > 1) {
                                val list = mutableListOf<View>()
                                var index = 0
                                while (index < count) {
                                    val child = fakeContainer.getChildAt(index)
                                    list.add(child)
                                    index++
                                }
                                NMDataBindingUtil.bind(list.toTypedArray(), inflateLayout())
                            } else {
                                DataBindingUtil.bind(fakeContainer.getChildAt(0))!!
                            }
                            b
                        }
                    }
            CachedResult(localBinding).apply {
                parent = fakeContainer
            }
        }
    }

    override fun initViewInternal(cache: CachedResult<BINDING>?) {
        if (!merge) {
            super.initViewInternal(cache)
            return
        }
        val tmp = binding
        if (tmp != null) {
            val localGroup = group
            val localFake = fake
            when {
                localGroup != null -> {
                    var parent = localGroup.parent
                    if (parent is ViewGroup && parent != locator.getParent()) {
                        localGroup.removeAllViews()
                        parent.removeView(localGroup)
                        parent = null
                    }
                    if (parent == null) {
                        locator.locate(localGroup)
                        localGroup.addAllViews(relocate)
                    }
                }
                localFake != null -> {
                    var parent = localFake.parent
                    if (parent is ViewGroup && parent != locator.getParent()) {
                        parent.removeView(localFake)
                        parent = null
                    }
                    if (parent == null) {
                        locator.locate(localFake)
                    }
                }
                else -> {
                    var parent = tmp.root.parent
                    if (parent is ViewGroup && parent != locator.getParent()) {
                        parent.removeView(tmp.root)
                        parent = null
                    }
                    if (parent == null) {
                        locator.locate(tmp.root)
                    }
                }
            }
            return
        }
        val result = cache ?: inflateView()
        var fakeContainer = result.parent
        if (fakeContainer == null) {
            super.initViewInternal(result)
            return
        }
        val context = locator.getParent().context
        val localBinding = result.binding!!
        binding = localBinding
        var count = fakeContainer.childCount
        // 分三种情况：
        // 1. 非merge，root不是ConstraintLayout，不处理，直接走添加流程
        // 2. 非merge，root是ConstraintLayout，并且parent也是ConstraintLayout，则减少层级
        // 3. 是merge，则减少层级
        val bindingRoot = localBinding.root
        var id = View.generateViewId()
        if (count == 1) {
            if (bindingRoot is ViewGroup) {
                if (bindingRoot !is ConstraintLayout
                        || fakeContainer !is ConstraintLayout
                ) {
                    fakeContainer.removeAllViews()
                    locator.locate(localBinding.root)
                    doOnViewCreated(localBinding)
                    return
                } else {
                    fakeContainer.removeAllViews()
                    fakeContainer = bindingRoot
                    if (fakeContainer.id != 0) {
                        id = fakeContainer.id
                    }
                }
            } else {
                fakeContainer.removeAllViews()
                locator.locate(localBinding.root)
                doOnViewCreated(localBinding)
                return
            }
        }
        val space = GroupSpace(context)

        if (fakeContainer.layoutParams != null) {
            space.layoutParams = fakeContainer.layoutParams
        }
        space.background = fakeContainer.background
        space.id = id
        locator.locate(space)
        val lp = space.layoutParams
        if (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT
                || lp.height == ViewGroup.LayoutParams.WRAP_CONTENT
        ) {
            val p = space.parent as? ViewGroup
            p?.removeView(space)
            fakeContainer.id = id
            locator.locate(fakeContainer)
            fake = fakeContainer
            doOnViewCreated(localBinding)
            return
        }
        val list = mutableListOf<View>()
        var index = 0
        count = fakeContainer.childCount
        while (index < count) {
            val child = fakeContainer.getChildAt(index)
            list.add(child)
            index++
        }
        list.add(fakeContainer)
        fakeContainer.id = View.generateViewId()
        fakeContainer.layoutParams = ConstraintLayout.LayoutParams(0, 0)
        fakeContainer.isVisible = false
        fakeContainer.removeAllViews()
        space.addViews(list, relocate)
        group = space
        doOnViewCreated(localBinding)
    }

    override fun destroyViewIfNeeded() {
        val local = binding ?: return
        val p = locator.getParent()
        when {
            fake != null -> {
                p.removeView(fake)
            }
            group != null -> {
                group?.removeAllViews()
                p.removeView(group)
            }
            else -> {
                p.removeView(local.root)
            }
        }
        if (!cached) {
            onViewDestroyed(local)
            group = null
            fake = null
            binding = null
        }
    }

    override fun onVisibleChanged(isPlugin: Boolean, meta: T?) {
        if (fake == null && group == null) {
            super.onVisibleChanged(isPlugin, meta)
        }
        when (changeConfig) {
            CHANGE_VISIBLE -> {
                when {
                    fake != null -> {
                        fake?.isVisible = isPlugin
                    }
                    group != null -> {
                        if (group?.parent != null) {
                            if (isPlugin) {
                                group?.removeAllViews()
                                group?.addAllViews(relocate)
                            } else {
                                group?.removeAllViews()
                            }
                            group?.isVisible = isPlugin
                        }
                    }
                }
            }
            CHANGE_REMOVE -> {
                when {
                    fake != null -> {
                        if (isPlugin) {
                            val current = fake?.parent
                            if (current is ViewGroup) {
                                current.removeView(fake)
                            }
                            fake?.apply {
                                locator.locate(this)
                            }
                        } else {
                            val p = fake?.parent
                            if (p is ViewGroup) {
                                p.removeView(fake)
                            }
                        }
                    }
                    group != null -> {
                        if (isPlugin) {
                            val current = group?.parent
                            if (current is ViewGroup) {
                                group?.removeAllViews()
                                current.removeView(group)
                            }
                            group?.apply {
                                locator.locate(this)
                                group?.addAllViews(relocate)
                            }
                        } else {
                            val p = group?.parent
                            if (p is ViewGroup) {
                                group?.removeAllViews()
                                p.removeView(group)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getRootView(): View? {
        return when {
            fake != null -> {
                fake
            }
            group != null -> {
                group
            }
            else -> {
                super.getRootView()
            }
        }
    }
}