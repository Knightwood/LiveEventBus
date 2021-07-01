package com.kiylx.liveeventbus

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.kiylx.bus.eventbus.utils.LiveEventBus
import com.kiylx.bus.eventbus.core.OstensibleObserver
import com.kiylx.bus.eventbus.ipc.binder.getServiceName
import com.kiylx.bus.eventbus.ipc.binder.services.MessageService
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(),CoroutineScope {

    val job: Job by lazy{ Job() }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*val channel = LiveEventBus.with<String>("one", String::class.java)
        val channel2 = LiveEventBus.with<String>("two", String::class.java)

        for (i in 0..9) {
            channel.post("ooo$i")
        }

        for (i in 10..20) {
            channel2.postDelay("ppp$i", 10L)
        }
        LiveEventBus.with<String>("one", String::class.java).observe(this, object : OstensibleObserver<String?>() {
            override fun onChanged(t: String?) {
                Log.d(TAG, "onChanged1: $t")
            }
        })

        launch(context = coroutineContext){
            delay(1000L)
            LiveEventBus.with<String>("one", String::class.java).observeSticky(this@MainActivity, object : OstensibleObserver<String?>() {
                override fun onChanged(t: String?) {
                    Log.d(TAG, "onChanged2: $t")
                }
            })
        }

        launch(Dispatchers.IO){
            LiveEventBus.with<String>("two", String::class.java).observeSticky(this@MainActivity, object : OstensibleObserver<String?>() {
                override fun onChanged(t: String?) {
                    Log.d(TAG, "onChanged3: $t")
                }
            })
        }*/
        //getSystemServiceName(MessageService::class.java)?.let { Log.d(TAG, it) }
        getServiceName(this)?.let { Log.d(TAG, it) }

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        const val TAG = "MainActivity"
    }
}