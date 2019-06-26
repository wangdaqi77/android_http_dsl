package com.wongki.framework.http.retrofit.core

import android.util.Log
import com.wongki.framework.model.domain.CommonResponse
import com.wongki.framework.http.base.IRequester
import com.wongki.framework.http.base.IServiceCore
import com.wongki.framework.http.lifecycle.HttpLifecycle
import com.wongki.framework.http.lifecycle.IHttpLifecycleFactory
import com.wongki.framework.http.lifecycle.IHttpLifecycleOwner
import com.wongki.framework.http.retrofit.observer.HttpCommonObserver
import com.wongki.framework.http.retrofit.IRetrofit
import com.wongki.framework.http.retrofit.converter.GsonConverterFactory
import com.wongki.framework.http.retrofit.lifecycle.HttpRetrofitLifecycleHelper
import com.wongki.framework.http.retrofit.lifecycle.IHttpRetrofitLifecycleObserver
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.lang.ref.WeakReference
import java.lang.reflect.ParameterizedType
import java.util.concurrent.TimeUnit


/**
 * @author  wangqi
 * date:    2019/6/6
 * email:   wangqi7676@163.com
 * desc:    retrofit网络请求框架核心类
 *
 */
abstract class RetrofitServiceCore<SERVICE> : IServiceCore, IRetrofit<SERVICE>, IHttpLifecycleOwner/*, ISSL*/ {

    companion object {
        val DAFAULT_onFailed: (Int, String?) -> Boolean = { _, _ -> false }
    }

    /**
     * 拦截处理网络请求中的异常，返回false代表不处理，最终会交给底层处理[HttpCommonObserver.onError]，返回true代表处理
     */
    abstract val onInterceptErrorCode: (Int, String?) -> Boolean
    override val mConnectTimeOut: Long = 15_000
    override val mReadTimeOut: Long = 15_000
    override val mWriteTimeOut: Long = 15_000
    protected val mRetrofit by lazy { generateRetrofit() }

    /**
     * 默认的服务对象
     */
    private val mDefaultService: SERVICE by lazy {
        val paramType = this.javaClass.genericSuperclass as ParameterizedType
        val serviceClazz = paramType.actualTypeArguments[0] as Class<SERVICE>
        mRetrofit.create(serviceClazz)
    }

    /**
     * 创建其他的服务对象
     */
    fun <T> createOther(serviceClazz: Class<T>): T {
        return mRetrofit.create(serviceClazz)
    }

    /**
     * 发起请求
     * @param request 默认的服务对象中具体的api请求方法
     * @param composer 线程调度
     */
    protected fun <RESPONSE_DATA> request(
        request: (SERVICE) -> Observable<CommonResponse<RESPONSE_DATA>>,
        composer: ObservableTransformer<CommonResponse<RESPONSE_DATA>, CommonResponse<RESPONSE_DATA>>
    ): Observable<CommonResponse<RESPONSE_DATA>> {
        return request(mDefaultService).compose(composer)
    }

    /**
     * 用于构建网络请求器
     * @param rxLifecycleObserver tag生命周期感知者
     * @param preRequest retrofit请求
     */
    fun <RESPONSE_DATA> newRequester(
        rxLifecycleObserver: IHttpRetrofitLifecycleObserver? = null,
        preRequest: (SERVICE) -> Observable<CommonResponse<RESPONSE_DATA>>
    ): RetrofitRequester<RESPONSE_DATA> {
        val retrofitRequester = RetrofitRequester<RESPONSE_DATA>()
        retrofitRequester.newRequester(rxLifecycleObserver, preRequest)
        return retrofitRequester
    }


