package com.kiylx.bus.eventbus;

import com.kiylx.bus.eventbus.core.BusCore;
import com.kiylx.bus.eventbus.core.Channel;

/**
 * 创建者 kiylx
 * 创建时间 2020/12/31 20:30
 * packageName：com.kiylx.liveeventbus.live_event_bus
 * 描述：
 */
public class DataBus {
    public static <T> Channel<T> with(String channelName) {
        return BusCore.getInstance().get(channelName);
    }
}
