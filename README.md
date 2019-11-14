# dsl 灵活的网络框架

## 优势
### 1.良好的阅读性
### 2.使用时无需声明网络请求返回的具体类型
### 3.感知生命周期，界面销毁即取消网络请求
### 4.良好的拓展性

## 例子
### 请求数据
```kotlin
// 推荐
musicService {
    
    api { searchMusic(name = name) }.thenCall {
        
        lifecycleObserver = this@MainActivity // 生命周期观察器
        config {  } //配置项
        observer { // 观察
            onStart {  } // 开始发起网络请求
            onCancel {  } // 取消网络请求，主动取消或页面销毁时
            onSuccess {  } // 成功
            onFailed { code, message -> true }// 失败
        }
        
    }
    
}
```
或者
```kotlin
MusicServiceCore
    .api {  }
    .thenCall { 
        lifecycleObserver = this
        config {  }
        observer {  }
    }
```

## 需要实现的类
### 1.Retrofit的Service接口
```kotlin
interface MusicApi {
    @GET("/searchMusic")
    fun searchMusic(@Query("name")name:String):Observable<MyResponse<ArrayList<SearchMusic.Item>>>
}
```
### 2.RetrofitServiceCore的实现类
```kotlin
object MusicServiceCore : RetrofitServiceCore<MusicApi>() {
    override fun generateDefaultConfig() = config {
        host = "https://api.apiopen.top"
        connectTimeOut = 10_000
        readTimeOut = 10_000
        writeTimeOut = 10_000
        addHeaders {  }
    }
}
```
### 3.拓展函数
```kotlin
@RetrofitServiceDslMarker
fun musicService(action:MusicServiceCore.()->Unit){
    MusicServiceCore.action()
}
```
### 4.全局配置(推荐在你的Application进行配置)
```kotlin
httpGlobalConfig {
    tag = "http全局配置"
    responseClass = MyResponse::class.java
    successfulCode = 200
    log { message -> Log.d("globalHttpConfig", message)}
    
    onResponseConvertFailed { response, mediaType ->
        var result: ApiException? = null
        when (mediaType) {
            CONTENTTYPE_JSON -> {
                val myResponse = response.transform(MyResponse::class.java) { target ->
                    target.code = optInt("code", -1)
                    target.message = optString("message", "")
                }

                if (myResponse.code != -1) {
                    result = ApiException(myResponse.code, myResponse.message)
                }
            }
        }
        Log.e(
            "onResponseConvertFailed", "解析结果:${result}\n" +
                    "mediaType:$mediaType, response:$response"
        )
        return@onResponseConvertFailed result
    }
    
    addApiErrorInterceptor2FirstNode { code, message ->
        when (code) {
            // token 失效
            1001 -> {
                // 跳转登录页...
                return@addApiErrorInterceptor2FirstNode true
            }
        }
        return@addApiErrorInterceptor2FirstNode false

    }
    
    addHeaders {
        mutableMapOf(
            "key" to "value"
        )
    }
}
```

