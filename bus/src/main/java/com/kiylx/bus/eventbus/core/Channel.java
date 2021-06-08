package com.kiylx.bus.eventbus.core;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.kiylx.bus.eventbus.core.interfaces.Action;
import com.kiylx.bus.eventbus.core.interfaces.Mode;
import com.kiylx.bus.eventbus.utils.PostTask;
import com.kiylx.bus.eventbus.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 创建者 kiylx
 * 创建时间 2020/12/28 21:40
 * packageName：com.kiylx.liveeventbus.live_event_bus
 * 描述：推送事件由通道实现.
 * T : 消息类
 */

public class Channel<T> implements Action<T> {
    //可配置项
    //通道是否可以发送消息
    private boolean isCanPush = true;
    private Mode crossProcess = Mode.normal;
    private Config config;
    private final Channel<T> outer = this;

    @NotNull
    private final String key;
    @NotNull
    private String messageClassName;
    //通道标识符
    private final UUID channelUUID = UUID.randomUUID();
    //存放消息的信箱
    private final LiveDataMod<T> inBox = new LiveDataMod<>();
    //PostMethod
    private final PostMethod mPostMethod = new PostMethod();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public Channel(@NonNull String key) {
        this.key = key;
    }

    public Channel(@NotNull String key, @NotNull String messageClassName) {
        this.key = key;
        this.messageClassName = messageClassName;
    }

    public UUID getUuid() {
        return channelUUID;
    }

    @NotNull
    public String getKey() {
        return key;
    }

    public Config config() {
        if (config == null)
            config = new Config();
        return config;
    }

    //以下是发送
    @Override
    public void post(T value) {
        post(value, 0L, null);
    }

    @Override
    public void postDelay(T value, long delay) {
        post(value, delay, null);
    }

    @Override
    public void postDelay(LifecycleOwner sender, T value, long delay) {
        post(value, delay, sender);
    }

    @Override
    public void postAcrossProcess(T value) {

    }

    @Override
    public void postAcrossApp(T value) {

    }

    //以下是监听
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull OstensibleObserver<T> ostensibleObserver) {
        ostensibleObserver.config().setSticky(false);
        observerInternal(owner, ostensibleObserver);
    }

    @Override
    public void observeSticky(@NonNull LifecycleOwner owner, @NonNull OstensibleObserver<T> ostensibleObserver) {
        ostensibleObserver.config().setSticky(true);
        ObserverAgent existing = observerInternal(owner, ostensibleObserver);
    }

    @Override
    public void observeForever(@NonNull OstensibleObserver<T> ostensibleObserver) {
        ostensibleObserver.config().setSticky(false);
        observerForeverInternal(ostensibleObserver);
    }

    @Override
    public void observeStickyForever(@NonNull OstensibleObserver<T> ostensibleObserver) {
        ostensibleObserver.config().setSticky(true);
        ObserverAgent existing = observerForeverInternal(ostensibleObserver);
    }

    //以下是移除监听
    @Override
    public void removeObserver(@NonNull OstensibleObserver<T> ostensibleObserver) {
        ObserverAgent<? super T> existing = inBox.mObservers.get(ostensibleObserver.uuid);
        if (existing != null) {
            inBox.removeObserver(existing.realObserver);
        }
    }

    @Override
    public void removeObservers(@NonNull LifecycleOwner owner) {
        this.inBox.removeObservers(owner);
    }

    //下面是具体实现
    /**
     * 最终的实现
     *
     * @param message
     * @param delay
     * @param sender
     */
    private void post(T message, long delay, LifecycleOwner sender) {
        switch (crossProcess) {
            case binder:
                break;
            case broadcast:
                break;
            case normal:
                if (delay < 1L) {
                    if (Utils.isMainThread()) {
                        inBox.setValue(message);
                    } else {
                        mainHandler.post(new PostTask(mPostMethod, message, sender));
                    }
                } else {
                    mainHandler.postDelayed(new PostTask(mPostMethod, message, sender), delay);
                }
        }
    }

    /**
     * args[0]: 消息
     * args[1]: LifecycleOwner,也就是sender
     */
    private class PostMethod implements PostTask.Method {

        @Override
        public void method(Object[] args) {
            T value = (T) args[0];
            LifecycleOwner owner = (LifecycleOwner) args[1];

            if (value != null && owner != null) {
                //带生命周期的发送消息的时候sender处于非激活状态时，消息取消发送
                if (owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    inBox.setValue(value);
                }
            }
            if (value != null && owner == null) {
                //不带有生命周期
                inBox.setValue(value);
            }

        }
    }

    /**
     * @param owner
     * @param ostensibleObserver 在observerInternal方法被调用之前，observer与OstensibleObserver之间没有产生联系，ObserverAgent也不存在。
     *                           因此，当调用observerInternal时，若从observers这个map中找得到，则说明已经new出来了ObserverAgent，
     *                           并与lifecycleOwner建立了关系。
     */
    private ObserverAgent observerInternal(LifecycleOwner owner, OstensibleObserver<T> ostensibleObserver) {
        ObserverAgent<? super T> existing = putIfAbsent(ostensibleObserver);
        if (existing != null) {
            existing.setOwner(owner);
            //刚刚new出来ObserverAgent，还没有与LifecycleOwner建立联系。
            if (Utils.isMainThread())
                inBox.observe(owner, existing.realObserver);
            else
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        inBox.observe(owner, existing.realObserver);
                    }
                });
            return existing;
        }
        //不重复监听
        return null;
    }

    private ObserverAgent observerForeverInternal(OstensibleObserver<T> ostensibleObserver) {
        ObserverAgent<? super T> existing = putIfAbsent(ostensibleObserver);
        if (existing != null) {
            //刚刚new出来ObserverWrapperMod，还没有与LifecycleOwner建立联系。
            if (Utils.isMainThread())
                inBox.observeForever(existing.realObserver);
            else
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        inBox.observeForever(existing.realObserver);
                    }
                });
            return existing;
        }
        return null;
    }

    /**
     * @param ostensibleObserver
     * @return 在map中能找到observer对应的值，返回null。
     * 在map中找不到observer对应的值，放入new出来的值，并返回new出来的值。
     */
    private ObserverAgent<? super T> putIfAbsent(OstensibleObserver<? super T> ostensibleObserver) {
        ObserverAgent<? super T> v = inBox.mObservers.get(ostensibleObserver.uuid);
        if (v == null) {
            v = new ObserverAgent<>(ostensibleObserver);
            inBox.mObservers.put(ostensibleObserver.uuid, v);
            return v.generateObserver(inBox.getVersion());
        }
        return null;
    }

    public class Config {
        public Config setCanPushMes(boolean b) {
            outer.isCanPush = b;
            return this;
        }

        public Config setIsUseCrossProcess(Mode mode) {
            outer.crossProcess = mode;
            return this;
        }

        public Channel<T> build() {
            return outer;
        }
    }
}
