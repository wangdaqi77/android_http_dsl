package com.wongki.framework.http.retrofit.observer

import com.wongki.framework.extensions.toast
import com.wongki.framework.http.exception.ApiException
import com.wongki.framework.http.HttpCode
import com.wongki.framework.http.exception.ParseResponseException
import io.reactivex.Observer
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException

/**
 * @author  wangqi
 * date:    2019/6/17
 * email:   wangqi7676@163.com
 * desc:    .
 */
abstract class HttpCommonObserver<R>(
    val onInterceptErrorCode: (Int, String?) -> Boolean,
    val onFailed: (Int, String?) -> Boolean,
    val onSuccess: (R) -> Unit
) : Observer<R> {

    override fun onNext(t: R) {
        onSuccess(t)
    }

    override fun onError(e: Throwable) {
        val wrapError: Pair<Int, String?> = parseAndWrapError(e)
        val code: Int = wrapError.first
        val msg: String? = wrapError.second

        // 上层是否拦截处理错误码？ 默认false，不处理
        val isIntercept = onInterceptErrorCode(code, msg)
        if (!isIntercept) {
            when (code) {
                // 升级
                100 -> {
                    //升级
                    // EventBus.getDefault().post()
                    // return
                }
                // token失效，弹窗提示或者返回登录界面
                101 -> {
                    //其他接口返回到登陆界面
//                EventBus.getDefault().post()
                }
            }
        }

        // 业务层是否处理
        val handle = onFailed(code, msg)
        if (!handle) {
            "$msg ($code)".toast()
        }
    }

    /**
     * 解析包装错误信息
     */
    private fun parseAndWrapError(e: Throwable): Pair<Int, String?> {
        val code: Int
        val msg: String?
        val message = e::class.java.name + ": ${e.message}"
        when (e) {
            is ApiException -> {
                code = e.code
                msg = e.msg
            }

            is IOException,  // 请求失败
            is HttpException // 非成功 !(code >= 200 && code < 300)
            -> {
                code = HttpCode.REQUEST_FAILED
                msg = message
            }
            // 未知host
            is UnknownHostException -> {
                code = HttpCode.HOST_UNKNOWN_FAILED
                msg = message
            }
            // 连接服务器超时,读写超时
            is SocketTimeoutException -> {
                code = HttpCode.TIMEOUT_FAILED
                msg = message
            }
            // 网路连接失败
            is ConnectException -> {
                code = HttpCode.CONNECTION_FAILED
                msg = message
            }
            // 解析异常
            is ParseResponseException,
            is ParseException
            -> {
                code = HttpCode.PARSE_FAILED
                msg = message
            }
            // 未知错误
            else -> {
                code = HttpCode.UNKNOWN_FAILED
                msg = message
            }
        }

        return code to msg
    }
}