package com.gooey.common

/**
 *@author lishihui01
 *@Date 2023/9/16
 *@Describe:
 */
interface IMReceiver {
    fun accept(): Set<String>
    fun onReceive(code: String, message: Any?):Boolean
}