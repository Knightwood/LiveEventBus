package com.kiylx.bus.eventbus.utils;

import android.util.Log;

/**
 * 创建者 kiylx
 * 创建时间 2020/7/29 23:00
 */
public class Logs {
    private static final String tag = "logUtil_logs";

    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;

    public static final int nowLevel = VERBOSE;
    /*
     * 在方法中，相应的日志级别大于等于nowLevel值，才可打印。
     * 比如，nowLevel是6，则，所有的日志级别都小于6，所有的日志都不可打印。
     */

    public static void v(String tag, String msg, Object... args) {
        if (VERBOSE >= nowLevel) {
            Log.v(tag, String.format(msg, args));
        }
    }

    public static void v(String tag, String msg) {
        if (VERBOSE >= nowLevel) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg, Object... args) {
        if (DEBUG >= nowLevel) {
            Log.d(tag, String.format(msg, args));
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG >= nowLevel) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg, Object... args) {
        if (INFO >= nowLevel) {
            Log.i(tag, String.format(msg, args));
        }
    }

    public static void i(String tag, String msg) {
        if (INFO >= nowLevel) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg, Object... args) {
        if (WARN >= nowLevel) {
            Log.w(tag, String.format(msg, args));
        }
    }

    public static void w(String tag, String msg) {
        if (WARN >= nowLevel) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg, Object... args) {
        if (ERROR >= nowLevel) {
            Log.w(tag, String.format(msg, args));
        }
    }

    public static void e(String tag, String msg) {
        if (ERROR >= nowLevel) {
            Log.w(tag, msg);
        }
    }
}
