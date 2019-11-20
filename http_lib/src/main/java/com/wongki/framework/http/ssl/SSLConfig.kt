package com.wongki.framework.http.ssl

import com.wongki.framework.http.HttpDsl
import java.security.SecureRandom
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory

/**
 * @author  wangqi
 * date:    2019/6/4
 * email:   wangqi7676@163.com
 * desc:
 */

@HttpDsl
open class SSLConfig {
    var ssLSocketFactory: SSLSocketFactory? = null
    var hostnameVerifier: HostnameVerifier? = null



    // https手机无须安装代理软件的证书就可以明文查看数据
    internal companion object DEFAULT : SSLConfig() {

        init {
            try {
                val sc = SSLContext.getInstance("TLS")
                sc.init(
                    null, arrayOf(TrustAllManager()),
                    SecureRandom()
                )
                ssLSocketFactory = sc.socketFactory
            } catch (e: Exception) {
            }


            hostnameVerifier = TrustAllHostnameVerifier()
        }
    }
}
