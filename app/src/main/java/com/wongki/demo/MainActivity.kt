package com.wongki.demo

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.view.Menu
import android.view.MenuItem
import com.wongki.demo.http.MusicServiceCore
import com.wongki.demo.http.newMusicRequester
import com.wongki.framework.base.BaseHttpLifecycleActivity
import com.wongki.framework.extensions.toast

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : BaseHttpLifecycleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

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



            newMusicRequester(this) { api -> api.searchMusic(name = name) }
                .onSuccess { result ->
                    result?.let { list ->
                        if (list.isNotEmpty()) {
                            val item = list.first()
                            Snackbar.make(view, "《${item.title}》 - ${item.author}", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show()
                        }

                    }

                }
                .onFailed { code, message ->
                    message?.toast()
                    true
                }
                .request()

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
