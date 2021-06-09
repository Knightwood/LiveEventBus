package com.kiylx.bus.eventbus.utils;

import androidx.lifecycle.LifecycleOwner;

/**
 * 创建者 kiylx
 * 创建时间 2021/1/1 20:44
 * packageName：com.kiylx.bus.eventbus.utils
 * 描述：给handler的runnable的类,接受Method实例和用于Method执行的参数
 */
@Deprecated
public class PostTask implements Runnable {
    /**
     * newValue[0]:message
     * newValue[1]:LifecycleOwner,也就是sender
     */
    private Object[] newValue;
    private Method method;

    public PostTask(Method method, Object... newValue) {
        this.newValue = newValue;
        this.method = method;
    }

    @Override
    public void run() {
        method.method(newValue);
    }

    public <T> Runnable obtainMessage(Method mPostMethod, T message, LifecycleOwner sender) {
        this.method=mPostMethod;
        newValue= new Object[]{message, sender};
        return this;
    }

    public <T> Runnable obtainMessage(T message, LifecycleOwner sender) {
        newValue= new Object[]{message, sender};
        return this;
    }

    public <T> Runnable obtainNewMessage(Method mPostMethod,T message, LifecycleOwner sender) {
        return new PostTask(mPostMethod,message,sender);
    }

    public interface Method {
        void method(Object[] args);
    }
}
/*
    // * args[0]: 消息
    // * args[1]: LifecycleOwner,也就是sender
     //
private inner class PostMethod() : PostTask.Method {
        override fun method(args: Array<Any>) {
        val value: T? = args[0] as T
        val owner: LifecycleOwner? = args[1] as LifecycleOwner
        if (value != null && owner != null) {
        //带生命周期的发送消息的时候sender处于非激活状态时，消息取消发送
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
        inBox.value = value
        }
        }
        if (value != null && owner == null) {
        //不带有生命周期
        inBox.value = value
        }
        }
        }
*
* */
