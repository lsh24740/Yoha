package com.gooey.common.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gooey.common.R

/**
 * Created by lishihui.
 */

fun LifecycleOwner.attach(create: (() -> Unit)? = null,
                          start: (() -> Unit)? = null,
                          resume: (() -> Unit)? = null,
                          pause: (() -> Unit)? = null,
                          stop: (() -> Unit)? = null,
                          destroy: (() -> Unit)? = null) {
    if (create == null
            && start == null
            && resume == null
            && pause == null
            && stop == null
            && destroy == null) {
        return
    }
    this.lifecycle.addObserver(LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                create?.invoke()
            }

            Lifecycle.Event.ON_START -> {
                start?.invoke()
            }

            Lifecycle.Event.ON_RESUME -> {
                resume?.invoke()
            }

            Lifecycle.Event.ON_PAUSE -> {
                pause?.invoke()
            }

            Lifecycle.Event.ON_STOP -> {
                stop?.invoke()
            }

            Lifecycle.Event.ON_DESTROY -> {
                destroy?.invoke()
            }

            else -> {}
        }
    })
}

/**
 * 注册广播接收器
 * 自动根据owner生命周期进行解注册
 */
fun BroadcastReceiver.register(owner: FragmentActivity?, filter: IntentFilter) {
    registerBroadcastReceiver(owner, owner, {
        it.registerReceiver(this, filter)
    }, {
        it.unregisterReceiver(this)
    })
}

/**
 * 注册广播接收器
 * 自动根据owner生命周期进行解注册
 */
fun BroadcastReceiver.register(owner: Fragment?, filter: IntentFilter) {
    registerBroadcastReceiver(owner, owner?.context, {
        it.registerReceiver(this, filter)
    }, {
        it.unregisterReceiver(this)
    })
}

fun FragmentActivity.register(vararg infos: BroadcastInfo) {
    attach(create = {
        infos.forEach {
            kotlin.runCatching {
                registerReceiver(it.receiver, it.filter)
            }
        }
    }, destroy = {
        infos.forEach {
            kotlin.runCatching {
                unregisterReceiver(it.receiver)
            }
        }
    })
}

/**
 * 注册广播接收器
 * 自动根据owner生命周期进行解注册
 */
fun BroadcastReceiver.registerLocal(owner: FragmentActivity?, filter: IntentFilter) {
    registerBroadcastReceiver(owner, owner, {
        LocalBroadcastManager.getInstance(it).registerReceiver(this, filter)
    }, {
        LocalBroadcastManager.getInstance(it).unregisterReceiver(this)
    })
}

/**
 * 注册广播接收器
 * 自动根据owner生命周期进行解注册
 */
fun BroadcastReceiver.registerLocal(owner: Fragment?, filter: IntentFilter) {
    registerBroadcastReceiver(owner, owner?.context, {
        LocalBroadcastManager.getInstance(it).registerReceiver(this, filter)
    }, {
        LocalBroadcastManager.getInstance(it).unregisterReceiver(this)
    })
}

/**
 * 注册广播接收器
 * 自动根据owner生命周期进行解注册
 */
private fun registerBroadcastReceiver(
        owner: LifecycleOwner?,
        context: Context?,
        registerCallback: (context: Context) -> Unit,
        unregisterCallback: (context: Context) -> Unit
) {
    if (owner == null || context == null) {
        return
    }
    val activity = context as? Activity
    if (activity?.isFinishing == true || activity?.isDestroyed == true) {
        return
    }
    owner.attach(create = {
        kotlin.runCatching {
            registerCallback.invoke(context)
        }
    }, destroy = {
        kotlin.runCatching {
            unregisterCallback.invoke(context)
        }
    })
}

/**
 * 页面可见时响应~
 */
fun BroadcastReceiver.registerLocalForVisible(owner: FragmentActivity?, filter: IntentFilter) {
    if (owner == null) {
        return
    }
    owner.attach(resume = {
        LocalBroadcastManager.getInstance(owner).registerReceiver(this, filter)
    }, pause = {
        LocalBroadcastManager.getInstance(owner).unregisterReceiver(this)
    })
}

fun FragmentActivity.registerLocal(vararg infos: BroadcastInfo) {
    attach(create = {
        infos.forEach {
            LocalBroadcastManager.getInstance(this).registerReceiver(it.receiver, it.filter)
        }
    }, destroy = {
        infos.forEach {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it.receiver)
        }
    })
}

class BroadcastInfo(val receiver: BroadcastReceiver, val filter: IntentFilter)


val FragmentActivity.alwaysLifecycle: LifecycleOwner
    get() {
        val view: View? = findViewById(android.R.id.content)
        return if (view == null) {
            ForegroundLifecycleOwner(this)
        } else {
            var owner = view.getTag(R.id.alwaysLifecycleOwner) as? ForegroundLifecycleOwner
            if (owner == null) {
                owner = ForegroundLifecycleOwner(this)
                view.setTag(R.id.alwaysLifecycleOwner, owner)
            }
            owner
        }
    }

val Fragment.alwaysLifecycle: LifecycleOwner
    get() {
        val view: View? = view
        return if (view == null) {
            ForegroundLifecycleOwner(this)
        } else {
            var owner = view.getTag(R.id.alwaysLifecycleOwner) as? ForegroundLifecycleOwner
            if (owner == null) {
                owner = ForegroundLifecycleOwner(this)
                view.setTag(R.id.alwaysLifecycleOwner, owner)
            }
            owner
        }
    }

fun <T> MutableLiveData<T>.safeObserve(owner: LifecycleOwner, onChange: (T) -> Unit) {
    val observe = this.observe(owner) {
        try {
            onChange(it)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}