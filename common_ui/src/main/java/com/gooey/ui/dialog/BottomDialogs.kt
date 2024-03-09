package com.gooey.ui.dialog

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

/**
 *@author lishihui01
 *@Date 2023/9/21
 *@Describe:
 */
object BottomDialogs {
    private val dialogs = ArrayList<WindowInfo>()
    private val pendings = ArrayList<WindowInfo>()

    private fun performPauseBehavior(window: IBottomDialog?, toAdd: IBottomDialog) {
        when (window?.getBehavior(toAdd)) {
            IBottomDialog.BEHAVIOR.FADE -> {
                window.performOperation(IBottomDialog.OPERATION.FADE)
                removeWindowInList(window)
            }
            IBottomDialog.BEHAVIOR.SLIDE -> {
                window.performOperation(IBottomDialog.OPERATION.SLIDE)
                removeWindowInList(window)
            }
            IBottomDialog.BEHAVIOR.HIDE -> window.performOperation(IBottomDialog.OPERATION.HIDE)
            else -> Unit
        }
    }

    private fun performResumeBehavior(window: IBottomDialog?, toRemove: IBottomDialog) {
        when (window?.getBehavior(toRemove)) {
            IBottomDialog.BEHAVIOR.HIDE -> window.performOperation(IBottomDialog.OPERATION.SHOW)
            else -> Unit
        }
    }

    fun topShowingWindow(): WindowInfo? {
        val iter = dialogs.iterator()
        while (iter.hasNext()) {
            val show = iter.next()
            val window = show.get()
            if (window != null && !window.isFinishing()) {
                return show
            } else {
                iter.remove()
            }
        }
        return null
    }

    private fun removeWindowInList(input: IBottomDialog): WindowInfo? {
        // 从上往下遍历
        val iter = dialogs.iterator()
        var list: ArrayList<Later>? = null
        var info: WindowInfo? = null
        while (iter.hasNext()) {
            val show = iter.next()
            val window = show.get()
            if (window != null && window === input || window == null) {
                iter.remove()
                if (window != null) {
                    info = show
                } else {
                    if (show.delayList != null) {
                        if (list == null) {
                            list = ArrayList()
                        }
                        list.addAll(show.delayList!!)
                    }
                }
            } else {
                if (list?.isNotEmpty() == true) {
                    show.addDelays(list)
                    list.clear()
                }
            }
        }
        return info
    }

    /* --------------------------- Window APIs ------------------------------ */
    internal fun isBlocking(bundle: Bundle?, later: Later): Boolean {
        val prior = bundle?.getInt(PROP_PRIOR, 0) ?: 0
        val blocking = bundle?.getBoolean(PROP_BLOCKING, false) ?: false
        val top = topShowingWindow()
        if (!blocking) {
            val topBarrier = top?.barrier ?: false
            if (topBarrier) {
                top?.addDelay(later)
                return true
            }
            val found = pendings.find {
                it.barrier
            }
            if (found != null) {
                found.addDelay(later)
                return true
            }
        }
        val topPrior = top?.prior ?: 0
        if (topPrior > prior) {
            top?.addDelay(later)
            return true
        }
        val found = pendings.find {
            it.prior > prior
        }
        if (found != null) {
            found.addDelay(later)
            return true
        }

        return false
    }

    internal fun addDelay(later: Later, fromBlocking: Boolean = true) {
        val top = topShowingWindow()
        if (top != null && (!fromBlocking || top.barrier)) {
            top.addDelay(later)
        } else {
            val found = pendings.find {
                it.barrier
            }
            if (found != null) {
                found.addDelay(later)
            } else {
                top?.addDelay(later)
            }
        }
    }

    internal fun addPending(
            fragmentActivity: FragmentActivity?,
            window: IBottomDialog?,
            barrier: Boolean = false
    ) {
        if (window == null) {
            return
        }
        pendings.add(
                WindowInfo(
                        WeakReference(fragmentActivity), WeakReference(window), barrier
                )
        )
    }

    fun addWindow(
            fragmentActivity: FragmentActivity?,
            window: IBottomDialog?,
            barrier: Boolean = false
    ) {
        if (window == null) {
            return
        }
        val iter = pendings.iterator()
        var delay : ArrayList<Later>? = null
        while (iter.hasNext()) {
            val item = iter.next()
            if (item.get() == window || item.get() == null) {
                if (item.get() == window) {
                    item.delayList?.apply {
                        if (delay == null) {
                            delay = arrayListOf()
                        }
                        delay?.addAll(this)
                    }
                }
                iter.remove()
            }
        }
        val reference = topShowingWindow()
        if (reference != null) {
            val show = reference.get()
            if (show === window) {
                return
            }
            if (reference.getHost() == fragmentActivity) {
                performPauseBehavior(show, window)
            }
        }
        dialogs.add(
                0, WindowInfo(
                WeakReference(fragmentActivity), WeakReference(window), barrier
        ).apply {
            delay?.let {
                addDelays(it)
            }
        }
        )
    }

