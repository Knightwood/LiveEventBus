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
 * 描述：
 */
//观察者的包装,虚假观察者与真正观察者之间的桥梁,将真正的observer与OstensibleObserver连接起来
public class ObserverAgency<T> {
    int lastVersion = START_VERSION;
    @NonNull
    private LifecycleOwner mOwner;
    /**
     * 真正的Observer的实例,liveData将会调用此observer的onChanged方法
     */
    Observer<? super T> realObserver = null;
    /**
     * 此UUID与OstensibleObserver中的保持一致，且用于标识ObserverAgency
     */
    @NotNull
    private UUID uuid;
    /**
     * 虚假的Observer实例,ObserverAgency会将真正的observer的方法转交给此实例处理
     */
    @NotNull
    OstensibleObserver<? super T> ostensibleObserver;


    public ObserverAgency(@NonNull OstensibleObserver<? super T> ostensibleObserver) {
        this.ostensibleObserver = ostensibleObserver;
        this.uuid = ostensibleObserver.uuid;
    }
    /**
     * @param observer
     * @return observer是否与已有的ObserverWrapperMod建立联系
     */
    public boolean isAttachedTo(Observer<? super T> observer) {
        return observer.equals(this.realObserver);
    }
    public boolean isAttachedTo(LifecycleOwner owner) {
        return this.mOwner == owner;
    }

    @NonNull
    public LifecycleOwner getOwner() {
        return this.mOwner;
    }

    public void setOwner(@NonNull LifecycleOwner mOwner2) {
        this.mOwner = mOwner2;
    }

    /**
     * 生成LiveData的observer,
     * 并将真正的observer的onChanged方法代理给虚假的observer的onChanged方法
     *
     * @return 返回ObserverAgency本身
     */
    public ObserverAgency<T> generateObserver(int mVersion) {
        if (realObserver == null) {
            realObserver = new Observer<T>() {
                @Override
                public void onChanged(T t) {
                    if (ostensibleObserver.isWantAcceptMessage()) {
                        if (ostensibleObserver.isSticky() && lastVersion == START_VERSION)
                            ostensibleObserver.onChanged(t);
                        else {
                            //非粘性下，拦截推送给新创建的observer的消息
                            if (lastVersion != START_VERSION)
                                ostensibleObserver.onChanged(t);
                        }
                        lastVersion = mVersion;
                    }
                }
            };

        }
        return this;
    }

}
