package com.kiylx.bus.eventbus.ipc.binder

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import com.kiylx.bus.eventbus.IClientListener
import com.kiylx.bus.eventbus.IMessageManager
import com.kiylx.bus.eventbus.ipc.binder.interfaces.ChannelsManagerAction
import com.kiylx.bus.eventbus.ipc.binder.interfaces.CrossProcessBusManagerAction
import com.kiylx.bus.eventbus.ipc.binder.model.*
import com.kiylx.bus.eventbus.utils.Logs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * 一个ChannelsManager连接到一个service,并管理着一堆channel，channel下有很多observers
 */
class ChannelsManager(context: Context,
                      var crossProcessBusManagerAction: CrossProcessBusManagerAction) : ChannelsManagerAction, CoroutineScope {
    private var mCrossProcessBusManagerAction = crossProcessBusManagerAction
    var uuid: UUID = UUID.randomUUID()
        private set
    private val job: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    //存储着所有消息通道，不同消息通道可能会连接不同的服务端<channelName,crossChannel>
    private val channelList: MutableMap<String, CrossChannel<*>> by lazy { mutableMapOf() }
    private var mContext: Context? = context

    var mProcessManager: IMessageManager.Stub? = null
    var mProcessCallback: IClientListener.Stub? = null

    //连接信息，连接到哪个服务
    var pkgName: String? = null
    var clsName: String? = null
    var isBound: Boolean = false
    lateinit var currentProcessName:String

    /**
     * 从服务端拉取一次消息,
     */
    private fun getRemoteDataOnces(channelInfo: ChannelConnectInfo) {
        mProcessManager?.getMessageOnces(channelInfo)
    }

    /**
     *  向服务端发送数据或消息
     */
    override fun send(data: EventMessage) {
        mProcessManager?.sendMessage(data)
    }

    /**
     * 1.请求远程服务端删除这个连接
     * 2.删除本地的channel
     * 3.channelsManager中不再有任何一个channel,销毁自身，并请求上层删除channelsManager
     */
    override fun destroyChannel(connectInfo: ChannelConnectInfo) {
        synchronized(channelList) {
            channelList.remove(connectInfo.channelName)// 删除本地channel
            mProcessManager?.deleteObserver(connectInfo)//请求服务端删除服务端对特定channel监听的服务端observer
            if (channelList.isEmpty()) {
                mCrossProcessBusManagerAction.deleteChannelsManager(pkgName+clsName)
                destroySelf()
            }
        }
    }

    fun initManager(context: Context, connectInfo: ChannelConnectInfo) {
        this.pkgName=connectInfo.pkgName
        this.clsName=connectInfo.clsName
        currentProcessName= getProcessName(context)
        bindService(context)
    }

    private fun destroySelf() {
        unbindService()
    }

    fun <T : Any> getChannel(channelInfo: ChannelConnectInfo, clazz: Class<T>): CrossChannel<T>? {
        synchronized(channelList) {
            var channel: CrossChannel<*>? = channelList[channelInfo.channelName]
            if (channel == null) {
                // 2021/6/16 与service建立连接并获取消息,并验证service端对应本地的channel是否存在
                var connect: ConnectResult? = mProcessManager?.requestConnect(channelInfo)
                if (connect == null || connect.code != ResultCode.success) {
                    return null
                }
                channel = CrossChannel(this@ChannelsManager, channelInfo, clazz)
                channelList[channelInfo.channelName] = channel
            }
            return channel as CrossChannel<T>
        }
    }

    private var mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mProcessManager = IMessageManager.Stub.asInterface(service) as IMessageManager.Stub?
            if (mProcessCallback == null) {
                mProcessCallback = object : IClientListener.Stub() {
                    override fun notifyDataChanged(message: EventMessage?) {
                        //("根据参数，把message解析成特定类型，发送给客户端channel")
                        if (message != null) {
                            channelList[message.channel]?.notifyObserver(message)
                        }
                    }

                    override fun getLocateFrom(): String {
                       return currentProcessName
                    }

                }
            }
            if (mProcessManager == null) return
            try {
                mProcessManager!!.registerListener(mProcessCallback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mProcessManager = null
            Logs.d(CrossProcessBusManager.tag, "onServiceDisconnected, process = $name")
        }
    }

    private fun bindService(context: Context) {
        //连接到服务端
        val intent = Intent(Intent.ACTION_MAIN)
        intent.component = ComponentName(pkgName!!, clsName!!)
        isBound = context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        if (!isBound) {
            Logs.e(CrossChannel.tag, "Can not find the host app under :${pkgName}")
            if (Logs.DEBUG >= Logs.nowLevel) {
                throw RuntimeException("Can not find the host app under :" + pkgName)
            }
        }
    }


    private fun unbindService() {
        if (isBound) {
            mContext?.unbindService(mServiceConnection)
            if (mProcessManager != null && mProcessManager!!.asBinder().isBinderAlive()) {
                try {
                    // 取消注册
                    mProcessManager!!.unregisterListener(mProcessCallback)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
            isBound = false
            mContext = null
            mProcessCallback = null
            mProcessManager = null
            job.cancel()
        }

    }


    companion object {
        const val tag = "ChannelsManager"
    }
}