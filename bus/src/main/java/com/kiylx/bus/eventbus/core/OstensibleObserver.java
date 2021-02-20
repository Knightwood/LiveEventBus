package com.kiylx.bus.eventbus.core;

import java.util.UUID;

/**
 * 创建者 kiylx
 * 创建时间 2021/1/2 13:17
 * packageName：com.kiylx.bus.eventbus.core
 * 描述：虚假的Observer,描述对于真正的observer的配置信息
 */
public abstract class OstensibleObserver<T> {
    public final UUID uuid = UUID.randomUUID();
    //观察者是否想接受消息
    private boolean wantAcceptMessage = true;
    //是否开启粘性，true：开启
    private boolean isSticky = true;
    private Config config;

    public boolean isSticky() {
        return isSticky;
    }

    public boolean isWantAcceptMessage() {
        return wantAcceptMessage;
    }

    private OstensibleObserver<T> complete() {
        return this;
    }

    public Config config() {
        if (config == null)
            config = new Config();
        return config;
    }
    /**
     * Called when the data is changed.
     *
     * @param t The new data
     */
    public abstract void onChanged(T t);


    public class Config {
        public Config setSticky(boolean b) {
            isSticky = b;
            return this;
        }

        public Config setWantAcceptMessage(boolean b) {
            wantAcceptMessage = b;
            return this;
        }

        public OstensibleObserver<T> complete() {
            return OstensibleObserver.this.complete();
        }
    }


}
