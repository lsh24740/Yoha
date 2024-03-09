package com.gooey.ui.base

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.BarUtils
import com.gooey.ui.dialog.CommonDialogFragment
import com.gooey.ui.dialog.launchDialog

/**
 *@author lishihui01
 *@Date 2023/9/22
 *@Describe:
 */
class TransparentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BarUtils.transparentStatusBar(this)
        val bundle = intent?.getBundleExtra(BUNDLE_PARAMS)
        if (bundle != null) {
            when (bundle.getInt(EVENT_TYPE_KEY, EVENT_TYPE_UNKNOWN)) {
                EVENT_TYPE_DIALOG -> {
                    val dialogClass = bundle.getString(DIALOG_CLASS_KEY, "")
                    if (dialogClass.isNotEmpty()) {
                        launchDialog((Class.forName(dialogClass) as Class<CommonDialogFragment>), bundle).also { dialog ->
                            dialog?.setOnDismissListener {
                                finish()
                            }
                        }
                    } else {
                        finish()
                    }
                }

                else -> {
                    finish()
                }
            }
        } else {
            finish()
        }
    }

    companion object {
        const val BUNDLE_PARAMS = "BUNDLE_PARAMS"
        const val EVENT_TYPE_KEY = "EVENT_TYP"
        const val DIALOG_CLASS_KEY = "KEY_DIALOG_CLASS"
        const val EVENT_TYPE_UNKNOWN = -1
        const val EVENT_TYPE_DIALOG = 1

        @JvmStatic
        fun <T : CommonDialogFragment> launchGlobalDialog(activity: Activity, fragmentClass: Class<T>, bundle: Bundle? = null) {
            val intent = Intent(activity, TransparentActivity::class.java)
            val reBundle = (bundle ?: Bundle()).apply {
                putInt(EVENT_TYPE_KEY, EVENT_TYPE_DIALOG)
                putString(DIALOG_CLASS_KEY, fragmentClass.name)
            }
            intent.putExtra(BUNDLE_PARAMS, reBundle)
            activity.startActivity(intent)
        }

        @JvmStatic
        fun launch(activity: Activity, eventType: Int, bundle: Bundle? = null) {
            val intent = Intent(activity, TransparentActivity::class.java)
            val reBundle = (bundle ?: Bundle()).apply {
                putInt(EVENT_TYPE_KEY, eventType)
            }
            intent.putExtra(BUNDLE_PARAMS, reBundle)
            activity.startActivity(intent)
        }
    }

}