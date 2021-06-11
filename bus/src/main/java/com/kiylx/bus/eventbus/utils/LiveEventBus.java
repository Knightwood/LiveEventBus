package com.kiylx.bus.eventbus;

import com.kiylx.bus.eventbus.core.Channel;
import com.kiylx.bus.eventbus.core.interfaces.Mode;

/**
 * 创建者 kiylx
 * 创建时间 2020/12/31 20:30
 * packageName：com.kiylx.liveeventbus.live_event_bus
 * 描述：
 */
public class LiveEventBus {
    public static <T> Channel<T> with(String channelName) {
        return BusCore.getInstance().<T>get(channelName);
    }

    public static <T> Channel<T> withCrossProcess(String channelName) {
        return BusCore.getInstance().<T>get(channelName)
                .config()
                .setIsUseCrossProcess(Mode.binder)
                .build();
    }
    public static <T> Channel<T> withCrossProcess(String channelName,Mode mode) {
        return BusCore.getInstance().<T>get(channelName)
                .config()
                .setIsUseCrossProcess(mode)
                .build();
    }
}
