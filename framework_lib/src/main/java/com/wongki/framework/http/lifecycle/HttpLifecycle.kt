package com.wongki.framework.http.lifecycle

import com.wongki.framework.http.base.IRequester
import java.lang.ref.WeakReference

/**
 * http生命周期管理接口
 * @author  wangqi
 * date:    2019/6/18
 * email:   wangqi7676@163.com
 * desc:    .
 */
class HttpLifecycle {
    private val mCaches by lazy { HashMap<WeakReference<IHttpLifecycleObserver?>, ArrayList<WeakReference<IRequester?>>>() }

    /**
     * 根据tag查找requester
     */
    private inline fun find(tag: IHttpLifecycleObserver, onFind: (MutableMap.MutableEntry<WeakReference<IHttpLifecycleObserver?>, java.util.ArrayList<WeakReference<IRequester?>>>) -> Unit) {
        val iterator = mCaches.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            val key = next.key
            val cacheTag = key.get()
            if (cacheTag == null) {
                iterator.remove()
            } else {
                if (cacheTag == tag) {
                    onFind(next)
                    return
                }
            }
        }
    }


    /**
     * 添加单个requester，requester绑定到tag
     */
    fun addRequester(tag: IHttpLifecycleObserver, requester: IRequester) {
        find(tag) { cache ->
            cache.value.add(WeakReference(requester))
            return@addRequester
        }

        val key = WeakReference(tag)
        val value = ArrayList<WeakReference<IRequester?>>()
        mCaches[key] = value
    }

    /**
     * 取消request&&移除单个requester，requester取消绑定tag
     */
    fun removeRequester(tag: IHttpLifecycleObserver, requester: IRequester) {
        find(tag) { cache ->
            val iterator = cache.value.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                val cacheRequest = next.get()
                if (cacheRequest == null) {
                    iterator.remove()
                    break
                } else {
                    if (requester == cacheRequest) {
//                        if (!requester.isCancel()) {
//                            requester.cancel()
//                        }
                        iterator.remove()
                        return@removeRequester
                    }
                }

            }
            return@removeRequester
        }
    }

    /**
     * 取消该tag下绑定的所有request
     */
    fun cancelRequest(tag: IHttpLifecycleObserver) {
        val iterator = mCaches.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            val key = next.key
            val cacheTag = key.get()
            if (cacheTag == null) {
                iterator.remove()
            } else {
                if (cacheTag == tag) {
                    iterator.remove()
                    val requestIterator = next.value.iterator()
                    while (requestIterator.hasNext()) {
                        val request = requestIterator.next().get()
                        if (request != null && !request.isCancel()) {
                            request.cancel()
                        }
                        requestIterator.remove()
                    }
                    return
                }
            }
        }
    }
}