package com.wongki.framework.http.listener

import com.wongki.framework.http.exception.ApiException
import com.wongki.framework.http.gInner
import com.wongki.framework.model.domain.CommonResponse
import com.wongki.framework.utils.transform

/**
 * @author  wangqi
 * date:    2019-11-11
 * email:   wangqi7676@163.com
 * desc:    网络请求响应转换失败的监听
 */
interface OnResponseFailedConvertListener {
    /**
     * @param response 服务器返回的数据
     * @param mediaType 数据类型
     */
    fun onConvertFailed(response: String, mediaType: String): ApiException?

    companion object DEFAULT:OnResponseFailedConvertListener{
        override fun onConvertFailed(response: String, mediaType: String): ApiException? {
            /**
             * 当转换失败时被触发
             * 在这里你需要把服务器的错误信息转换成ApiException，
             * 如果没有有效的服务器错误信息需要返回null
             */
            gInner.logger?.log("DEFAULT.onConvertFailed-> response:$response, mediaType:$mediaType")
            var code: Int = -1
            var msg: String = ""
            response.transform(CommonResponse::class.java) { target ->
                code = optInt("code", -1)
                msg = optString("message", "")
            }

            if (code != -1) {
                return ApiException(code, msg)
            }
            return null
        }
    }
}
