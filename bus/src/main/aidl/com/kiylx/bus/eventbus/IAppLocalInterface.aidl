// IAppLocalInterface.aidl
package com.kiylx.bus.eventbus;

// Declare any non-default types here with import statements
import com.kiylx.bus.eventbus.IClientListener;
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage;
import com.kiylx.bus.eventbus.ipc.binder.model.Request;
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo;
import com.kiylx.bus.eventbus.ipc.binder.model.ConnectResult;

interface IAppLocalInterface {
//app自身有很多进程的时候,将自己所在进程注册给service。
//给予服务端客服端的来源信息
   String getLocateFrom();

   //将数据发送到host
   void sendDataToHost(out Request data);
   //service获取一次数据，会调用binder的
   void getDataOnce(out Request data);
   //令host添加一个obsever，在channel数据变化时，此observer会通知远程客户端
  //void addRemoteObserver(out Request data);

  //void removeRemoteObserver(out Request data);

}