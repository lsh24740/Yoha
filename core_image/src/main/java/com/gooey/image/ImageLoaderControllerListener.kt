package com.gooey.image

import android.graphics.Bitmap




/**
 *@author lishihui01
 *@Date 2024/3/9
 *@Describe:
 */

interface ImageLoaderControllerListener {
    fun onFailureImpl()
    fun onNewResultImpl(bitmap: Bitmap?)
}