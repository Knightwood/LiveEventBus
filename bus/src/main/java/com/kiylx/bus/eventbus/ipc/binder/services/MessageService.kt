package com.kiylx.bus.eventbus.ipc.binder.services

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.RemoteCallbackList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.kiylx.bus.eventbus.IAppLocalInterface
import com.kiylx.bus.eventbus.IClientListener
import com.kiylx.bus.eventbus.IMessageManager
import com.kiylx.bus.eventbus.ipc.binder.model.*
import com.kiylx.bus.eventbus.ipc.binder.util.Const
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * 单应用多进程场景请使用{EXPORTED_OTHER_APP : false}
 * <p>
 * 多应用且多进程场景请使用{EXPORTED_OTHER_APP : true}
 * 同时配置应用包名 {BUS_MAIN_APPLICATION_ID :共享服务且常驻的包名 }
 * <p>
 * eg:<pre><code>{
 *      manifestPlaceholders = [
 *         EXPORTED_OTHER_APP  : true,
 *         BUS_MAIN_APPLICATION_ID: "com.example.bus"
 *     ]
 * }</code></pre>
 */

class MessageService() : Service(), CoroutineScope, LifecycleOwner {

    private val hostCallbackList: RemoteCallbackList<IAppLocalInterface> =
        RemoteCallbackList<IAppLocalInterface>()
    private val clientCallbackList: RemoteCallbackList<IClientListener> =
        RemoteCallbackList<IClientListener>()
    private var hostClientManager: HostClientManager = HostClientManager.INSTANCE


    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    private val job: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        lifecycleRegistry.addObserver(hostClientManager)
    }

    private var mBinder = object : IMessageManager.Stub() {
        override fun registerListener(listener: IClientListener?) {
            if (listener != null) {
                clientCallbackList.register(listener)
                hostClientManager.registerClient(listener)
            }
        }

        override fun unregisterListener(listener: IClientListener?) {
            if (listener != null) {
                clientCallbackList.unregister(listener)
                hostClientManager.unregisterClient(listener)
            }
        }

        /**
         * 发送数据到host
         */
        override fun sendMessage(request: Request) {
            request?.let {
                hostClientManager.sendDataToHostChannel(it)
            }
        }

        /**
         * app里的某一进程的mainbusmanager把数据发送至service，在这里将数据转发给client
         * dataTo为“null”，发送到所有监听此host进程中的channel的client进程
         * dataTo不为”null“，发送到这个指定的client进程
         */
        override fun postMessage(request: Request) {
            if (request.dataTo == "null")
                hostClientManager.dispatchDataToClient(request)
            else
                hostClientManager.sendDataToClient(request)
        }

        /**
         * 删除服务端的一个observer
         */
        override fun deleteObserver(connectInfo: ChannelConnectInfo?) {
            hostClientManager.unConnectToChannel(connectInfo)
        }

        /**
         * 添加一个监听某个channel的observer到服务端
         */
        override fun requestConnect(connectInfo: ChannelConnectInfo?): ConnectResult {
            return hostClientManager.connectToChannel(connectInfo = connectInfo)
        }

        /**
         * 从服务端的某个channel中拉取一次数据,发送到指定client进程
         */
        override fun getMessageOnces(connectInfo: ChannelConnectInfo?) {
            hostClientManager.sendMesToClient2(connectInfo)
        }
        /**
         * 从服务端的某个channel中拉取一次数据,发送到指定client进程
         */
        override fun dataFromHostOnce(message : EventMessage?){
            message?.let { hostClientManager.sendDataToClient(it) }
        }

        //下面是app内的进程，也是发送数据的客户端
        override fun registerAppListener(listener: IAppLocalInterface?) {
            if (listener != null) {
                hostCallbackList.register(listener)
                hostClientManager.registerHostListener(listener)
            }
        }

        override fun unregisterAppListener(listener: IAppLocalInterface?) {
            if (listener != null) {
                hostCallbackList.unregister(listener)
                hostClientManager.unregisterHostListener(listener)
            }
        }


    }

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.markState(Lifecycle.State.CREATED)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED)
        job.cancel()
    }

    override fun onBind(intent: Intent): IBinder? {
        lifecycleRegistry.markState(Lifecycle.State.STARTED)
        //权限验证
        val applicationId: String? = intent.getStringExtra(Const.serviceIntentExtra_permission)
        val check: Int = checkCallingOrSelfPermission("${applicationId}.bus_ipc.PERMISSION")
        return if (check == PackageManager.PERMISSION_DENIED) {
            null
        } else {
            mBinder
        }
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}