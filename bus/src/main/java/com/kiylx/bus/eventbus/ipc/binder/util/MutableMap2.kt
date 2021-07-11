package com.kiylx.bus.eventbus.ipc.binder.util

class MutableMap2<T, K, V> {
    val root: MutableMap<T, MutableMap<K, V>> = mutableMapOf()

    fun add(TName: T, KName: K, VName: V) {
        var list: MutableMap<K, V>? = root[TName]
        if (list == null) {
            root[TName] = mutableMapOf()
            list = root[TName]
        }
        list!![KName] = VName
    }

    fun remove(TName: T): MutableMap<K, V>? {
        return root.remove(TName)
    }

    fun remove(TName: T, KName: K): V? {
        return root[TName]?.remove(KName)
    }

    fun isEmpty(): Boolean {
        return root.isEmpty()
    }

    fun get(TName: T, KName: K): V? {
        return root[TName]?.get(KName)
    }

    fun getTArray(): List<T> {
        return root.keys.toList()
    }

}