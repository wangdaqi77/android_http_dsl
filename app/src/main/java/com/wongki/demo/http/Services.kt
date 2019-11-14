package com.wongki.demo.http

import com.wongki.framework.http.HttpDsl

/**
 * @author  wangqi
 * date:    2019/6/26
 * email:   wangqi7676@163.com
 * desc:    .
 */


@HttpDsl
fun musicService(action:MusicServiceCore.()->Unit){
    MusicServiceCore.action()
}
@HttpDsl
fun otherService(action:OtherServiceCore.()->Unit){
    OtherServiceCore.action()
}