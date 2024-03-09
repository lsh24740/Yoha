package com.gooey.ui.plugin

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*

/**
 * @author lishihui
 */
@Deprecated(message = "use BetterPlugin")
abstract class BindingPlugin<BINDING : ViewDataBinding, T>(
        var locator: ILocator,
        private val input: LifecycleOwner,
        timeout: Long = 0,
        handler: IPluginHandler? = null)
    : IdlePlugin<T>(handler ?: HandlerWrapper(Handler(Looper.getMainLooper())), timeout, input) {
    var async = false
    protected open var changeConfig = CHANGE_NONE
    protected var owner = getPluginLifecycleOwner(input)
    protected var binding: BINDING? = null
    protected var job: Job? = null
    protected var jobCanceled = false
    protected var mMeta: T? = null
    private var delayPlugin: Runnable? = null
    private var delayPlugout: Runnable? = null


    @CallSuper
    override fun render(meta: T, plugin: Boolean) {
        mMeta = meta
    }

    override fun plugin(info: T?) {
        if (isPlugin || !async || binding != null) {
            super.plugin(info)
        } else {
            if (blocker?.onPlugin(true, info) == true) {
                return
            }
            if (job != null) {
                return
            }
            jobCanceled = false
            job = GlobalScope.launch(Dispatchers.IO) {
                val cache = kotlin.runCatching {
                    val ret = inflateView()
                    ret
                }
                cache.exceptionOrNull()?.printStackTrace()
                if (cache.exceptionOrNull() is CancellationException) {
                    return@launch
                }
                if (jobCanceled || destroyed) {
                    return@launch
                }
                launch(Dispatchers.Main) {
                    if (jobCanceled || destroyed) {
                        return@launch
                    }
                    pluginWithCache(info, cache.getOrNull())
                }
            }
        }
    }

    private fun pluginWithCache(info: T?, cache: CachedResult<BINDING>?) {
        isPlugin = true
        initViewInternal(cache)
        toggleOldView(false)
        toggleNewView(true)
        if (info != null) {
            render(info, true)
        }
        onPlugChanged(true, info)
        lifecycle?.plugin(null)
    }

    override fun initViewIfNeeded() {
        initViewInternal(null)
    }

    open fun initViewInternal(cache: CachedResult<BINDING>?) {
        val tmp = binding
        if (tmp != null) {
            if (tmp.root.parent == null) {
                locator.locate(tmp.root)
            }
            return
        }
        val local: BINDING = cache?.binding ?: inflateView().binding!!
        locator.locate(local.root)
        binding = local
        doOnViewCreated(local)
    }

    open fun inflateView(): CachedResult<BINDING> {
        val p = locator.getParent()
        val local: BINDING = if (Looper.getMainLooper().thread == Thread.currentThread()) {
            DataBindingUtil.inflate(LayoutInflater.from(p.context), inflateLayout(), p, false)
        } else {
            val view = LayoutInflater.from(p.context).inflate(inflateLayout(), p, false)
            runBlocking(Dispatchers.Main) {
                val b: BINDING = DataBindingUtil.bind(view)!!
                b
            }
        }
        return CachedResult(local)
    }

    override fun getRootView(): View? {
        return binding?.root
    }

    override fun destroyViewIfNeeded() {
        val local = binding ?: return
        val p = locator.getParent()
        p.removeView(local.root)
        if (!cached) {
            onViewDestroyed(local)
            binding = null
        }
    }

    @CallSuper
    override fun onPlugChanged(isPlugin: Boolean, meta: T?) {
        super.onPlugChanged(isPlugin, meta)
        if (!isPlugin) {
            job?.cancel()
            jobCanceled = true
        }
        delayPlugin?.apply {
            handler.removeCallbacks(this)
        }
        delayPlugout?.apply {
            handler.removeCallbacks(this)
        }
        job = null
        onVisibleChanged(isPlugin, meta)
    }

    protected open fun onVisibleChanged(isPlugin: Boolean, meta: T?) {
        when (changeConfig) {
            CHANGE_VISIBLE -> binding?.root?.isVisible = isPlugin
            CHANGE_REMOVE -> {
                if (isPlugin) {
                    val current = binding?.root?.parent
                    if (current is ViewGroup) {
                        current.removeView(binding?.root)
                    }
                    binding?.root?.apply {
                        locator.locate(this)
                    }
                } else {
                    val p = binding?.root?.parent
                    if (p is ViewGroup) {
                        p.removeView(binding?.root)
                    }
                }
            }
        }
    }

    override fun plugout(info: T?) {
        super.plugout(info)
        job?.cancel()
        jobCanceled = true
        job = null
    }

    internal fun doOnViewCreated(binding: BINDING) {
        owner = getPluginLifecycleOwner(input)
        binding.lifecycleOwner = owner
        onViewCreated(binding)
    }

    open fun onViewCreated(binding: BINDING) {}

    open fun onViewDestroyed(binding: BINDING) {}

    @LayoutRes
    abstract fun inflateLayout(): Int

    open fun relocat(input: ILocator) {
        if (isPlugin) {
            val c = cached
            cached = true
            destroyViewIfNeeded()
            locator = input
            initViewIfNeeded()
            cached = c
        } else {
            locator = input
        }
    }

    fun plugin(info: T?, delay: Long) {
        if (delay >= 0) {
            delayPlugin?.apply {
                handler.removeCallbacks(this)
            }
            delayPlugin = Runnable {
                super.plugin(info)
            }
            handler.postDelayed(delayPlugin, delay)
        } else {
            super.plugin(info)
        }
    }

    fun plugout(info: T?, delay: Long) {
        if (delay >= 0) {
            delayPlugout?.apply {
                handler.removeCallbacks(this)
            }
            delayPlugout = Runnable {
                super.plugout(info)
            }
            handler.postDelayed(delayPlugout, delay)
        } else {
            super.plugout(info)
        }
    }

    companion object {
        const val CHANGE_NONE = 0
        const val CHANGE_VISIBLE = 1
        const val CHANGE_REMOVE = 2
    }
}

class CachedResult<BINDING : ViewDataBinding>(var binding: BINDING?) {
    var parent: ViewGroup? = null
}
