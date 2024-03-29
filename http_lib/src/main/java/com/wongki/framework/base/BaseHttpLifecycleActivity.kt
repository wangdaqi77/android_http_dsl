package com.wongki.framework.base

import com.wongki.framework.http.retrofit.lifecycle.IHttpDestroyedObserver


/**
 * @author  wangqi
 * date:    2019/6/26
 * email:   wangqi7676@163.com
 * desc:    .
 */
open class BaseHttpLifecycleActivity:BaseActivity(), IHttpDestroyedObserver {
    override fun clearRequest() {
        super<IHttpDestroyedObserver>.clearRequest()
        super<BaseActivity>.onDestroy()
    }
}