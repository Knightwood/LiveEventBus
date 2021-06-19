package com.kiylx.bus.eventbus.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.kiylx.bus.eventbus.core.interfaces.Action
import com.kiylx.bus.eventbus.core.interfaces.BaseChannel
import com.kiylx.bus.eventbus.utils.Utils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOn
import java.util.*


/**
 * 创建者 kiylx
 * 创建时间 2020/12/28 21:40
 * packageName：com.kiylx.liveeventbus.live_event_bus
 * 描述：推送事件由通道实现.
 * T : 消息类
 */
public open class Channel<T>(val channelName: String) : BaseChannel(), Action<T> {
    private val config: Config by lazy { Config() }
    private val inBox = LiveDataMod<T>()//存放消息的信箱

    @ObsoleteCoroutinesApi
    private var mesSender = actor<T> {
        //Log.d(TAG, "kotlin的channel接到消息: ")
        this.consumeAsFlow()
                .flowOn(Dispatchers.Main).collect {
                    //Log.d(TAG, "flow分发消息")
                    inBox.value = it
                }
    }

    //以下是发送
    @ObsoleteCoroutinesApi
    override fun post(value: T) = postToInBox(value, 0L, null)

    @ObsoleteCoroutinesApi
    override fun postDelay(value: T, delay: Long) = postToInBox(value, delay, null)

    @ObsoleteCoroutinesApi
    override fun postDelay(sender: LifecycleOwner, value: T, delay: Long) =
            postToInBox(value, delay, sender)

    //以下是监听
    override fun observe(owner: LifecycleOwner, ostensibleObserver: OstensibleObserver<T>) {
        observerInternal(owner, ostensibleObserver
                .config()
                .setSticky(false)
                .build()
        )
    }

    override fun observeSticky(owner: LifecycleOwner, ostensibleObserver: OstensibleObserver<T>) {
        observerInternal(owner, ostensibleObserver
                .config()
                .setSticky(true)
                .build()
        )
    }

    override fun observeForever(ostensibleObserver: OstensibleObserver<T>) {
        observerForeverInternal(ostensibleObserver
                .config()
                .setSticky(false)
                .build()
        )
    }

    override fun observeStickyForever(ostensibleObserver: OstensibleObserver<T>) {
        observerForeverInternal(ostensibleObserver
                .config()
                .setSticky(true)
                .build()
        )
    }


    //以下是移除监听
    override fun removeObserver(ostensibleObserver: OstensibleObserver<T>) {
        val existing = inBox.mObservers[ostensibleObserver.uuid]
        if (existing != null) {
            inBox.removeObserver(existing.realObserver)
        }
    }

    override fun removeObservers(owner: LifecycleOwner) {
        inBox.removeObservers(owner)
    }
    //下面是具体实现
    /**
     * 最终的实现
     *
     * @param message
     * @param delay
     * @param owner 数据发送方
     */

    @ObsoleteCoroutinesApi
    private fun postToInBox(message: T, delay: Long, owner: LifecycleOwner? = null) {
        //Log.d(TAG, "postToInBox: 发送消息")
        CoroutineScope(coroutineContext).launch {
            if (delay > 1L)
                delay(delay)
            //带生命周期的发送消息的时候sender处于非激活状态时，消息取消发送
            if (owner != null) {
                if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    mesSender.send(message)
                }
            } else {
                //不带有生命周期
                mesSender.send(message)
            }

        }
    }

    /**
     * @param owner observer所需要的lifecycleOwner
     * @param ostensibleObserver 在observerInternal方法被调用之前，observer与OstensibleObserver之间没有产生联系，ObserverAgent也不存在。
     * 因此，当调用observerInternal时，若从observers这个map中找得到，则说明已经new出来了ObserverAgent，
     * 并与lifecycleOwner建立了关系。
     */
    private fun observerInternal(
            owner: LifecycleOwner,
            ostensibleObserver: OstensibleObserver<T>
    ): ObserverAgent<*>? {
        val existing = putIfAbsent(ostensibleObserver)
        if (existing != null) {
            existing.owner = owner
            //刚刚new出来ObserverAgent，还没有与LifecycleOwner建立联系。
            if (Utils.isMainThread())
                inBox.observe(owner, existing.realObserver)
            else
                launch(context = coroutineContext) {
                    inBox.observe(owner, existing.realObserver)
                }
            return existing
        }
        //不重复监听
        return null
    }

    private fun observerForeverInternal(ostensibleObserver: OstensibleObserver<T>): ObserverAgent<*>? {
        val existing = putIfAbsent(ostensibleObserver)
        if (existing != null) {
            //刚刚new出来ObserverWrapperMod，还没有与LifecycleOwner建立联系。
            if (Utils.isMainThread())
                inBox.observeForever(existing.realObserver)
            else
                launch(context = coroutineContext) {
                    inBox.observeForever(existing.realObserver)
                }
            return existing
        }
        return null
    }

    /**
     * @param ostensibleObserver
     * @return 在map中能找到ostensibleObserver.uuid对应的ObserverAgent实例，返回null。
     * 在map中找不到ostensibleObserver.uuid对应的ObserverAgent实例，放入new出来的值，并返回此实例。
     */
    private fun putIfAbsent(ostensibleObserver: OstensibleObserver<in T>): ObserverAgent<in T>? {
        var v = inBox.mObservers[ostensibleObserver.uuid]
        if (v == null) {
            v = ObserverAgent(ostensibleObserver)
            inBox.mObservers[ostensibleObserver.uuid] = v
            return v.generateObserver(inBox.version)
        }
        return null
    }

    /**
     * 存在，返回null；不存在，new出来，并返回此实例。
     */
    private fun getObserverAgent(ostensibleObserver: OstensibleObserver<in T>): ObserverAgent<in T>? {
        var v = inBox.mObservers[ostensibleObserver.uuid]
        if (v == null) {
            v = ObserverAgent(ostensibleObserver)
            inBox.mObservers[ostensibleObserver.uuid] = v
            return v
        }
        return null
    }

    fun getDataOnce(): T? {
        return inBox.value
    }

    fun convertToJson(): String {
        // TODO: 2021/6/19
    }

    fun config(): Config {
        return config
    }

    inner class Config {
        //可配置项
        private var isCanPush = true //通道是否可以发送消息
        //private var crossProcess = Mode.normal

        fun setCanPushMes(b: Boolean): Config {
            isCanPush = b
            return this
        }

       /* fun setIsUseCrossProcess(mode: Mode): Config {
            crossProcess = mode
            return this
        }*/

        fun build(): Channel<T> {
            return this@Channel
        }
    }

    companion object {
        const val TAG = "消息通道"
    }
}