    /**
     * 每次请求都会构建一个retrofit请求器
     */
    open inner class RetrofitRequester<RESPONSE_DATA> : IRequester {
        private lateinit var preRequest: (SERVICE) -> Observable<CommonResponse<RESPONSE_DATA>>
        private var onFailed: (Int, String?) -> Boolean = DAFAULT_onFailed
        /**
         * 返回解析后完整的Response [CommonResponse]
         * 业务层同时设置[onSuccess]和[onFullSuccess]时，只会触发[onFullSuccess]
         */
        private var onFullSuccess: (CommonResponse<RESPONSE_DATA>) -> Unit = { result -> onSuccess(result.result) }
        /**
         * 返回data [CommonResponse.result]
         */
        private var onSuccess: (RESPONSE_DATA?) -> Unit = { _ -> }
        private var rxLifecycleObserver: WeakReference<IHttpRetrofitLifecycleObserver?>? = null
        private var composer: ObservableTransformer<CommonResponse<RESPONSE_DATA>, CommonResponse<RESPONSE_DATA>>? = null
        private var mDisposable: WeakReference<Disposable?>? = null
        fun newRequester(
            rxLifecycleObserver: IHttpRetrofitLifecycleObserver?,
            request: (SERVICE) -> Observable<CommonResponse<RESPONSE_DATA>>
        ): RetrofitRequester<RESPONSE_DATA> {
            rxLifecycleObserver?.let { observer ->
                this.rxLifecycleObserver = WeakReference(observer)
            }
            this.preRequest = request
            return this
        }


        fun compose(composer: ObservableTransformer<CommonResponse<RESPONSE_DATA>, CommonResponse<RESPONSE_DATA>>): RetrofitRequester<RESPONSE_DATA> {
            this.composer = composer
            return this
        }

        /**
         * @param onFailed 业务层返回true是代表业务层处理了该错误码，否则该错误码交给框架层处理
         */
        fun onFailed(onFailed: (Int, String?) -> Boolean): RetrofitRequester<RESPONSE_DATA> {
            this.onFailed = onFailed
            return this
        }

        fun onSuccess(onSuccess: (RESPONSE_DATA?) -> Unit): RetrofitRequester<RESPONSE_DATA> {
            this.onSuccess = onSuccess
            return this
        }


        fun onFullSuccess(onFullSuccess: (CommonResponse<RESPONSE_DATA>) -> Unit): RetrofitRequester<RESPONSE_DATA> {
            this.onFullSuccess = onFullSuccess
            return this
        }

        /**
         * 被观察者在io，观察者在主线程
         */
        private fun applyRetrofitHttpDefaultSchedulers(): ObservableTransformer<CommonResponse<RESPONSE_DATA>, CommonResponse<RESPONSE_DATA>> {
            return ObservableTransformer { observable ->
                observable.subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }
        }

        override fun request(): RetrofitRequester<RESPONSE_DATA> {
            realRequestOnLifecycle(
                preRequest = preRequest,
                composer = composer ?: applyRetrofitHttpDefaultSchedulers(),
                onInterceptErrorCode = onInterceptErrorCode,
                onFailed = onFailed,
                onSuccess = onFullSuccess,
                onStart = { disposable ->
                    this.mDisposable = WeakReference(disposable)
                    //添加请求
                    rxLifecycleObserver?.get()?.let { tag ->
                        getLifecycle().addRequester(tag, this@RetrofitRequester)
                    }
                },
                onComplete = {
                    // 完成remove请求
                    rxLifecycleObserver?.get()?.let { tag ->
                        getLifecycle().removeRequester(tag, this@RetrofitRequester)
                    }
                }
            )
            return this
        }

        override fun isCancel(): Boolean {
            return getDisposable()?.isDisposed ?: false
        }

        override fun cancel() {
            getDisposable()?.dispose()
            rxLifecycleObserver?.get()?.let { tag ->
                getLifecycle().removeRequester(tag, this)
            }

        }

        private fun getDisposable() = mDisposable?.get()
    }

    /**
     * 创建网络请求生命周期管理器
     */
    private object HttpRetrofitLifecycleFactory : IHttpLifecycleFactory {

        override fun createLifecycle(): HttpLifecycle {
            val lifecycle = HttpLifecycle()
            // 将生命周期管理器添加到缓存中
            HttpRetrofitLifecycleHelper.addLifecycle(lifecycle)
            return lifecycle
        }
    }

    /**
     * Log拦截器
     */
    protected object CommonLogInterceptor : HttpLoggingInterceptor.Logger {

        override fun log(message: String) {
            Log.i(javaClass.simpleName, message)
        }
    }

