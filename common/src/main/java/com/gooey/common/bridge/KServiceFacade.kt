package com.gooey.common.bridge

import com.gooey.common.INoProguard

object KServiceFacade {

    private val sCachedLoader = HashMap<Class<*>, Any>()

    @Synchronized
    fun put(clz: Class<*>?, impl: INoProguard?) {
        if (impl == null || clz == null) {
            return
        }
        sCachedLoader[clz] = impl
    }

    @Synchronized
    operator fun <T> get(clazz: Class<T>): T? {
        return sCachedLoader[clazz] as T?
    }
}