# 一个阅读性良好的网络框架（必须使用kotlin）

## 优势
### 1.良好的阅读性
### 2.使用时无需声明网络请求返回的具体类型
### 3.感知生命周期，界面销毁即取消网络请求
### 4.良好的拓展性

## 例子
### 1.请求数据
    MusicServiceCore
    .newRequester(this) { api -> api.searchMusic(name = name) }
    .onSuccess { list ->
        //show...
    }
    .request()
    
或者
    // 需要手写拓展函数
    newMusicRequester(this) { api -> api.searchMusic(name = name) }
    .onSuccess { list ->
        //show...
    }
    .request()
    
### 2.取消请求
    val requester = MusicServiceCore.newRequester(this) { api -> api.searchMusic(name = name) }.request()
    requester.cancel()
    
## 需要实现的类
### 1.Retrofit的Service接口
    interface MusicApi {
        @GET("/searchMusic")
        fun searchMusic(@Query("name")name:String):Observable<CommonResponse<ArrayList<SearchMusic.Item>>>
    }
    
### 2.RetrofitServiceCore的实现类
    object MusicServiceCore : RetrofitServiceCore<MusicApi>() {
        override val mHost = "https://api.apiopen.top"

        /**
         * 公共请求头
         */
        override var mCommonRequestHeader: MutableMap<String, String> = mutableMapOf()

        /**
         * 公共Url参数
         * ex: &sex=1&age=18
         */
        override var mCommonUrlRequestParams: MutableMap<String, String> = mutableMapOf()

        /**
         * （可选）
         * 拦截处理网络请求中的异常错误码,详情查看[HttpCommonObserver.onError]
         */
        override var errorInterceptor: ErrorInterceptor? = object : ErrorInterceptor() {
            override fun onInterceptErrorCode(code: Int, message: String?): Boolean {
                return false
            }
        }
    }
### 3.拓展函数(可选)
    fun <R> Any.newMusicRequester(lifecycleObserver: IHttpRetrofitLifecycleObserver? = null, preRequest: (MusicApi) -> Observable<CommonResponse<R>>) = MusicServiceCore.newRequester(lifecycleObserver, preRequest)

