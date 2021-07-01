package com.kiylx.bus.eventbus.ipc.binder.services

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.kiylx.bus.eventbus.IClientListener
import com.kiylx.bus.eventbus.IMessageManager
import com.kiylx.bus.eventbus.ipc.binder.Const
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo
import com.kiylx.bus.eventbus.ipc.binder.model.ConnectResult
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
    private var mCommunicationManager: CommunicationManager = CommunicationManager.instance
    private var mAppCommunicationManager: AppCommunicationManager = AppCommunicationManager.instance

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    private val job: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        lifecycleRegistry.addObserver(mCommunicationManager)
    }

    private var mBinder = object : IMessageManager.Stub() {
        override fun registerListener(listener: IClientListener?) {
            if (listener != null) {
                mCommunicationManager.registerLocate(listener)
            }
        }

        override fun unregisterListener(listener: IClientListener?) {
            if (listener != null)
                mCommunicationManager.unregisterLocate(listener)
        }

        /**
         * 发送数据到服务端
         */
        override fun sendMessage(message: EventMessage?) {
            mCommunicationManager.dispatchMsgToChannel(message)
        }

        /**
         * 删除服务端的一个observer
         */
        override fun deleteObserver(connectInfo: ChannelConnectInfo?) {
            mCommunicationManager.unListenChannel(connectInfo)
        }

        /**
         * 添加一个监听某个channel的observer到服务端
         */
        override fun requestConnect(connectInfo: ChannelConnectInfo?): ConnectResult {
            return mCommunicationManager.listenChannel(connectInfo)
        }

        /**
         * 从服务端的某个channel中拉取一次数据
         */
        override fun getMessageOnces(connectInfo: ChannelConnectInfo?) {
            mCommunicationManager.sendMesToClient(connectInfo)
        }

        override fun registerAppListener(listener: IClientListener?) {
            if (listener != null) {
                mAppCommunicationManager.registerAppListener(listener)
            }
        }

        override fun unregisterAppListener(listener: IClientListener?) {
            if (listener != null)
                mAppCommunicationManager.unregisterAppListener(listener)
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