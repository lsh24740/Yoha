package com.gooey.ui.dialog

/**
 *@author lishihui01
 *@Date 2023/9/21
 *@Describe:
 */
interface BottomDialogCallback {
    fun dismiss(fade: Boolean)
    fun onReShow()
    fun canDismiss() : Boolean
    fun closeBtnClick()
}