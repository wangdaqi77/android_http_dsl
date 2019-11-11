# 一个阅读性良好的网络框架（kotlin）

## 优势
### 1.良好的阅读性
### 2.使用时无需声明网络请求返回的具体类型
### 3.感知生命周期，界面销毁即取消网络请求
### 4.良好的拓展性

## 例子
### 请求数据
```kotlin
musicService {

    api { searchMusic(name = name) }.thenCall {

        lifecycleObserver = this@MainActivity

        observer {
        
            onSuccess {
                handleSuccess(view,this?.data)
            }

            onFailed { code, message ->
                message.toast()
                true
            }
        }
    }

}
```

## 需要实现的类
### 1.Retrofit的Service接口
```kotlin
interface MusicApi {
    @GET("/searchMusic")
    fun searchMusic(@Query("name")name:String):Observable<CommonResponse<ArrayList<SearchMusic.Item>>>
}
```
### 2.RetrofitServiceCore的实现类
```kotlin
object MusicServiceCore : RetrofitServiceCore<MusicApi>() {
    override val mHost = "https://api.apiopen.top"

    /**
     * 公共请求头
     */
    override fun getCommonRequestHeader(): MutableMap<String, String> = mutableMapOf()

    /**
     * 公共Url参数
     * ex: &sex=1&age=18
     */
    override fun getCommonUrlRequestParams(): MutableMap<String, String> = mutableMapOf()
}
```
### 3.拓展函数(可选)
```kotlin
@RetrofitServiceDslMarker
fun musicService(action:MusicServiceCore.()->Unit){
    MusicServiceCore.action()
}
```
### 4.全局配置(推荐在你的Application进行配置)
```kotlin
globalHttpConfig {
    // 配置统一的Response class
    RESPONSE_SUB_CLASS = MyResponse::class.java
    // 配置成功状态码
    CODE_API_SUCCESS = 200

    // 框架解析失败监听器，可以在此自己解析错误
    onConvertFailed { response, mediaType ->
        /**
         * 当转换失败时被触发
         * 在这里你需要把服务器的错误信息转换成ApiException，
         * 如果没有有效的服务器错误信息需要返回null
         */
        Log.e("onConvertFailed","mediaType:$mediaType")
        var code: Int = -1
        var msg: String = ""
        response.transform(MyResponse::class.java) { target ->
            code = optInt("code", -1)
            msg = optString("message", "")
        }

        if (code != -1) {
            return@onConvertFailed ApiException(code, msg)
        }
        return@onConvertFailed null
    }


    // 全局的错误拦截
    onErrorIntercept { code, message ->
        /**
         * 当请求失败时被触发
         * 当返回true表示当前拦截处理
         */
        when (code) {
            // token 失效
            1001->{
                // 跳转登录页...
                return@onErrorIntercept true
            }
        }
        return@onErrorIntercept false

    }
}
```

