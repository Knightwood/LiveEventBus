package com.kiylx.bus.eventbus.core;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

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
    @NotNull
    private final String key;
    @NotNull
    private String messageClassName;
    //通道标识符
    private final UUID channelUUID = UUID.randomUUID();
    //存放消息的信箱
    private final LiveDataMod<T> inBox = new LiveDataMod<>();
    //通道是否可以发送消息
    private boolean isCanPush = true;
    private Config config;
    //postmethod
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

    private void post(T message, long delay, LifecycleOwner sender) {
        if (delay < 1L) {
            if (Utils.isMainThread()) {
                inBox.setValue(message);
            } else {
                mainHandler.post(new PostTask(new Object[]{message, sender}, mPostMethod));
            }
        } else {
            mainHandler.postDelayed(new PostTask(new Object[]{message, sender}, mPostMethod), delay);
        }

    }

    /**
     * args[0]: 消息
     * args[1]: LifecycleOwner
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

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull OstensibleObserver<T> ostensibleObserver) {
        ostensibleObserver.config().setSticky(false);
        observerInternal(owner, ostensibleObserver);
    }

    @Override
    public void observeSticky(@NonNull LifecycleOwner owner, @NonNull OstensibleObserver<T> ostensibleObserver) {
        ostensibleObserver.config().setSticky(true);
        ObserverAgency existing = observerInternal(owner, ostensibleObserver);
    }

    @Override
    public void observeForever(@NonNull OstensibleObserver<T> ostensibleObserver) {
        ostensibleObserver.config().setSticky(false);
        observerForeverInternal(ostensibleObserver);
    }

    @Override
    public void observeStickyForever(@NonNull OstensibleObserver<T> ostensibleObserver) {
        ostensibleObserver.config().setSticky(true);
        ObserverAgency existing = observerForeverInternal(ostensibleObserver);
    }

    @Override
    public void removeObserver(@NonNull OstensibleObserver<T> ostensibleObserver) {
        ObserverAgency<? super T> existing = inBox.mObservers.get(ostensibleObserver.uuid);
        if (existing != null) {
            inBox.removeObserver(existing.realObserver);
        }
    }
    @Override
    public void removeObservers(@NonNull LifecycleOwner owner) {
        this.inBox.removeObservers(owner);
    }

    /**
     * @param owner
     * @param ostensibleObserver 在observerInternal方法被调用之前，observer与observerMod之间没有产生联系，ObserverWrapperMod也不存在。
     *                    因此，当调用observerInternal时，若从observers这个map中找得到，则说明已经new出来了observerWrapperMod，
     *                    并与lifecycleOwner建立了关系。
     */
    private ObserverAgency observerInternal(LifecycleOwner owner, OstensibleObserver<T> ostensibleObserver) {
        ObserverAgency<? super T> existing = putIfAbsent(ostensibleObserver);
        if (existing != null) {
            existing.setOwner(owner);
            //刚刚new出来ObserverWrapperMod，还没有与LifecycleOwner建立联系。
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
        return null;
    }

    private ObserverAgency observerForeverInternal(OstensibleObserver<T> ostensibleObserver) {
        ObserverAgency<? super T> existing = putIfAbsent(ostensibleObserver);
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
    private ObserverAgency<? super T> putIfAbsent(OstensibleObserver<? super T> ostensibleObserver) {
        ObserverAgency<? super T> v = inBox.mObservers.get(ostensibleObserver.uuid);
        if (v == null) {
            v = new ObserverAgency<>(ostensibleObserver);
            inBox.mObservers.put(ostensibleObserver.uuid, v);
            return v.generateObserver(inBox.getVersion());
        }
        return null;
    }

    protected class Config {
        public Config setCanPushMes(boolean b) {
            isCanPush = b;
            return this;
        }
    }
}
