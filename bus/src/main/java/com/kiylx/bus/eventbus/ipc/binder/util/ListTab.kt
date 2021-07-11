package com.kiylx.bus.eventbus.ipc.binder.util

class ListTab<K, T> {
    private val root: MutableMap<K, MutableList<T>> by lazy {  mutableMapOf()}

fun add(key: K, value: T) {
        var list: MutableList<T>? = root[key]
        if (list == null) {
            root[key] = mutableListOf()
            list = root[key]
        }
        list!!.add(value)
    }

    fun remove(key: K, value: T) {
        root[key]?.remove(value)
    }

    fun getValuesList(key: K): MutableList<T>? {
        return root[key]
    }

    fun getValue(key: K, value: T): T? {
        return root[key]?.find {
            return@find it==value
        }
    }
}