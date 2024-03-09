package com.gooey.common.base

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.sankuai.waimai.router.common.DefaultUriRequest
import com.sankuai.waimai.router.core.OnCompleteListener
import com.sankuai.waimai.router.core.UriRequest


/**
 *@author lishihui01
 *@Date 2024/3/2
 *@Describe:
 */

class UriProxyActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DefaultUriRequest.startFromProxyActivity(this, object : OnCompleteListener {
            override fun onSuccess(request: UriRequest) {
                finish()
            }

            override fun onError(request: UriRequest, resultCode: Int) {
                finish()
            }
        })
    }
}