    fun removeWindow(window: IBottomDialog?) {
        if (window == null) {
            return
        }
        val iter = pendings.iterator()
        while (iter.hasNext()) {
            val item = iter.next()
            if (item.get() == window || item.get() == null) {
                iter.remove()
            }
        }
        val reference = topShowingWindow()
        if (reference != null) {
            val show = reference.get()
            val found = removeWindowInList(window)
            if (show !== window) {
                return
            }
            val nextReference = topShowingWindow()
            if (nextReference != null) {
                val next = nextReference.get()
                performResumeBehavior(next, window)
            }
            if (found != null) {
                startNext(found.getHost(), found.delayList)
            }
        }
    }

    fun removeWindowFromIdentifier(identifier: String?) {
        var foundDialog: IBottomDialog? = null
        for (windowInfo in dialogs) {
            val searchDialog = windowInfo.get()
            if (searchDialog?.getIdentifier() == identifier) {
                foundDialog = searchDialog
                break
            }
        }
        if (foundDialog != null) {
            removeWindow(foundDialog)
        }
    }

    /**
     * 通过identifier判断某个浮层是否已经展示
     * true：已经展示
     */
    fun windowIsShowed(identifier: String?): Boolean {
        return identifier?.takeIf { it.isNotEmpty() }?.let {
            dialogs.find { it.get()?.getIdentifier() == identifier } != null
        } ?: false
    }

    private fun startNext(host: FragmentActivity?, list: ArrayList<Later>?) {
        if (host == null || list == null) {
            return
        }
        if (list.size <= 0) {
            return
        }
        list.sortByDescending {
            val prior = it.bundle?.getInt(PROP_PRIOR, 0) ?: 0
            prior
        }
        val later = list.removeAt(0)
        val ret = host.launchDialog(
                later.fragmentClass, later.bundle,
                later.relaunch, later.any as? ((BottomDialogCallback?) -> Unit)
        )
        if (ret != null && ret is Fragment) {
            ret.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_CREATE) {
                        startNext(host, list)
                    }
                }
            })
        } else {
            startNext(host, list)
        }
    }

    fun closeAllWindow() {
        val tmp = ArrayList<WindowInfo>(dialogs)
        dialogs.clear()
        tmp.forEach {
            val window = it.get()
            if (window != null && !window.isFinishing()) {
                window.performOperation(IBottomDialog.OPERATION.FADE)
            }
        }
    }

    /**
     * 本方法供外部在横竖屏切换，需要关闭所有浮层时调用
     * 当[CommonDialogFragment.needCloseWhenOrientationChanged] 返回结果为false时，
     * 该浮层将不会因为横竖屏切换而被关闭
     */
    fun closeAllWindowWhenConfigurationChanged() {
        val iterator = dialogs.iterator()
        while (iterator.hasNext()) {
            val window = iterator.next().get()
            val needSave =
                    window is CommonDialogFragment && !window.needCloseWhenOrientationChanged()
            if (!needSave && window != null && !window.isFinishing()) {
                window.performOperation(IBottomDialog.OPERATION.FADE)
                iterator.remove()
            }
        }
    }

    fun closeAllWindow(fragmentActivity: FragmentActivity?) {
        if (fragmentActivity == null) {
            closeAllWindow()
            return
        }
        val iterator = dialogs.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (fragmentActivity == next.getHost()) {
                iterator.remove()

                val window = next.get()
                if (window != null && !window.isFinishing()) {
                    window.performOperation(IBottomDialog.OPERATION.FADE)
                }
            }
        }
    }
}

class WindowInfo(
        val host: WeakReference<FragmentActivity?>,
        val ref: WeakReference<IBottomDialog>,
        var barrier: Boolean = false
) {
    var prior = 0
    var delayList: ArrayList<Later>? = null

    fun get(): IBottomDialog? = ref.get()

    fun getHost(): FragmentActivity? = host.get()

    fun addDelay(later: Later) {
        if (delayList == null) {
            delayList = ArrayList()
        }
        delayList?.add(0, later)
    }

    fun addDelays(input: List<Later>) {
        if (delayList == null) {
            delayList = ArrayList()
        }
        delayList?.addAll(0, input)
    }
}

open class Later(
        val fragmentClass: Class<out CommonDialogFragment>,
        val bundle: Bundle? = null,
        val relaunch: Boolean = false,
        val any: Any? = null
)