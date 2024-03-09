package com.gooey.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.LogUtils
import com.gooey.ui.databinding.LayoutCommonDialogFragmentBinding

/**
 *@author lishihui01
 *@Date 2023/9/16
 *@Describe:
 */
abstract class CommonDialogFragment : DialogFragment(), BottomDialogCallback, IBottomDialog {
    protected lateinit var mView: View
    protected var helper: BottomDialogHelper? = null
    private val config: BottomDialogConfig by lazy {
        getDialogConfig()
    }
    private var dismissListener: DialogInterface.OnDismissListener? = null
    private var showListener: DialogInterface.OnShowListener? = null
    private val keyboardHelper by lazy {
        KeyboardHelper(this)
    }

    open fun getDialogConfig(): BottomDialogConfig {
        return BottomDialogConfig()
    }

    open fun setLightStatusBar(view: View, isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = view.systemUiVisibility
            flags = if (isLight) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            view.systemUiVisibility = flags
        }
    }

    override fun closeBtnClick() {
    }

    override fun onReShow() {}

    override fun canDismiss(): Boolean = true


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(config.style, config.theme)
        val dialog = object : AppCompatDialog(requireContext(), theme) {
            override fun onBackPressed() {
                if (config.cancelable) {
                    super.onBackPressed()
                }
            }
        }
        val window = dialog.window
        window?.let {
            val params = window.attributes
            if (config.transparentStatusBar) {
                BarUtils.transparentStatusBar(window)
            }
            if (!config.dimBehind && config.dimAmount < 0f) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                if (config.dimAmount >= 0f) {
                    params.dimAmount = config.dimAmount
                }
            }
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            if (config.gravity == Gravity.BOTTOM) {
                window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            } else {
                window.decorView.systemUiVisibility = config.customUiFlag
            }
            if (config.gravity == Gravity.BOTTOM && config.navigationBarColor != 0) {
                BarUtils.setNavBarColor(window, config.navigationBarColor)
            }
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            params.gravity = Gravity.BOTTOM
            params.windowAnimations = config.windowAnim
            window.attributes = params
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnShowListener {
            showListener?.onShow(it)
        }
        return dialog
    }

    abstract fun onCreateViewInner(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = LayoutCommonDialogFragmentBinding.inflate(inflater)
        val ret = super.onCreateView(inflater, binding.root as ViewGroup, savedInstanceState)
        mView = ret ?: onCreateViewInner(inflater, binding.root as ViewGroup, savedInstanceState)
        helper = BottomDialogHelper(this, mView, binding, config)
        if (config.hasEditor) {
            keyboardHelper.attach(true, helper?.binding?.rootContainer)
        }
        setLightStatusBar(mView, config.statusBarIsLight)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (config.hasEditor) {
            keyboardHelper.attach(false)
        }
    }


    override fun dismiss(fade: Boolean) {
        if (fade) {
            dialog?.window?.setWindowAnimations(0)
        }
        KeyboardUtils.hideSoftInput(activity)
        dismissAllowingStateLoss()
    }

    override fun dismiss() {
        KeyboardUtils.hideSoftInput(activity)
        dismissAllowingStateLoss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDismiss(dialog)
    }

    fun setOnDismissListener(listener: DialogInterface.OnDismissListener) {
        this.dismissListener = listener
    }

    fun setOnShowListener(listener: DialogInterface.OnShowListener) {
        this.showListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (config.inTask) {
            BottomDialogs.addWindow(activity, this, config.block)
        }
    }

    protected fun <T : View> findViewById(id: Int): T? {
        return mView.findViewById(id)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (config.inTask) {
            BottomDialogs.removeWindow(this)
        }
    }

    override fun getBehavior(target: IBottomDialog): IBottomDialog.BEHAVIOR {
        return IBottomDialog.BEHAVIOR.HIDE
    }

    override fun performOperation(operation: IBottomDialog.OPERATION) {
        if (isStateSaved) {
            return
        }
        when (operation) {
            IBottomDialog.OPERATION.HIDE -> helper?.hide()
            IBottomDialog.OPERATION.FADE -> dismiss(true)
            IBottomDialog.OPERATION.SHOW -> helper?.show()
            IBottomDialog.OPERATION.SLIDE -> dismiss(false)
        }
    }

    override fun isFinishing(): Boolean {
        return activity?.isFinishing ?: true
    }

    override fun getIdentifier(): String {
        return ""
    }

    /**
     * 横竖屏切换时是否需要关闭 默认关闭
     * @return true:需要关闭  false:不需要关闭
     */
    open fun needCloseWhenOrientationChanged(): Boolean {
        return true
    }

    /**
     * 在每个add事务前增加一个remove事务，防止连续的add
     *
     */
    override fun show(manager: FragmentManager, tag: String?) {
        try {
            manager.beginTransaction().remove(this).commit()
            super.show(manager, tag)
        } catch (e: java.lang.Exception) {
            //同一实例使用不同的tag会异常,这里捕获一下
            e.printStackTrace()
        }
    }
}

