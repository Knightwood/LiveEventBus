package com.kiylx.liveeventbus

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kiylx.bus.eventbus.LiveEventBus
import com.kiylx.bus.eventbus.core.OstensibleObserver
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(),CoroutineScope {

    val job: Job by lazy{ Job() }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val channel = LiveEventBus.with<String>("one")
        val channel2 = LiveEventBus.with<String>("two")

        for (i in 0..9) {
            channel.post("ooo$i")
        }

        for (i in 10..20) {
            channel2.postDelay("ppp$i",10L)
        }
        LiveEventBus.with<String>("one").observe(this, object : OstensibleObserver<String?>() {
            override fun onChanged(s: String?) {
                Log.d(TAG, "onChanged1: $s")
            }
        })

        launch(context = coroutineContext){
            delay(1000L)
            LiveEventBus.with<String>("one").observeSticky( this@MainActivity, object : OstensibleObserver<String?>() {
                override fun onChanged(s: String?) {
                    Log.d(TAG, "onChanged2: $s")
                }
            })
        }

        launch(Dispatchers.IO){
            LiveEventBus.with<String>("two").observeSticky( this@MainActivity, object : OstensibleObserver<String?>() {
                override fun onChanged(s: String?) {
                    Log.d(TAG, "onChanged3: $s")
                }
            })
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        const val TAG = "MainActivity"
    }
}