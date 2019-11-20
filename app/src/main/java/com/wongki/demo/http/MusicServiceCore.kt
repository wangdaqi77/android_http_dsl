package com.wongki.demo.http

import com.wongki.demo.model.api.MusicApi
import com.wongki.framework.http.HttpDsl
import com.wongki.framework.http.retrofit.core.RetrofitServiceCore

/**
 * @author  wangqi
 * date:    2019/6/26
 * email:   wangqi7676@163.com
 * desc:    .
 */
@HttpDsl
object MusicServiceCore : RetrofitServiceCore<MusicApi>() {
    /**
     * 基于全局配置进行配置
     */
    override fun generateConfig() = config {
        // host = "https://api.apiopen.top"
        tag = "MusicServiceCore"
        addApiErrorInterceptor2FirstNode{code,message->
            false
        }
        addHeaders {
            mutableMapOf(
                "header" to "musicService",
                "headerMusicService" to "musicService"
            )
        }

        addUrlQueryParams {
            mutableMapOf(
                "urlQueryParam" to "musicService",
                "urlQueryParamMusicService" to "musicService"
            )
        }

    }

    /**
     * 全新的独立配置，和全局配置没有关系
     */
//    override var defaultConfig: HttpConfig = newConfig {
//        host = "https://api.apiopen.top"
//        tag = "MusicServiceCore"
//        addHeaders {
//            mutableMapOf(
//                "header" to "musicService",
//                "headerMusicService" to "musicService"
//            )
//        }
//
//        addUrlQueryParams {
//            mutableMapOf(
//                "urlQueryParam" to "musicService",
//                "urlQueryParamMusicService" to "musicService"
//            )
//        }
//    }


}