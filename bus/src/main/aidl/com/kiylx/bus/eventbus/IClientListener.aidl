// IClientListener.aidl
package com.kiylx.bus.eventbus;

// Declare any non-default types here with import statements
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage;

interface IClientListener {
    //将数据从服务端传到客户端
   void notifyDataChanged(out EventMessage message);
   //给予服务端，此客户端的来源信息,即自身进程名称
   String getLocateFrom();
   //给予服务端，此客户端要连接到哪个进程，要连接到的进程名称
   List<String> getLinkToProcess();
 }