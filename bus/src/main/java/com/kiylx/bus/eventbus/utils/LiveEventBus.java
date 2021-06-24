package com.kiylx.bus.eventbus.utils;

import android.content.Context;

import com.kiylx.bus.eventbus.core.MainBusManager;
import com.kiylx.bus.eventbus.core.ChannelX;
import com.kiylx.bus.eventbus.ipc.binder.CrossChannel;
import com.kiylx.bus.eventbus.ipc.binder.CrossProcessBusManager;
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo;

/**
 * 创建者 kiylx
 * 创建时间 2020/12/31 20:30
 * packageName：com.kiylx.liveeventbus.live_event_bus
 * 描述：
 */
public class LiveEventBus {
    public static <T> ChannelX<T> with(String channelName, Class<T> clazz) {
        return MainBusManager.getInstance().<T>getChannel(channelName, null, clazz);
    }

    public static <T> CrossChannel<T> withCrossProcess(Context context, ChannelConnectInfo channelConnectInfo, Class<T> clazz) {
        return CrossProcessBusManager.getInstance().<T>getChannel(context, channelConnectInfo, clazz);
    }
}
