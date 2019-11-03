package com.wongki.demo

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.wongki.demo.http.*
import com.wongki.demo.model.bean.SearchMusic
import com.wongki.framework.base.BaseHttpLifecycleActivity
import com.wongki.framework.extensions.toast

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : BaseHttpLifecycleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        cacheDir.listFiles().forEach(::println)
        init()
    }

    private fun init() {

        fab.setOnClickListener { view ->
            val name = et_primary_key.text.toString()
            if (name.isEmpty()) {
                Snackbar.make(view, "请输入关键字~", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                return@setOnClickListener
            }

//            MusicServiceCore
//                .newRequester(this) { api -> api.searchMusic(name = name) }
//                .onSuccess { list ->
//                    //show...
//                }
//                .request()
//
//            newMusicRequester(this) { api -> api.searchMusic(name = name) }
//                .onSuccess { list ->
//                    //show...
//                }
//                .request()


            musicService {

                requestServer<ArrayList<SearchMusic.Item>> {

                    lifecycleObserver { this@MainActivity }

                    api {
                        searchMusic(name = name)
                    }

                    observer {
                        // 成功
                        onSuccess {
                            handleSuccess(view,this)
                        }

                        onFailed { code, message ->
                            message.toast()
                            true
                        }
                    }

                }

            }


            newMusicRequester(this) { api -> api.searchMusic(name = name) }
                .onSuccess {
                    handleSuccess(view,this)
                }
                .onFailed { code, message ->
                    message.toast()
                    true
                }
                .request()


//            cacheDir.listFiles().forEach{file -> file.delete() }
//            // 下载
//            for (i in 1..10) {
//                val file = File(cacheDir, "test_$i.apk")
//                Thread {
//                    newDownloadRequester(
//                        lifecycleObserver = this,
//                        filePath = file.absolutePath
//                    ) { api -> api.download("http://yqlapp.geruiter.com/yql_v1.0.4.apk") }
//                        .onStart {
//                            Log.e("DownloaderService", "开始下载 i->$i")
//                        }
//                        .onProgress { progress ->
//                            // Log.e("DownloaderService", "progress:$progress i->$i")
//                        }
//                        .onSuccess { filePath ->
//                            Log.e("DownloaderService", "下载成功 i->$i")
//                        }
//                        .onFailed { code, message ->
//                            Log.e("DownloaderService", "下载失败 i->$i")
//                            false
//                        }
//                        .onCancel { Log.e("DownloaderService", "下载取消 i->$i") }
//                        .request()
//                }.start()
//            }
        }
    }

    private fun handleSuccess(
        view: View,
        arrayList: ArrayList<SearchMusic.Item>?
    ) {
        arrayList?.let { list ->
            if (list.isNotEmpty()) {
                val item = list.first()
                Snackbar.make(view, "《${item.title}》 - ${item.author}", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
