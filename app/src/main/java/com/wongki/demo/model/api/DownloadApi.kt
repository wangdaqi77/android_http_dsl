package com.wongki.demo.model.api

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * @author  wangqi
 * date:    2019/7/1
 * email:   wangqi7676@163.com
 * desc:    .
 */
interface DownloadApi {
    @GET
    @Streaming
    fun download(@Url url:String) :Observable<ResponseBody>
}