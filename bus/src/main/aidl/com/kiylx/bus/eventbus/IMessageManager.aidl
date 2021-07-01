// IMessageManager.aidl
package com.kiylx.bus.eventbus;

// Declare any non-default types here with import statements
import com.kiylx.bus.eventbus.IClientListener;
import com.kiylx.bus.eventbus.IAppLocalInterface;
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage;
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo;
import com.kiylx.bus.eventbus.ipc.binder.model.ConnectResult;

interface IMessageManager {
    void registerListener(IClientListener listener); //注册本应用外的进程的接口
    void unregisterListener(IClientListener listener); //解注册本应用外的进程的接口
    void sendMessage(in EventMessage message);
    void deleteObserver(in ChannelConnectInfo connectInfo);
    ConnectResult requestConnect(in ChannelConnectInfo connectInfo);
    void getMessageOnces(in ChannelConnectInfo connectInfo);

    void registerAppListener(IAppLocalInterface listener); //注册本应用内的进程的接口
    void unregisterAppListener(IAppLocalInterface listener); //解注册本应用内的进程的接口
    void postMessage(in EventMessage message,in ChannelConnectInfo connectInfo);//app里的某一进程的mainbusmanager把数据发送至service

}