    /**
     * 生成retrofit
     */
    private fun generateRetrofit(): Retrofit {
        val okHttpBuilder = OkHttpClient.Builder()
        //builder.cookieJar(cookieJar);
        okHttpBuilder.addCommonUrlParams(mCommonUrlRequestParams)
        okHttpBuilder.addCommonHeaders(mCommonRequestHeader)
        okHttpBuilder.addCommonPostParams(mCommonPostRequestParams)
        okHttpBuilder.addInterceptor(HttpLoggingInterceptor(CommonLogInterceptor).setLevel(HttpLoggingInterceptor.Level.BODY))

        okHttpBuilder.connectTimeout(mConnectTimeOut, TimeUnit.MILLISECONDS)
        okHttpBuilder.writeTimeout(mWriteTimeOut, TimeUnit.MILLISECONDS)
        okHttpBuilder.readTimeout(mReadTimeOut, TimeUnit.MILLISECONDS)


        // 错误重连
        // builder.retryOnConnectionFailure(true)

//        if (BuildConfig.DEBUG) {
//            getSSLSocketFactory()?.let {
//                okHttpBuilder.sslSocketFactory(it)
//            }
//            getHostnameVerifier()?.let {
//                okHttpBuilder.hostnameVerifier(it)
//            }
//        }

        /*int[] certificates = {R.raw.myssl};//cer文件
        String hosts[] = {HConst.BASE_DEBUG_URL, HConst.BASE_PREVIEW_URL, HConst.BASE_RELEASE_URL, HConst.BASE_RELEASE_SHARE_URL};
        builder.socketFactory(HttpsFactroy.getSSLSocketFactory(context, certificates));
        builder.hostnameVerifier(HttpsFactroy.getHostnameVerifier(hosts));*/
        val retrofit: Retrofit = Retrofit.Builder()
            .client(okHttpBuilder.build())
            .baseUrl(mHost)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit
    }

    /**
     * 发送网络请求
     */
    private fun <RESPONSE_DATA> realRequestOnLifecycle(
        preRequest: (SERVICE) -> Observable<CommonResponse<RESPONSE_DATA>>,
        composer: ObservableTransformer<CommonResponse<RESPONSE_DATA>, CommonResponse<RESPONSE_DATA>>,
        onInterceptErrorCode: (Int, String?) -> Boolean,
        onFailed: (Int, String?) -> Boolean,
        onSuccess: (CommonResponse<RESPONSE_DATA>) -> Unit,
        onStart: (Disposable) -> Unit,
        onComplete: () -> Unit
    ) {
        request(preRequest, composer)
            .subscribe(object :
                HttpCommonObserver<CommonResponse<RESPONSE_DATA>>(onInterceptErrorCode, onFailed, onSuccess) {

                override fun onComplete() {
                    onComplete()
                }

                override fun onSubscribe(d: Disposable) {
                    onStart(d)
                }

            })
    }

    /**
     * 生命周期管理器
     */
    private var mLifecycle: HttpLifecycle? = null


    /**
     * 获取生命周期管理器
     */
    final override fun getLifecycle(): HttpLifecycle {
        if (mLifecycle == null) {
            synchronized(RetrofitServiceCore::class.java) {
                if (mLifecycle == null) {
                    val lifecycle = HttpRetrofitLifecycleFactory.createLifecycle()
                    mLifecycle = lifecycle
                }
            }
        }

        return mLifecycle!!
    }


//    /*****SSL相关******/
//    override fun getSSLSocketFactory() = SSLFactory.DEFAULT.getSSLSocketFactory()
//
//    /*****SSL相关******/
//    override fun getHostnameVerifier() = SSLFactory.DEFAULT.getHostnameVerifier()
}


private fun OkHttpClient.Builder.addCommonHeaders(map: MutableMap<String, String>) {
    if (map.isEmpty()) return
    addInterceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        for (entry in map) {
            requestBuilder.addHeader(entry.key, entry.value)
        }
        return@addInterceptor chain.proceed(requestBuilder.build())
    }
}


private fun OkHttpClient.Builder.addCommonUrlParams(map: MutableMap<String, String>) {
    if (map.isEmpty()) return
    addInterceptor { chain ->
        val urlBuilder = chain.request().url().newBuilder()
        for (entry in map) {
            urlBuilder.addQueryParameter(entry.key, entry.value)
        }
        val requestBuilder = chain.request().newBuilder().url(urlBuilder.build())
        return@addInterceptor chain.proceed(requestBuilder.build())
    }
}


private fun OkHttpClient.Builder.addCommonPostParams(map: MutableMap<String, String>) {
    if (map.isEmpty()) return
//    addInterceptor { chain ->
//        val requestBuilder = chain.request().newBuilder()
//        for (entry in map) {
//            requestBuilder.addHeader(entry.key, entry.value)
//        }
//        return@addInterceptor chain.proceed(requestBuilder.build())
//    }
}

