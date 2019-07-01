package com.wongki.demo.http

import com.wongki.demo.model.api.DownloadApi
import com.wongki.framework.http.retrofit.core.RetrofitDownloaderServiceCore

/**
 * @author  wangqi
 * date:    2019/7/1
 * email:   wangqi7676@163.com
 * desc:    .
 */
object DownloaderService : RetrofitDownloaderServiceCore<DownloadApi>() {
    override val mHost: String = "https://api.apiopen.top"
}