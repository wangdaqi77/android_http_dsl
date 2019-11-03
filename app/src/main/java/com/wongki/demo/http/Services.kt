package com.wongki.demo.http

import com.wongki.demo.model.api.DownloadApi
import com.wongki.demo.model.api.MusicApi
import com.wongki.demo.model.api.OtherApi
import com.wongki.framework.http.retrofit.core.RetrofitServiceDslMarker
import com.wongki.framework.http.retrofit.lifecycle.IHttpRetrofitLifecycleObserver
import com.wongki.framework.model.domain.CommonResponse
import io.reactivex.Observable
import okhttp3.ResponseBody

/**
 * @author  wangqi
 * date:    2019/6/26
 * email:   wangqi7676@163.com
 * desc:    .
 */


@RetrofitServiceDslMarker
fun musicService(action:MusicServiceCore.()->Unit){
    MusicServiceCore.action()
}

fun <R> Any.newMusicRequester(
    lifecycleObserver: IHttpRetrofitLifecycleObserver? = null,
    preRequest: (MusicApi) -> Observable<CommonResponse<R>>
) = MusicServiceCore.newRequester(lifecycleObserver, preRequest)

fun <R> Any.newOtherRequester(
    lifecycleObserver: IHttpRetrofitLifecycleObserver? = null,
    preRequest: (OtherApi) -> Observable<CommonResponse<R>>
) = OtherServiceCore.newRequester(lifecycleObserver, preRequest)

fun  Any.newDownloadRequester(
    lifecycleObserver: IHttpRetrofitLifecycleObserver? = null,
    filePath: String,
    preRequest: (DownloadApi) -> Observable<ResponseBody>
) = DownloaderService.newRequester(lifecycleObserver, filePath, preRequest)
