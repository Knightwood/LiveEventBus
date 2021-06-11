package com.kiylx.bus.eventbus.ipc.binder.interfaces;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.kiylx.bus.eventbus.core.OstensibleObserver;

public interface Action {
    /**
     * @param ostensibleObserver
     * @param info               描述所要连接到的服务端的信息
     */
    <T> void observeByCrossProcess(@NonNull OstensibleObserver<T> ostensibleObserver, ServiceInfo info);

    /**
     * 注册一个Observer，生命周期感知，自动取消订阅
     *
     * @param owner              LifecycleOwner
     * @param ostensibleObserver 观察者
     */
    <T> void observe(@NonNull LifecycleOwner owner, @NonNull OstensibleObserver<T> ostensibleObserver);

    /**
     * 注册一个Observer，生命周期感知，自动取消订阅
     * 如果之前有消息发送，可以在注册时收到消息（消息同步）
     *
     * @param owner              LifecycleOwner
     * @param ostensibleObserver 观察者
     */
    <T> void observeSticky(@NonNull LifecycleOwner owner, @NonNull OstensibleObserver<T> ostensibleObserver);

    /**
     * 注册一个Observer，需手动解除绑定
     *
     * @param ostensibleObserver 观察者
     */
    <T> void observeForever(@NonNull OstensibleObserver<T> ostensibleObserver);

    /**
     * 注册一个Observer，需手动解除绑定
     * 如果之前有消息发送，可以在注册时收到消息（消息同步）
     *
     * @param ostensibleObserver 观察者
     */
    <T> void observeStickyForever(@NonNull OstensibleObserver<T> ostensibleObserver);

    /**
     * 通过observeForever或observeStickyForever注册的，需要调用该方法取消订阅
     *
     * @param ostensibleObserver 观察者
     */
    <T> void removeObserver(@NonNull OstensibleObserver<T> ostensibleObserver);

    void removeObservers(@NonNull LifecycleOwner lifecycleOwner);
}
