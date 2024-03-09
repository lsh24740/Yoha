package com.gooey.network.net

/**
 *@author lishihui01
 *@Date 2023/9/9
 *@Describe:
 */
interface IErrorCodeDispatcher {
    fun dispatch(errorCode: Int, msg: String)
}