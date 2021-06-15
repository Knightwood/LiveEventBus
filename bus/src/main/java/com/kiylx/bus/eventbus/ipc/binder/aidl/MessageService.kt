package com.kiylx.bus.eventbus.ipc.binder.aidl

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.kiylx.bus.eventbus.core.MainBusManager
import com.kiylx.bus.eventbus.IClientListener
import com.kiylx.bus.eventbus.IMessageManager

/**
 * 进程创建时调用，一般在 Application 的 onCreate 中调用
 * 单应用多进程场景请使用{BUS_SUPPORT_MULTI_APP : false}
 * <p>
 * 多应用且多进程场景请使用{BUS_SUPPORT_MULTI_APP : true}
 * 同时配置应用包名 {BUS_MAIN_APPLICATION_ID :共享服务且常驻的包名 }
 * 主应用必须安装，否则不能正常运行
 * <p>
 * eg:<pre><code>{
 *      manifestPlaceholders = [
 *         BUS_SUPPORT_MULTI_APP  : true,
 *         BUS_MAIN_APPLICATION_ID: "com.example.bus"
 *     ]
 * }</code></pre>
 */

class MessageService() : Service() {
    private var mainBusManager: MainBusManager? = null
    private val clientArr: MutableList<IClientListener> by lazy { mutableListOf() }
    private var mBinder = object : IMessageManager.Stub() {
        override fun registerListener(listener: IClientListener?) {
            if (listener != null) {
                clientArr.add(listener)
                //generate(listener)
            }
        }

        override fun unregisterListener(listener: IClientListener?) {
            if (listener != null)
                clientArr.remove(listener)
        }

        override fun sendMessage() {
            TODO("Not yet implemented")
        }

        override fun deleteObserver() {
            TODO("Not yet implemented")
        }

    }

    override fun onCreate() {
        super.onCreate()
        mainBusManager = MainBusManager.instance
    }

    override fun onDestroy() {
        super.onDestroy()
        mainBusManager=null
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
}