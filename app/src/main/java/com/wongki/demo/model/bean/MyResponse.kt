package com.wongki.demo.model.bean

import com.google.gson.annotations.SerializedName
import com.wongki.framework.model.domain.CommonResponse

/**
 * @author  wangqi
 * date:    2019-11-11
 * email:   wangqi7676@163.com
 * desc:    .
 */
class MyResponse<T> : CommonResponse<T> {

    @SerializedName("code")
    override var code: Int = -1
    @SerializedName("message")
    override var message: String? = null
    @SerializedName("result")
    override var data: T? = null

    override fun toString(): String {
        return "code = $code" +
                "，message = $message" +
                "，result = $data"
    }
}