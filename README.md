# android_http_dsl
基于OkHttp+Retrofit+RxJava+Kotlin开发封装的网络框架
## 优点

 * 良好的阅读性
 * 发起api请求时无需声明响应的具体类型
 * 生命周期管理，界面销毁即取消网络请求
 * 优雅的配置
 * 错误拦截处理机制
 
 
## 使用前必读
该框架为了良好的阅读性进行了中度封装，是否适合请查看：
 
 * 如果服务的成功状态码存在多种，此框架是不合适的，因为仅限在全局的配置中指定一个[成功状态码](#successfulcode)。
 * 如果服务的返回结构存在多种情况，此框架是不合适的，因为仅限在全局的配置中指定一个[responseClass](#responseclass)。
 * Retrofit+OkHttp的实例是根据配置生成的，这导致开发者在该框架不能使用Retrofit+OkHttp部分原本所支持的能力，例如OkHttp拦截器等等，但是框架提供了日常开发中常用的配置，也方便管理。
 * 使用前请务必查看[配置项](#配置项说明)是否满足你的开发需求！
 
## 名称概念说明

 * XX服务接口：在此服务接口中定义Http API，参考[Retrofit-API Declaration](https://square.github.io/retrofit/#api-declaration)
 * XX服务核心类： 一个XX服务接口对应一个XX服务核心类，该类需要继承[RetrofitServiceCore](/http_lib/src/main/java/com/wongki/framework/http/retrofit/core/RetrofitServiceCore.kt)，参考例子[MusicServiceCore](/app/src/main/java/com/wongki/demo/http/MusicServiceCore.kt)
 * 配置：发起网络请求时用于构建请求器的配置，域名、公参等等，参考[配置](#配置)

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
    override fun generateConfig() = config {
        // ...
        host = "https://XX.com"
    }
}
```
 * 音乐服务的拓展函数
```kotlin
@HttpDsl
fun musicService(action:MusicServiceCore.()->Unit){
    MusicServiceCore.action()
}
```
 * 进行全局的配置
```kotlin
// 在自定义的Application进行配置
httpGlobal {
    newConfig {
        tag = "http全局的配置"
        responseClass = MyResponse::class.java
        successfulCode = 200
        onResponseConvertFailed { response, mediaType ->
            // ...
            return@onResponseConvertFailed result
        }
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
### config
网络请求配置。你在担心[在哪里配置？](#在哪里配置？)，该[如何配置？](#如何配置？)
### observe
网络请求观察器。
 * onStart：当开始请求服务器时。
 * onCancel：当该请求被取消时。
 * onSuccess：当请求成功时。
 * onFailed：当失败时。
## 配置
在每次发起网络请求时根据配置构建请求器。
### 在哪里配置？
可以在三个位置进行配置，按层级大小排序：
 * [全局的配置](#全局的配置)
 * [XX服务核心的配置](#XX服务核心的配置)
 * [单次api请求时的配置](#单次api请求时的配置)
  
注意：如果按照优先级的排序，跟层级大小的顺序恰好相反，这里所说的优先级是使用了[基于XX配置进行配置](#如何配置？)来说的。
#### 全局的配置
推荐在你的Application进行配置。
```kotlin
httpGlobal { 
    newConfig { /*...*/ }
 }
```
必配项：[successfulCode](#successfulcode)、[responseClass](#responseclass)、[onResponseConvertFailedListener](#onresponseconvertfailedlistener)    

#### XX服务核心的配置
当实现XX服务核心类，在重写generateConfig函数时进行配置。
```kotlin
object XXServiceCore : RetrofitServiceCore<XXSERVICE>() {
    override fun generateConfig() = config { /*...*/ }
}
```
#### 单次api请求时的配置
当发起api请求时，在thenCall代码块里进行配置。
```kotlin
XXService {
    api { /*...*/ }.thenCall {
        config { /*...*/ }
    }
}
```
### 如何配置？
分为两类：
 * [基于XX配置进行配置](#基于XX配置进行配置)
 * [全新的独立配置](#全新的独立配置)
#### 基于XX配置进行配置
XX服务核心类的配置可以基于全局的配置进行配置，单次api请求时的配置可以基于其对应的XX服务核心类进行配置。  
```kotlin
config {  }
```
例如在上面的搜索音乐的例子中：  
 * [音乐服务核心类](#需要实现的类和配置)重写了generateConfig()函数，该函数的返回值使用了config代码块，这表示该服务核心类的配置是**基于全局的配置进行配置**的，需要关注[基于XX配置进行配置的影响](#配置项说明)。  
 * [发起搜索音乐请求api](#发起搜索音乐请求api)时，thenCall代码块中使用了config代码块，表示单次api请求时的配置是**基于其对应的XX服务核心类的配置进行配置**的，需要关注[基于XX配置进行配置的影响](#配置项说明)。

#### 全新的独立配置
新创建的配置，和其他配置没有关系。
```kotlin
newConfig {  }
```
例如在上面搜索音乐的例子中改动一下：  
 * [音乐服务核心类](#需要实现的类和配置)重写generateConfig()函数，将该函数的返回值替换为newConfig代码块，这表示该服务核心类的配置是**全新的独立配置**，该配置与全局的配置没有关系。  
```kotlin
override fun generateConfig() = newConfig {  }
```
 * [发起搜索音乐请求api](#发起搜索音乐请求api)时，thenCall代码块中声明了config代码块替换为newConfig代码块，表示单次api请求时的配置是**全新的独立配置**，该配置与其对应的XX服务核心类的配置没有关系。  
```kotlin
// ..
api { searchMusic(name = name) }.thenCall {
    // ..
    newConfig {  } 
}
```
## 配置项说明

名称 | 含义 | 支持全局的配置 | 支持服务核心配置 | 支持api请求配置 | [基于XX配置进行配置](#如何配置？)的影响
-------- | -------- | :--------: | :--------: | :--------: | :--------:
[tag](#tag) | 标签 | ✔️ | ✔️ | ✔️ | 覆盖
[host](#host) | 域名 | ✔️ | ✔️ | ✔️ | 覆盖
[successfulCode](#successfulcode) | 成功码 | ✔️ |  ❌| ❌ | -
[responseClass](#responseclass) | 响应结构 | ✔️ |  ❌| ❌ | -
[connectTimeOut](#connecttimeout) | 连接超时 | ✔️ | ✔️ | ✔️ | 覆盖
[readTimeOut](#readtimeout) | 读取超时 | ✔️ | ✔️ | ✔️ | 覆盖
[writeTimeOut](#writetimeout) | 写入超时 | ✔️ | ✔️ | ✔️ | 覆盖
[logger](#logger) | log | ✔️ | ✔️ | ✔️ | 覆盖
[onResponseConvertFailedListener](#onresponseconvertfailedlistener) | 框架解析失败 | ✔️ | ❌| ❌ | -
[addApiErrorInterceptor2FirstNode](#addapierrorinterceptor2firstnode) | 失败拦截器 | ✔️ | ✔️ | ✔️ | 链表插入头
[addHeaders](#addheaders) | 请求头公参 | ✔️ | ✔️ | ✔️ | 追加，key相同时覆盖
[addUrlQueryParams](#addurlqueryparams) | url公参 | ✔️ | ✔️ | ✔️ | 追加，key相同时覆盖

### tag
标签，目前在框架内打印log时使用。
### host
域名。
### successfulCode
成功码，仅支持全局的配置。[onSuccess](#observe)的触发是基于这个成功码判定的。
### responseClass
响应的结构体，仅支持全局的配置。必须实现[CommonResponse](/http_lib/src/main/java/com/wongki/framework/model/domain/CommonResponse.kt)接口，参考[如何定义Response？](#如何定义Response？)
### connectTimeOut
连接超时，单位ms。
### readTimeOut
读取超时，单位ms。
### writeTimeOut
写入超时，单位ms。
### logger
打印日志。
### onResponseConvertFailedListener
解析失败监听器，当框架层解析结构失败时触发，仅支持全局的配置。如果服务器返回数据遵循Gson的解析规则，那么这个监听始终不会被触发。
  
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
 * 当你能理解这个错误时需要返回[ApiException](/http_lib/src/main/java/com/wongki/framework/http/exception/ApiException.kt)，能不能理解的判定在于你是否可以在[onFailed](#observe)或者[错误拦截器](#addapierrorinterceptor2firstnode)中正确的处理该错误code；  
 * 当你无法理解这个错误时返回null，当返回null时，你会在[onFailed](#observe)中接收到code:[PARSE_FAILED](#内部错误码说明)  
```kotlin
httpGlobal {
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
### addApiErrorInterceptor2FirstNode
api错误拦截器，当请求失败时触发。
  
返回值遵循以下约定：  
 * 当明确需要拦截处理该错误时，返回true。该错误会停止继续传递；  
 * 当不处理该错误时，返回false。该错误会继续传递。  
  
例如：登录失效的错误码为1001，常规的处理是跳转到登录页。  
```kotlin
httpGlobal {
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
因允许可以基于原有的配置进行配置，例如全局的配置设置了该配置项，服务的配置项是基于全局的配置也设置了该配置项，那么服务的该配置项的优先级高于全局的配置的优先级，也就是说会先分发到服务的配置的api错误拦截器，如果服务的api错误拦截器不处理该错误时，会继续分发到全局的api错误拦截器。

### addHeaders
添加公共的请求头。
### addUrlQueryParams
添加公共的url请求公参。

## 四、其他
### 如何定义Response？
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
### 内部错误码说明
常量 | 值 | 含义
------ | :------: | ------
UNKNOWN_FAILED | -100 | 未知失败
PARSE_FAILED | -101 | 解析数据失败
CONNECTION_FAILED | -102 | 连接失败
HOST_UNKNOWN_FAILED | -103 | host错误
TIMEOUT_FAILED | -104 | 请求超时
REQUEST_FAILED | -105 | 请求失败
FILE_NOT_FOUND_FAILED | -106 | 文件未找到
FILE_WRITE_FAILED | -107 | 写入文件失败

详情查看[HttpErrorCode](/http_lib/src/main/java/com/wongki/framework/http/HttpErrorCode.kt)

### 未来改进说明
目前使用框架时上层依然会感知到retrofit的存在，因为需要定义retrofit的服务接口。未来的改进方向是深度封装，上层彻底感知不到底层使用到某个网络框架，这样的好处是底层在未来替换网络框架时上层并不需要改动代码。