const val PROP_PREEMPT = "preempt"
const val PROP_LATER = "showLater"
const val PROP_BLOCKING = "blocking"
const val PROP_PRIOR = "prior"

fun <T : CommonDialogFragment> FragmentActivity.launchDialog(fragmentClass: Class<T>,
                                                             bundle: Bundle? = null,
                                                             relaunch: Boolean = true,
                                                             init: ((T?) -> Unit)? = null): T? {
    // 优先级高于当前正在显示的block弹窗
    val preempt = bundle?.getBoolean(PROP_PREEMPT, false) ?: false
    val showLater = bundle?.getBoolean(PROP_LATER, false) ?: false
    val blocking = bundle?.getBoolean(PROP_BLOCKING, false) ?: false
    if (!preempt && BottomDialogs.isBlocking(bundle, Later(fragmentClass, bundle, relaunch, init))) {
        return null
    }
    // 如果当前正好没有dialog显示，addDelay会导致弹框直接没有了。
    // 可以改造addDelay内部，但是可能会影响其他业务，因此在外部由业务侧自行判断topShowingWindow()是否存在
    if ((showLater && BottomDialogs.topShowingWindow() != null)) {
        LogUtils.dTag("launchDialog", "add addDelay. showLater=$showLater")
        BottomDialogs.addDelay(Later(fragmentClass, bundle, relaunch, init), false)
        return null
    }
    LogUtils.dTag("launchDialog", "preempt=$preempt, showLater=$showLater")
    val tag = if (relaunch)
        fragmentClass.canonicalName ?: fragmentClass.name
    else
        (fragmentClass.canonicalName ?: fragmentClass.name) + "_" + SystemClock.elapsedRealtime()
    val fragmentManager = supportFragmentManager
    var slidingFragment = fragmentManager.findFragmentByTag(tag) as T?
    if (relaunch && slidingFragment != null) {
        slidingFragment.dismiss(true)
        slidingFragment = null
    }
    if (slidingFragment == null) {
        try {
            slidingFragment = fragmentClass.newInstance()
            init?.invoke(slidingFragment)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        }
    }
    if (slidingFragment == null) {
        return null
    }
    if (slidingFragment is IBottomDialog) {
        BottomDialogs.addPending(this, slidingFragment, blocking)
    }
    if (slidingFragment is Fragment) {
        if (!slidingFragment.isStateSaved) {
            slidingFragment.arguments = bundle
        }
    }
    if (isFinishing) {
        return slidingFragment
    }
    try {
        if (slidingFragment is CommonDialogFragment) {
            if (slidingFragment.isAdded) {
                slidingFragment.show(fragmentManager, tag)
            } else {
                fragmentManager
                        .beginTransaction()
                        .add(slidingFragment, tag)
                        .commitAllowingStateLoss()
            }
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }

    return slidingFragment
}
