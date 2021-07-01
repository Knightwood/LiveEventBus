// IAppLocalInterface.aidl
package com.kiylx.bus.eventbus;

// Declare any non-default types here with import statements
import com.kiylx.bus.eventbus.IClientListener;
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage;
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo;
import com.kiylx.bus.eventbus.ipc.binder.model.ConnectResult;

interface IAppLocalInterface {
//app自身有很多进程的时候,将自己所在进程注册给service。
//给予服务端客服端的来源信息
   String getLocateFrom();

}