package com.kiylx.bus.eventbus.utils;

/**
 * 创建者 kiylx
 * 创建时间 2021/1/1 20:44
 * packageName：com.kiylx.bus.eventbus.utils
 * 描述：给handler的runnable的类,接受Method实例和用于Method执行的参数
 */
public class PostTask implements Runnable {
    private Object[] newValue;
    private Method method;

    public PostTask(Object[] newValue, Method method) {
        this.newValue = newValue;
        this.method = method;
    }

    @Override
    public void run() {
        method.method(newValue);
    }

    public interface Method {
        void method(Object[] args);
    }
}
