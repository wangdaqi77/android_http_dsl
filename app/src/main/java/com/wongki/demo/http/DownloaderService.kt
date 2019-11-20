package com.wongki.demo.http

import com.wongki.demo.model.api.DownloadApi
import com.wongki.framework.http.config.HttpConfig
import com.wongki.framework.http.retrofit.core.RetrofitDownloaderServiceCore

/**
 * @author  wangqi
 * date:    2019/7/1
 * email:   wangqi7676@163.com
 * desc:    .
 */
class DownloaderService : RetrofitDownloaderServiceCore<DownloadApi>() {
    override fun generateConfig() = config {
        host = "https://api.apiopen.top"
    }
}