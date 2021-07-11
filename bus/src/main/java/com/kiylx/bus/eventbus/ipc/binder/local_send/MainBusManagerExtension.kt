package com.kiylx.bus.eventbus.ipc.binder.local_send

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import com.kiylx.bus.eventbus.IAppLocalInterface
import com.kiylx.bus.eventbus.IMessageManager
import com.kiylx.bus.eventbus.core.MainBusManager
import com.kiylx.bus.eventbus.core.interfaces.CrossProcessAction
import com.kiylx.bus.eventbus.ipc.binder.local_receive.CrossProcessBusManager
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage
import com.kiylx.bus.eventbus.ipc.binder.model.Request
import com.kiylx.bus.eventbus.ipc.binder.services.MessageService
import com.kiylx.bus.eventbus.utils.Logs
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainBusManagerExtension private constructor() : CoroutineScope, CrossProcessAction {
    private val job: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job


    private var mContext: Context? = null

    //连接信息，连接到哪个服务
    var pkgName: String? = null
    var clsName: String? = null
    var isBound: Boolean = false
    lateinit var currentProcessName: String

    var mServiceBinder: IMessageManager? = null
    var mLocalCallBack: IAppLocalInterface? = null

    protected val mServiceConnection: ServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                mServiceBinder = IMessageManager.Stub.asInterface(service) as IMessageManager.Stub?
                if (mLocalCallBack == null) {
                    mLocalCallBack = object : IAppLocalInterface.Stub() {

                        override fun getLocateFrom(): String {
                            return currentProcessName
                        }

                        override fun sendDataToHost(data: Request?) {
                            data?.let {
                                MainBusManager.instance.getChannel2(data.channelName)
                                    ?.postJson(data.json)
                            }
                        }

                        override fun getDataOnce(data: Request?) {
                            launch(coroutineContext){
                                data?.let {
                                    val result= async(coroutineContext){
                                        val json=  MainBusManager.instance.getChannel2(data.channelName)
                                            ?.dataConvertToJson()
                                        if (json.isNullOrEmpty())
                                            //json为空，抛异常
                                            mServiceBinder?.dataFromHostOnce(EventMessage(it.dataFrom,it.dataTo,it.channelName,it.connectService," ",json!!))
                                    }
                                }
                            }
                        }
                    }
                }
                if (mServiceBinder == null) return
                try {
                    mServiceBinder!!.registerAppListener(mLocalCallBack)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                mServiceBinder = null
                Logs.d(CrossProcessBusManager.tag, "onServiceDisconnected, process = $name")
            }
        }
    }

    protected suspend fun bindService(context: Context): Boolean {
        //连接到服务端
        val intent = Intent(context, MessageService::class.java)
        isBound = context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        if (!isBound) {
            Logs.e(tag, "Can not find the class :${pkgName}")
            if (Logs.DEBUG >= Logs.nowLevel) {
                throw RuntimeException("Can not find the class :$pkgName")
            }
        }
        return isBound
    }


    protected fun unbindService() {
        if (isBound) {
            if (mServiceBinder != null && mServiceBinder!!.asBinder().isBinderAlive) {
                try {
                    // 取消注册
                    mServiceBinder!!.unregisterAppListener(mLocalCallBack)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
            mContext?.unbindService(mServiceConnection)
            isBound = false
        }
        mContext = null
        mLocalCallBack = null
        mServiceBinder = null
        job.cancel()
    }

    override fun destroySelf() {
        unbindService()
    }

    //将数据发送到service，service会将数据转发给client
    override suspend fun <T> postDataToService(
        value: String,
        dataFrom: String,
        dataTo: String,
        thisChannelName: String,
        connectService: String,
        dataType: String
    ) {
        if (!isBound) {
            mContext?.let { context ->
                bindService(context = context)
                mServiceBinder?.let {
                    it.registerAppListener(mLocalCallBack)
                    it.postMessage(
                        Request(
                            dataFrom,
                            dataTo,
                            thisChannelName,
                            connectService,
                            dataType,
                            value
                        )
                    )
                }

            }
        } else {
            mServiceBinder?.postMessage(
                Request(
                    dataFrom,
                    dataTo,
                    thisChannelName,
                    connectService,
                    dataType,
                    value
                )
            )
        }

    }


    companion object {
        const val tag = "跨进程-发送数据端"

        @JvmStatic
        val INSTANCE: MainBusManagerExtension
                by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { MainBusManagerExtension() }
    }
}