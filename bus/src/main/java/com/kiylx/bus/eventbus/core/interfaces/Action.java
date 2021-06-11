package com.kiylx.bus.eventbus.core.interfaces;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.kiylx.bus.eventbus.core.OstensibleObserver;

/**
 * 创建者 kiylx
 * 创建时间 2020/12/29 14:09
 * packageName：com.kiylx.liveeventbus.live_event_bus
 * 描述：
 */
public interface Action<T> {
    /**
     * 进程内发送消息
     *
     * @param value 发送的消息
     */
    void post(T value);

    /**
     * App内发送消息，跨进程使用
     *
     * @param value 发送的消息
     */
    //void postAcrossProcess(T value);

    /**
     * App之间发送消息
     *
     * @param value 发送的消息
     */
    //void postAcrossApp(T value);

    /**
     * 进程内发送消息，延迟发送
     *
     * @param value 发送的消息
     * @param delay 延迟毫秒数
     */
    void postDelay(T value, long delay);

    /**
     * 进程内发送消息，延迟发送，带生命周期
     * 如果延时发送消息的时候sender处于非激活状态，消息取消发送
     *
     * @param sender 消息发送者
     * @param value  发送的消息
     * @param delay  延迟毫秒数
     */
    void postDelay(LifecycleOwner sender, T value, long delay);

    /**
     * 以广播的形式发送一个消息
     * 需要跨进程、跨APP发送消息的时候调用该方法
     * 可使用postAcrossProcess or postAcrossApp代替
     *
     * //@param value 发送的消息

    @Deprecated
    void broadcast(T value);
 */
    /**
     * 以广播的形式发送一个消息
     * 需要跨进程、跨APP发送消息的时候调用该方法
     *
     * @param value      发送的消息
     * @param foreground true:前台广播、false:后台广播
     * @param onlyInApp  true:只在APP内有效、false:全局有效

    void broadcast(T value, boolean foreground, boolean onlyInApp);
*/
    /**
     * 注册一个Observer，生命周期感知，自动取消订阅
     *
     * @param owner    LifecycleOwner
     * @param ostensibleObserver 观察者
     */
    void observe(@NonNull LifecycleOwner owner, @NonNull OstensibleObserver<T> ostensibleObserver);

    /**
     * 注册一个Observer，生命周期感知，自动取消订阅
     * 如果之前有消息发送，可以在注册时收到消息（消息同步）
     *
     * @param owner    LifecycleOwner
     * @param ostensibleObserver 观察者
     */
    void observeSticky(@NonNull LifecycleOwner owner, @NonNull OstensibleObserver<T> ostensibleObserver);

    /**
     * 注册一个Observer，需手动解除绑定
     *
     * @param ostensibleObserver 观察者
     */
    void observeForever(@NonNull OstensibleObserver<T> ostensibleObserver);

    /**
     * 注册一个Observer，需手动解除绑定
     * 如果之前有消息发送，可以在注册时收到消息（消息同步）
     *
     * @param ostensibleObserver 观察者
     */
    void observeStickyForever(@NonNull OstensibleObserver<T> ostensibleObserver);

    /**
     * 通过observeForever或observeStickyForever注册的，需要调用该方法取消订阅
     *
     * @param ostensibleObserver 观察者
     */
    void removeObserver(@NonNull OstensibleObserver<T> ostensibleObserver);

    void removeObservers(@NonNull LifecycleOwner lifecycleOwner);
}
