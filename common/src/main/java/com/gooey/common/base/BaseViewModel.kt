package com.gooey.common.base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *@author lishihui01
 *@Date 2023/9/1
 *@Describe:
 */
open class BaseViewModel : ViewModel() {



    fun getApp() : Application {
        return Utils.getApp()
    }

    fun postIO(runnable: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                runnable()
            }
        }
    }
}