package com.wongki.demo.http

import com.wongki.demo.model.api.MusicApi
import com.wongki.framework.http.retrofit.core.RetrofitServiceCore

/**
 * @author  wangqi
 * date:    2019/6/26
 * email:   wangqi7676@163.com
 * desc:    .
 */
object MusicServiceCore : RetrofitServiceCore<MusicApi>() {
    /**
     * 第一种：在全局的配置下进行配置
     */
    override fun generateDefaultConfig() = config {
        host = "https://api.apiopen.top"
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
     * 第二种：全新的配置，和全局配置没有关系
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