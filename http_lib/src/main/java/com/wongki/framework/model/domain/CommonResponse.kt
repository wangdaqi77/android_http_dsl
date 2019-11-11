package com.wongki.framework.model.domain

/**
 * @author  wangqi
 * date:    2019/6/12
 * email:   wangqi@feigeter.com
 * desc:    .
 */
interface CommonResponse<T> {
    var code: Int
    var message: String?
    var data: T?
}