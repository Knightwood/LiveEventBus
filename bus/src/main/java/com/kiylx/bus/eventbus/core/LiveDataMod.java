package com.kiylx.bus.eventbus.core;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 创建者 kiylx
 * 创建时间 2020/12/29 17:02
 * packageName：com.kiylx.liveeventbus.live_event_bus
 * 描述：包装livedata
 */
public class LiveDataMod<T> extends MutableLiveData<T> {
    public static final int START_VERSION = -1;

    private static int mVersion = START_VERSION;//自定义mVersion，取代liveData中对version值的控制
    /**
     * uuid:ObserverAgent中的uuid,也就是ostensibleObserver的uuid
     */
    final Map<UUID, ObserverAgent<? super T>> mObservers = new HashMap<>();

    @Override
    public void postValue(T value) {
        super.postValue(value);
        mVersion++;
    }

    @Override
    public void setValue(T value) {
        super.setValue(value);
        mVersion++;
    }

    public int getVersion() {
        return mVersion;
    }

    public void removeObserver(UUID uuid) {
        ObserverAgent<? super T> existing = this.mObservers.get(uuid);
        if (existing != null) {
            this.mObservers.remove(uuid);
            removeObserver(existing.realObserver);
        }
    }

    /**
     *
     * @param observer RealObserver,也就是androidx.lifecycle.Observer;
     */
    @Override
    public void removeObserver(@NonNull Observer<? super T> observer) {
        super.removeObserver(observer);
        ObserverAgent<? super T> existing = get(observer);
        if (existing != null) {
            this.mObservers.remove(existing.ostensibleObserver.uuid);
        }
    }

    @Override
    public void removeObservers(@NonNull LifecycleOwner owner) {
        for (Map.Entry<UUID, ObserverAgent<? super T>> entry : this.mObservers.entrySet()) {
            if (entry.getValue().isAttachedTo(owner)) {
                this.mObservers.remove(entry.getKey());
            }
        }
        super.removeObservers(owner);
    }

    /**
     * 根据realObserver删除与之相关的虚假Observer及其realObserver自身
     * @param realObserver
     */
    public void deleteObserver(@NonNull Observer<? super T> realObserver) {
        for (Map.Entry<UUID, ObserverAgent<? super T>> entry : this.mObservers.entrySet()) {
            if (entry.getValue().realObserver == realObserver) {
                this.mObservers.remove(entry.getKey());
            }
        }
    }
    /**
     *
     * @param realObserver RealObserver,也就是androidx.lifecycle.Observer;
     */
    private ObserverAgent<? super T> get(@NonNull Observer<? super T> realObserver) {
        for (Map.Entry<UUID, ObserverAgent<? super T>> entry : this.mObservers.entrySet()) {
            if (entry.getValue().realObserver == realObserver) {
                return entry.getValue();
            }
        }
        return null;
    }

    public ObserverAgent<? super T> getRenegade() {
        for (Map.Entry<UUID, ObserverAgent<? super T>> entry : this.mObservers.entrySet()) {
            if (entry.getValue().lastVersion != getVersion()) {
                return entry.getValue();
            }
        }
        return null;
    }

    public boolean isHasRenegade() {
        for (Map.Entry<UUID, ObserverAgent<? super T>> entry : this.mObservers.entrySet()) {
            if (entry.getValue().lastVersion != getVersion()) {
                return true;
            }
        }
        return false;
    }

}
