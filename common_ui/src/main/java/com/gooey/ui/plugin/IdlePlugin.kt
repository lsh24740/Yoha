
package com.gooey.ui.plugin

import android.os.Handler
import android.os.SystemClock
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

const val TIMEOUT_NEVER = -2L
/**
 * @author lishihui
 */
@Deprecated(message = "use BetterPlugin")
abstract class IdlePlugin<META>(
    inputHandler: IPluginHandler?,
    private val timeout: Long,
    owner: LifecycleOwner? = null
) : SimplePlugin<META>() {
    protected var destroyed = false
    private var lastPlugoutStack: Throwable? = null
    private var lastDestroyTime = 0L

    init {
        owner?.lifecycle?.addObserver(LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                if (timeout > 0) {
                    handler.removeCallbacks(runnable)
                }
                destroyed = true
                val ret = kotlin.runCatching {
                    cached = false
                    destroyViewIfNeeded()
                }
                kotlin.runCatching {
                    lifecycle?.plugout(null)
                }
                isPlugin = false
            }
        })
    }

    protected val handler: IPluginHandler by lazy {
        inputHandler ?: HandlerWrapper(Handler())
    }

    private val runnable = Runnable {
        lastDestroyTime = SystemClock.elapsedRealtime()
        destroyViewIfNeeded()
        kotlin.runCatching {
            lifecycle?.plugout(null)
        }.exceptionOrNull()?.let { e ->
            e.printStackTrace()
        }
        if (!cached) {
            lifecycle = null
        }
    }

    override fun plugout(info: META?) {
        if (blocker?.onPlugin(false, info) == true) {
            return
        }
        if (!isPlugin) {
            return
        }
        isPlugin = false
        toggleOldView(true)
        toggleNewView(false)
        onPlugChanged(false, info)
    }

    override fun onPlugChanged(isPlugin: Boolean, meta: META?) {
        super.onPlugChanged(isPlugin, meta)
        if (timeout > 0) {
            if (isPlugin) {
                handler.removeCallbacks(runnable)
            } else {
                lastPlugoutStack = Throwable()
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, timeout)
            }
        } else {
            if (!isPlugin && timeout != TIMEOUT_NEVER) {
                runnable.run()
            }
        }
    }

    override fun toggleOldView(show: Boolean) {}

    override fun toggleNewView(show: Boolean) {}
}

interface IPluginHandler {
    fun removeCallbacks(r: Runnable?)
    fun postDelayed(r: Runnable?, delayMillis: Long): Boolean
}

class ViewWrapper(private val view: View) : IPluginHandler {
    override fun removeCallbacks(r: Runnable?) {
        view.removeCallbacks(r)
    }

    override fun postDelayed(r: Runnable?, delayMillis: Long): Boolean {
        return view.postDelayed(r, delayMillis)
    }
}

class HandlerWrapper(private val handler: Handler) : IPluginHandler {
    override fun removeCallbacks(r: Runnable?) {
        r?.let {
            handler.removeCallbacks(it)
        }
    }

    override fun postDelayed(r: Runnable?, delayMillis: Long): Boolean {
        return if (r != null) {
            handler.postDelayed(r, delayMillis)
        } else false
    }
}