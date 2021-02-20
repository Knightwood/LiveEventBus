package com.kiylx.bus.eventbus.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 创建者 kiylx
 * 创建时间 2020/10/5 19:29
 * packageName：com.crystal.aplayer.module_base.tools.databus
 * 描述：存储消息通道，分发消息通道，全局配置调整
 */
public class BusCore {
    //存放通道
    private final Map<String, Channel<Object>> mChannels;
    /**
     * 配置项
     */
    private final Config config = new Config();
    private boolean lifecycleObserverAlwaysActive;
    private boolean autoClear;

    private BusCore() {
        this.mChannels = new HashMap<>();
        lifecycleObserverAlwaysActive = true;
        autoClear = false;
    }

    public static BusCore getInstance() {
        return Singleton.INSTANCE.getInstance();
    }


    private static enum Singleton {
        INSTANCE;
        private BusCore classInstance;

        Singleton() {
            classInstance = new BusCore();
        }

        public BusCore getInstance() {
            return classInstance;
        }
    }


    /**
     * @param <T>    消息通道的泛型类
     * @param target 消息通道名称
     * @return 返回消息通道
     */
    public <T> Channel<T> get(String target) {
        if (!mChannels.containsKey(target)) {
            mChannels.put(target, new Channel<>(target));
        }
        return (Channel<T>) mChannels.get(target);
    }

    public <T> Channel<T> get(UUID uuid) {
        Iterator<Channel<Object>> item = mChannels.values().iterator();
        while (item.hasNext()) {
            Channel<Object> c = item.next();
            if (c.getUuid().compareTo(uuid) == 0) {
                return (Channel<T>) item;
            }
        }
        return null;
    }

    public <T> Channel<T> get(String target, UUID uuid) {
        Iterator<Channel<Object>> item = mChannels.values().iterator();
        while (item.hasNext()) {
            Channel<Object> c = item.next();
            if (c.getUuid().compareTo(uuid) == 0 && c.getKey().equals(target)) {
                return (Channel<T>) item;
            }
        }
        return null;
    }

    public Config config() {
        return config;
    }

    private class Config {
        public Config setLifecycleObserverAlwaysActive(boolean b) {
            lifecycleObserverAlwaysActive = b;
            return this;
        }

        public Config setAutoClear(boolean b) {
            autoClear = b;
            return this;
        }
    }
}
