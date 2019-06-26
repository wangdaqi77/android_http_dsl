package com.wongki.demo.http

import com.wongki.demo.model.MusicApi
import com.wongki.framework.http.retrofit.core.RetrofitServiceCore
import com.wongki.framework.http.retrofit.observer.HttpCommonObserver

/**
 * @author  wangqi
 * date:    2019/6/26
 * email:   wangqi7676@163.com
 * desc:    .
 */
object MusicServiceCore : RetrofitServiceCore<MusicApi>() {
    override val mHost = "https://api.apiopen.top"

    /**
     * 公共请求头
     */
    override var mCommonRequestHeader: MutableMap<String, String> = mutableMapOf()

    /**
     * 公共Url参数
     * ex: &sex=1&age=18
     */
    override var mCommonUrlRequestParams: MutableMap<String, String> = mutableMapOf()

    /**
     * 拦截处理网络请求中的异常错误码，
     * 返回true代表拦截处理
     * 返回false代表不处理，最终会交给底层处理，详情查看[HttpCommonObserver.onError]
     */
    override val onInterceptErrorCode: (Int, String?) -> Boolean = { _, _ -> false }
}