package com.wongki.framework.http

/**
 * @author  wangqi
 * date:    2019/6/17
 * email:   wangqi7676@163.com
 * desc:    .
 */
object HttpCode {
    //成功状态码
    const val SUCCESSFUL = 200

    // 未知失败
    const val UNKNOWN_FAILED = -100
    // 解析数据失败
    const val PARSE_FAILED = -101
    // 连接失败
    const val CONNECTION_FAILED = -102
    // host错误
    const val HOST_UNKNOWN_FAILED = -103
    // 请求超时
    const val TIMEOUT_FAILED = -104
    // 请求失败
    const val REQUEST_FAILED = -105
}