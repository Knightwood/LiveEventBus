package com.kiylx.bus.eventbus.core

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.kiylx.bus.eventbus.utils.PostTask
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import com.kiylx.bus.eventbus.core.interfaces.Action
import com.kiylx.bus.eventbus.core.interfaces.Mode
import com.kiylx.bus.eventbus.utils.Utils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOn
import java.lang.Runnable
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * 创建者 kiylx
 * 创建时间 2020/12/28 21:40
 * packageName：com.kiylx.liveeventbus.live_event_bus
 * 描述：推送事件由通道实现.
 * T : 消息类
 */
class Channel<T> : Action<T>, CoroutineScope, LifecycleObserver {
    //可配置项
    //通道是否可以发送消息
    private var isCanPush = true
    private var crossProcess = Mode.normal
    private val config: Config by lazy { Config() }
    private val outer = this
    val key: String
    private var messageClassName: String? = null

    //通道标识符
    val uuid = UUID.randomUUID()

    //存放消息的信箱
    private val inBox = LiveDataMod<T>()

    val job: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    @ObsoleteCoroutinesApi
    private var mesSender = actor<T> {
        //Log.d(TAG, "kotlin的channel接到消息: ")
        this.consumeAsFlow()
            .flowOn(Dispatchers.Main).collect {
                //Log.d(TAG, "flow分发消息")
                inBox.value = it
            }

    }

    constructor(key: String) : this(key, null)
    constructor(key: String, messageClassName: String?) {
        this.key = key
        this.messageClassName = messageClassName
    }

    fun config(): Config {
        return config
    }

    //以下是发送
    @ObsoleteCoroutinesApi
    override fun post(value: T) = postToInBox(value, 0L, null)

    @ObsoleteCoroutinesApi
    override fun postDelay(value: T, delay: Long) = postToInBox(value, delay, null)

    @ObsoleteCoroutinesApi
    override fun postDelay(sender: LifecycleOwner, value: T, delay: Long) =
        postToInBox(value, delay, sender)

    override fun postAcrossProcess(value: T) {}
    override fun postAcrossApp(value: T) {}

    //以下是监听
    override fun observe(owner: LifecycleOwner, ostensibleObserver: OstensibleObserver<T>) {
        ostensibleObserver.config().setSticky(false)
        observerInternal(owner, ostensibleObserver)
    }

    override fun observeSticky(owner: LifecycleOwner, ostensibleObserver: OstensibleObserver<T>) {
        ostensibleObserver.config().setSticky(true)
        observerInternal(owner, ostensibleObserver)
    }

    override fun observeForever(ostensibleObserver: OstensibleObserver<T>) {
        ostensibleObserver.config().setSticky(false)
        observerForeverInternal(ostensibleObserver)
    }

    override fun observeStickyForever(ostensibleObserver: OstensibleObserver<T>) {
        ostensibleObserver.config().setSticky(true)
        val existing = observerForeverInternal(ostensibleObserver)
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
    private fun postToInBox(message: T, delay: Long, owner: LifecycleOwner?) {
        when (crossProcess) {
            Mode.binder -> {
            }
            Mode.broadcast -> {
            }
            Mode.normal -> {
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
        }
    }

    /**
     * @param owner
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
     * @return 在map中能找到observer对应的值，返回null。
     * 在map中找不到observer对应的值，放入new出来的值，并返回new出来的值。
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

    inner class Config {
        fun setCanPushMes(b: Boolean): Config {
            outer.isCanPush = b
            return this
        }

        fun setIsUseCrossProcess(mode: Mode): Config {
            outer.crossProcess = mode
            return this
        }

        fun build(): Channel<T> {
            return outer
        }
    }

    companion object {
        const val TAG = "消息通道"
    }
}


