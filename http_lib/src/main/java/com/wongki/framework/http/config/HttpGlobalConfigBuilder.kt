package com.wongki.framework.http.config

import com.wongki.framework.http.exception.ApiException
import com.wongki.framework.http.listener.OnResponseFailedConvertListener
import com.wongki.framework.model.domain.CommonResponse
import com.wongki.framework.http.config.HttpConfigBuilder as HttpConfigBuilder

/**
 * @author  wangqi
 * date:    2019-11-12
 * email:   wangqi7676@163.com
 * desc:    .
 */
@HttpConfigDslMarker
class HttpGlobalConfigBuilder : HttpConfigBuilder {

    constructor(config: HttpGlobalConfig? = null) : super(config) {
        config ?: return
        this.successfulCode = config.successfulCode
        this.responseClass = config.responseClass
        this.onResponseConvertFailedListener = config.onResponseConvertFailedListener
    }

    var successfulCode: Int? = null
    var responseClass: Class<out CommonResponse<*>>? = null
    internal var onResponseConvertFailedListener: OnResponseFailedConvertListener? = null

    /**
     * 响应体结构转换失败
     * 当框架层内部转换失败时被触发
     * 你需要解析服务器返回的的错误信息转换成ApiException，如果没有有效的错误信息返回null
     * @param convert  1.response  2.mediaType
     */
    fun onResponseConvertFailed(convert: (String, String) -> ApiException?) {
        onResponseConvertFailedListener =
            object : OnResponseFailedConvertListener {
                override fun onConvertFailed(response: String, mediaType: String): ApiException? {
                    @Suppress("UNCHECKED_CAST")
                    return convert.invoke(response, mediaType)
                }

            }
    }

    override fun build(): HttpConfig = HttpGlobalConfig(this)
}