package com.kiylx.bus.eventbus.utils;

import android.os.Looper;

/**
 * 创建者 kiylx
 * 创建时间 2021/1/1 20:40
 * packageName：com.kiylx.bus.eventbus.utils
 * 描述：
 */
public class Utils {
    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
