package com.wongki.demo.http

import com.wongki.demo.model.api.OtherApi
import com.wongki.framework.http.retrofit.core.RetrofitServiceCore

/**
 * @author  wangqi
 * date:    2019/6/26
 * email:   wangqi7676@163.com
 * desc:    .
 */
object OtherServiceCore : RetrofitServiceCore<OtherApi>() {
    override val mHost = ""

    override fun getCommonRequestHeader(): MutableMap<String, String> = mutableMapOf()
    override fun getCommonUrlRequestParams(): MutableMap<String, String> = mutableMapOf()

}