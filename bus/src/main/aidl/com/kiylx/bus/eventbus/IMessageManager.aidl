// IMessageManager.aidl
package com.kiylx.bus.eventbus;

// Declare any non-default types here with import statements
import com.kiylx.bus.eventbus.IClientListener;
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage;
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo;
import com.kiylx.bus.eventbus.ipc.binder.model.ConnectResult;

interface IMessageManager {
    void registerListener(IClientListener listener); //注册接口
    void unregisterListener(IClientListener listener); //解注册接口
    void sendMessage(in EventMessage message);
    void deleteObserver(in ChannelConnectInfo connectInfo);
    ConnectResult requestConnect(in ChannelConnectInfo connectInfo);
    EventMessage getMessageOnces(in ChannelConnectInfo connectInfo);
}