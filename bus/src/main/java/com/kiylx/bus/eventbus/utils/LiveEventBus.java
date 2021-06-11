package com.kiylx.bus.eventbus.utils;

import com.kiylx.bus.eventbus.core.MainBusManager;
import com.kiylx.bus.eventbus.core.Channel;

/**
 * 创建者 kiylx
 * 创建时间 2020/12/31 20:30
 * packageName：com.kiylx.liveeventbus.live_event_bus
 * 描述：
 */
public class LiveEventBus {
    public static <T> Channel<T> with(String channelName) {
        return MainBusManager.getInstance().<T>getChannel(channelName,null);
    }

}
