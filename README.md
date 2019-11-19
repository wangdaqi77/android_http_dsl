# android_http_dsl
###### 基于okHttp+retrofit+rxJava+kotlin开发的网络框架
## 优势

 * 良好的阅读性
 * 发起api请求时无需声明响应的具体类型
 * 生命周期管理，界面销毁即取消网络请求
 * 优雅的配置
 * 错误拦截处理机制
## 概念

 * xx服务接口，kotlin接口中定义的HTTP API，参考[Retrofit-API Declaration](https://square.github.io/retrofit/#api-declaration)
 * xx服务核心类，基于xx服务接口声明的服务核心类，需要继承[RetrofitServiceCore]，参考[核心类例子](/app/src/main/java/com/wongki/demo/http/MusicServiceCore.kt)
 * 配置，网络请求的配置，域名、公参等等..，参考[配置](#配置)
 
## 例子
### 搜索音乐
#### 发起搜索音乐请求api
```kotlin
// 推荐
musicService {
    // 在音乐服务核心下发起搜索音乐的api请求
    api { searchMusic(name = name) }.thenCall {
        
        lifecycleObserver = this@MainActivity // 生命周期观察器
        config {  } // api请求的配置项
        observer { // 观察器
            onSuccess {  } // 成功
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

#### 需要实现的类和配置
 * 音乐服务接口
```kotlin
interface MusicApi {
    @GET("/searchMusic")
    fun searchMusic(@Query("name")name:String):Observable<MyResponse<ArrayList<SearchMusic.Item>>>
}
```
 * 音乐服务核心类
```kotlin
object MusicServiceCore : RetrofitServiceCore<MusicApi>() {
    override fun generateDefaultConfig() = config {
        // ...
        host = "https://xx.com"
    }
}
```
 * 音乐服务的拓展函数
```kotlin
@RetrofitServiceDslMarker
fun musicService(action:MusicServiceCore.()->Unit){
    MusicServiceCore.action()
}
```
 * 进行全局配置
```kotlin
// 在自定义的Application进行配置
httpGlobalConfig {
    tag = "http全局配置"
    responseClass = MyResponse::class.java
    successfulCode = 200
    onResponseConvertFailed { response, mediaType ->
        // ...
        return@onResponseConvertFailed result
    }
}
```
## 发起api请求的说明
### api
kotlin接口中定义的HTTP API，例如[音乐服务接口](#需要实现的类和配置)
### thenCall
该函数会构建请求器并真正发起网络请求。
### lifecycleObserver
网络请求的生命周期观察器。设置该参数意味着一次网络请求和该观察器存在绑定关系，作用是避免内存泄漏。  
lifecycleObserver的值需要实现[IHttpDestroyedObserver](/http_lib/src/main/java/com/wongki/framework/http/retrofit/lifecycle/IHttpDestroyedObserver.kt)接口，该接口里有clearRequest()的默认实现函数，该函数会将所有绑定到自己的请求器进行取消请求处理。你需要在你明确需要取消请求的位置主动调用clearRequest()，一般需求下是在Activity.onDestroy()、Fragment.onDestroyView()、ViewModel.onCleared()或者根据你的实际场景来判定。
### 配置
配置有三种：[全局配置](#全局配置)、[基于xx配置进行配置](#基于xx配置进行配置)、[全新的独立配置](#全新的独立配置)

#### 全局配置
```kotlin
// 推荐在你的自定义application进行全局配置。  
httpGlobalConfig {  }
```
  
必配项(只支持全局配置)：  
[successfulCode](#successfulcode)、[responseClass](#responseclass)、[onResponseConvertFailedListener](#onresponseconvertfailedlistener)    


#### 基于xx配置进行配置
```kotlin
config{  }
```
例如在上面的搜索音乐的例子中：  
 * [音乐服务核心类](#需要实现的类和配置)重写了generateDefaultConfig()函数，该函数的返回值使用了config代码块，这表示该服务核心类的默认配置是**基于[全局配置](#全局配置)进行配置**的，需要关注配置项的[覆盖，追加值等](#配置项说明)。  
 * [发起搜索音乐请求api](#发起搜索音乐请求api)时，thenCall代码块中使用了config代码块，表示发起api请求时的配置是**基于其所属的服务核心类的默认配置进行配置**的，需要关注配置项的[覆盖，追加值等](#配置项说明)。

#### 全新的独立配置
```kotlin
newConfig{  }
```
例如在上面搜索音乐的例子中改动一下：  
 * [音乐服务核心类](#需要实现的类和配置)重写了generateDefaultConfig()函数，将该函数的返回值替换为为newConfig代码块，这表示该服务核心类的默认配置是**全新的独立配置**，它所能配置的配置项与[全局配置](#全局配置)没有关系。  
```kotlin
override fun generateDefaultConfig() = newConfig {  }
```
 * [发起搜索音乐请求api](#发起搜索音乐请求api)时，thenCall代码块中声明了[config](#基于xx配置进行配置)代码块替换为newConfig代码块，表示发起api请求时的配置是**全新的独立配置**，它所能配置的配置项与其所属的服务核心类的默认配置没有关系。  
```kotlin
// ..
api { searchMusic(name = name) }.thenCall {
    // ..
    newConfig {  } 
}
```
### observe
网络请求观察器。
 * onStart：当开始请求服务器时。
 * onCancel：当该请求被取消时。
 * onSuccess：当请求成功时。
 * onFailed：当失败时。

## 配置项说明

名称 | 含义 | 支持全局配置 | 支持服务配置 | 支持单次api配置 | [config代码块](#基于xx配置进行配置)
-------- | -------- | :--------: | :--------: | :--------: | :--------:
[tag](#tag) | 标签 | ✔️ | ✔️ | ✔️ | 覆盖
[host](#host) | 域名 | ✔️ | ✔️ | ✔️ |覆盖
[successfulCode](#successfulcode) | 成功码 | ✔️ |  ❌| ❌ | -
[responseClass](#responseclass) | 响应结构 | ✔️ |  ❌| ❌ | -
[connectTimeOut](#connecttimeout) | 连接超时 | ✔️ | ✔️ | ✔️ | 覆盖
[readTimeOut](#readtimeout) | 读取超时 | ✔️ | ✔️ | ✔️ | 覆盖
[writeTimeOut](#writetimeout) | 写入超时 | ✔️ | ✔️ | ✔️ | 覆盖
[logger](#logger) | log | ✔️ | ✔️ | ✔️ | 覆盖
[onResponseConvertFailedListener](#onresponseconvertfailedlistener) | 框架解析失败 | ✔️ | ❌| ❌ | -
[apiErrorInterceptorNode](#apierrorinterceptornode) | 失败拦截器 | ✔️ | ✔️ | ✔️ | 链表插入头
[addHeaders](#addheaders) | 请求头公参 | ✔️ | ✔️ | ✔️ | 追加
[addUrlQueryParams](#addurlqueryparams) | url公参 | ✔️ | ✔️ | ✔️ | 追加

### tag
标签，目前在框架内打印log时使用。
### host
域名。
### successfulCode
成功码，[onSuccess](#observe)的触发是基于这个成功码判定的。
### responseClass
响应的结构体。必须继承[CommonResponse](/http_lib/src/main/java/com/wongki/framework/model/domain/CommonResponse.kt)
### connectTimeOut
连接超时，单位ms。
### readTimeOut
读取超时，单位ms。
### writeTimeOut
写入超时，单位ms。
### logger
打印日志。
### onResponseConvertFailedListener
解析失败监听器，当框架层解析结构失败时触发。如果服务器返回数据遵循Gson的解析规则，那么这个监听始终不会被触发。
  
例如：
当请求搜索音乐api时，服务器返回了以下json
```json
{
	"meta": {
		"code": 200,
		"message": "成功!"
	},
	"result": ""
}
```
但是[音乐服务接口](#需要实现的类和配置)声明响应体MyResponse<T>的result类型为ArrayList<SearchMusic.Item>，当此次请求正常响应时，那么此方法必定被执行，因为result为String被解析成ArrayList<SearchMusic.Item>是不允许的。  
  
返回值遵循以下约定：  
 * 当你能理解这个错误时需要返回[ApiException]，能不能理解的判定在于你是否可以在[onFailed](#observe)或者[错误拦截器](#apierrorinterceptornode)中正确的处理该错误code；  
 * 当你能理解这个错误时返回null，当返回null时，你会在[onFailed](#observe)中接收到code:[HttpErrorCode.PARSE_FAILED]  
```kotlin
httpGlobalConfig {
    // ...
    onResponseConvertFailed { response, mediaType ->
        var result: ApiException? = null
        when (mediaType) {
            CONTENTTYPE_JSON -> {
                // 这里示例的是将json转换成JSONObject再转换成MyResponse
                val myResponse = response.transform(MyResponse::class.java) { target ->
                    target.code = optInt("code", -1)
                    target.message = optString("message", "")
                }

                if (myResponse.code != -1) {
                    result = ApiException(myResponse.code, myResponse.message)
                }
            }
        }
        return@onResponseConvertFailed result
    }
}
```
### apiErrorInterceptorNode
api错误拦截器，当请求失败时触发。
  
返回值遵循以下约定：  
 * 当明确需要拦截处理该错误时，返回true。该错误会停止继续传递；  
 * 当不处理该错误时，返回false。该错误会继续传递。  
  
例如：登录失效的错误码为1001，常规的处理是跳转到登录页。  
```kotlin
httpGlobalConfig {
    // ...
    addApiErrorInterceptor2FirstNode { code, message ->
        when (code) {
            // token 失效
            1001 -> {
                // 跳转登录页...
                jumpLogin()
                return@addApiErrorInterceptor2FirstNode true
            }
        }
        return@addApiErrorInterceptor2FirstNode false
    }
}
```
###### 因允许可以基于原有的配置进行配置，例如全局配置设置了该配置项，服务的配置项是基于全局的配置也设置了该配置项，那么服务的该配置项的优先级高于全局配置的优先级，也就是说会先分发到服务的配置的api错误拦截器，如果服务的api错误拦截器不处理该错误时，会继续分发到全局的api错误拦截器。

### addHeaders
添加公共的请求头。
### addUrlQueryParams
添加公共的url请求公参。

## 其他
#### 定义响应体
必须继承[CommonResponse](/http_lib/src/main/java/com/wongki/framework/model/domain/CommonResponse.kt)
  
第一种情况：  
```json
{
	"code": 200,
	"message": "成功!",
	"result": {}
}
```
```kotlin
class MyResponse<T> : CommonResponse<T> {
    @SerializedName("code")
    override var code: Int = -1
    @SerializedName("message")
    override var message: String? = null
    @SerializedName("result")
    override var data: T? = null
}
```
第二种情况： 
```json
{
	"meta": {
		"code": 200,
		"message": "成功!"
	},
	"data": {}
}
```
```kotlin
class MyResponse<T> : CommonResponse<T> {
    @SerializedName("meta")
    private var meta :Meta? = null
    override var code: Int = meta?.code ?:-1
    override var message: String? = meta?.message
    @SerializedName("data")
    override var data: T? = null
   
    class Meta{
        @SerializedName("code")
        var code = -1
        @SerializedName("message")
        var message: String? = null
    }
}
```