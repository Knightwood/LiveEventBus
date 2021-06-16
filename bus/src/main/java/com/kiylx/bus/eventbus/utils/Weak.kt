package com.kiylx.bus.eventbus.utils

import android.util.Log
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

/**
 * 使用kotlin委托机制创建弱引用的工具类
 *
 *     //自动推断出泛型
 *      var act by Weak{
 *      context
 *      }
 *      //也可以指定泛型，一种是给属性指定类型，必须为可null的
 *       var act: Activity? by Weak {
 *       context
 *       }
 *       //第二种是为Weak指定泛型，不可null的
 *       var act by Weak<Activity> {
 *       context
 *       }
 *
 *       //不指定初始值的情况，此时必须指定泛型
 *       var act:Activity? by Weak()
 *       或者
 *      var act by Weak<Activity>()
 */
class Weak<T : Any>(initializer: () -> T?) {
    var weakReference = WeakReference<T?>(initializer())

    constructor() : this({
        null
    })

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        Log.d("Weak Delegate", "-----------getValue")
        return weakReference.get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        Log.d("Weak Delegate", "-----------setValue")
        weakReference = WeakReference(value)
    }
}