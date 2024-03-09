package com.gooey.ui.dialog

/**
 *@author lishihui01
 *@Date 2023/9/21
 *@Describe:
 */
interface IBottomDialog {
    enum class BEHAVIOR {
        FADE,
        SLIDE,
        HIDE,
        STAY
    }

    enum class OPERATION {
        SHOW,
        HIDE,
        FADE,
        SLIDE
    }

    fun getBehavior(target: IBottomDialog): BEHAVIOR

    fun performOperation(operation: OPERATION)

    fun isFinishing(): Boolean

    fun getIdentifier(): String // 当前页面的唯一标识，用来进行页面移除操作
}