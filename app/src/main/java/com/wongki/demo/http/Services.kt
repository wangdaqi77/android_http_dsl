package com.wongki.demo.http

import com.wongki.demo.model.MusicApi
import com.wongki.demo.model.OtherApi
import com.wongki.framework.http.retrofit.lifecycle.IHttpRetrofitLifecycleObserver
import com.wongki.framework.model.domain.CommonResponse
import io.reactivex.Observable

/**
 * @author  wangqi
 * date:    2019/6/26
 * email:   wangqi7676@163.com
 * desc:    .
 */

fun <R> Any.newMusicRequester(lifecycleObserver: IHttpRetrofitLifecycleObserver? = null, preRequest: (MusicApi) -> Observable<CommonResponse<R>>) = MusicServiceCore.newRequester(lifecycleObserver, preRequest)
fun <R> Any.newOtherRequester(lifecycleObserver: IHttpRetrofitLifecycleObserver? = null, preRequest: (OtherApi) -> Observable<CommonResponse<R>>) = OtherServiceCore.newRequester(lifecycleObserver, preRequest)
