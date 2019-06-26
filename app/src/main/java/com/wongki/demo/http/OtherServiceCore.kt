package com.wongki.demo.http

import com.wongki.demo.model.OtherApi
import com.wongki.framework.http.retrofit.core.RetrofitServiceCore

/**
 * @author  wangqi
 * date:    2019/6/26
 * email:   wangqi7676@163.com
 * desc:    .
 */
object OtherServiceCore : RetrofitServiceCore<OtherApi>() {
    override val mHost = ""

    override var mCommonRequestHeader: MutableMap<String, String> = mutableMapOf()
    override var mCommonUrlRequestParams: MutableMap<String, String> = mutableMapOf()
    override var mCommonPostRequestParams: MutableMap<String, String> = mutableMapOf()

    override val onInterceptErrorCode: (Int, String?) -> Boolean = { _, _ -> false }
}