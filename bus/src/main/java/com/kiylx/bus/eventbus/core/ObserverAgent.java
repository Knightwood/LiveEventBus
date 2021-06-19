package com.kiylx.bus.eventbus.core;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.kiylx.bus.eventbus.core.LiveDataMod.START_VERSION;

/**
 * 创建者 kiylx
 * 创建时间 2020/12/31 20:21
 * packageName：com.kiylx.liveeventbus.live_event_bus
 * 描述：虚假observer（OstensibleObserver类）与真正observer（androidx.lifecycle.Observer类）之间的桥梁,不是对于真正的observer的包装。
 * 将真正的observer与OstensibleObserver连接起来,代理真正的observer的一切
 */
class ObserverAgent<T> {
    int observerLastVersion = START_VERSION;//取代observer的version值控制
    @NonNull
    @Deprecated
    private LifecycleOwner mOwner;
    /**
     * 真正的Observer的实例,liveData将会调用此observer的onChanged方法
     */
    Observer<? super T> realObserver = null;
    /**
     * 此UUID与OstensibleObserver中的保持一致，且用于标识ObserverAgent
     */
    @NotNull
    private UUID uuid;
    /**
     * 虚假的Observer实例,ObserverAgent会将真正的observer的方法转交给此实例处理
     */
    @NotNull
    OstensibleObserver<? super T> ostensibleObserver;

    public ObserverAgent(@NonNull OstensibleObserver<? super T> ostensibleObserver) {
        this.ostensibleObserver = ostensibleObserver;
        this.uuid = ostensibleObserver.getUuid();
    }

    /**
     * @param observer
     * @return observer是否与已有的ObserverAgent建立联系
     */
    public boolean isAttachedTo(Observer<? super T> observer) {
        return observer.equals(this.realObserver);
    }

    public boolean isAttachedTo(LifecycleOwner owner) {
        return this.mOwner == owner;
    }

    @NonNull
    @Deprecated
    public LifecycleOwner getOwner() {
        return this.mOwner;
    }

    @Deprecated
    public void setOwner(@NonNull LifecycleOwner mOwner2) {
        this.mOwner = mOwner2;
    }

    /**
     * 生成LiveData的observer（androidx.lifecycle.Observer类）,
     * 并将真正的observer,且把onChanged方法代理给虚假的observer（OstensibleObserver类）的onChanged方法。
     *
     * 若livedata已经存在，生成新的observer附加给已存在的livedata时，
     *          observer的lastVersion==-1，livedata的mVersion>-1,
     *          此时非粘性下observer不应接收到livedata之前的事件。
     * 若livedata与observer都是新的，
     *          observer的lastVersion==-1，livedata的mVersion==-1，
     *          此时非粘性下observer应接收到livedata之前的事件。
     *
     * @param mVersion livedata的mVersion值
     * @return 返回ObserverAgent本身
     */
    public ObserverAgent<T> generateObserver(int mVersion) {
        if (realObserver == null) {
            realObserver = new Observer<T>() {
                @Override
                public void onChanged(T t) {
                    switch (ostensibleObserver.config().getCrossProcess()){
                        case normal:
                            if (ostensibleObserver.config().isWantAcceptMessage()) {
                            if (ostensibleObserver.config().isSticky() && observerLastVersion == START_VERSION)
                                ostensibleObserver.onChanged(t);
                            else {
                                //非粘性下，拦截推送给新创建的observer的消息
                                if (mVersion == START_VERSION && observerLastVersion == START_VERSION)
                                    ostensibleObserver.onChanged(t);
                            }
                            observerLastVersion = mVersion;
                        }
                            break;
                        case broadcast:
                            break;
                        case binder:
                           ostensibleObserver.onChanged(convertToJson(t));
                           observerLastVersion = mVersion;
                            break;
                    }

                }
            };

        }
        return this;
    }

    private T convertToJson(T t) {
        return null;
    }

}
