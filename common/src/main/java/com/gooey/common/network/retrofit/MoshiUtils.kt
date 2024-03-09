package com.gooey.common.network.retrofit

import com.blankj.utilcode.util.AppUtils
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory

/**
 * @CreateDate:   2019-10-27 22:00
 * @Author:       lishihui
 * @Description:
 */

inline fun <reified T> fromJson(paramMoshi: Moshi? = null, json: String): T? {
    val moshi = paramMoshi ?: getBaseMoshi()
    val jsonAdapter = moshi.adapter(T::class.java)
    return jsonAdapter.fromJson(json)
}

inline fun <reified T> fromJsonArray(paramMoshi: Moshi? = null, json: String): List<T>? {
    val moshi = paramMoshi ?: getBaseMoshi()
    val type = Types.newParameterizedType(List::class.java, T::class.java)
    val adapter : JsonAdapter<List<T>> = moshi.adapter(type)
    return adapter.fromJson(json)
}

inline fun <reified T> toJson(paramMoshi: Moshi? = null, data: T?): String {
    val moshi = paramMoshi ?: getBaseMoshi()
    val jsonAdapter = moshi.adapter(T::class.java)
    return jsonAdapter.toJson(data)
}

fun getBaseMoshi(adapters: Any? = null, addNullSafeAdapter: Boolean = true): Moshi {
    val moshi = Moshi.Builder().apply {
        if (adapters is List<*>) {
            adapters.forEach {
                it?.let {
                    add(it)
                }
            }
        } else if (adapters != null) {
            add(adapters)
        }
    }
    if (addNullSafeAdapter) {
        moshi.add(NullSafeAdapter)
    }
    return moshi.add(KotlinJsonAdapterFactory()).build()
}

//from party_1.1.3
fun getBaseMoshi2(adapter: ((Moshi.Builder) -> Unit)? = null, addNullSafeAdapter: Boolean = true): Moshi {
    val moshi = Moshi.Builder().apply {
        adapter?.invoke(this)
    }
    if (addNullSafeAdapter) {
        moshi.add(NullSafeAdapter)
    }
    return moshi.add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
}

/**
 *  通过Moshi的PolymorphicJsonAdapterFactory来自动将基类（可以是abstract class，sealed class等等）
 *  解析成对应的子类。
 *
 *  @param typeLabel 作为区分不同子类的key
 *  @param subClasses 所有子类的Class, T是基类类型
 *  @return 返回可解析多态的Moshi对象
 */
inline fun <reified T> getPolymorphicMoshi(
    typeLabel: String,
    vararg subClasses: Class<out T>
): Moshi {
    var factory = PolymorphicJsonAdapterFactory.of(T::class.java, typeLabel)
    subClasses.forEach {
        factory = factory.withSubtype(it, it.name)
    }
    return Moshi.Builder().add(factory).add(KotlinJsonAdapterFactory()).build()
}

/**
 * 将同基类的子类的范型对象转换成json
 *
 * @param data 是包裹基类范型的范型类，比如List<String>, List就是B，String就是T
 * @param subClasses 是T的所有子类Class
 * @return 返回moshi序列化后的json字符串
 */
inline fun <reified B, reified T> toPolymorphicGenericJson(
    data: B, typeLabel: String,
    vararg subClasses: Class<out T>
): String {
    val moshi = getPolymorphicMoshi(typeLabel, *subClasses)
    val type = Types.newParameterizedType(B::class.java, T::class.java)
    val adapter = moshi.adapter<B>(type)
    return try {
        adapter.toJson(data)
    } catch (e: Exception) {
        if (AppUtils.isAppDebug()) {
            throw e
        } else {
            e.printStackTrace()
            ""
        }
    }
}

/**
 * 将同基类的子类的范型对象转换成json
 *
 * @param data 是包裹基类范型的范型类，比如List<String>, List就是B，String就是T
 * @param subClasses 是T的所有子类Class
 * @return 返回moshi反序列化后的对象
 */
inline fun <reified B, reified T> fromPolymorphicGenericJson(
    json: String, typeLabel: String,
    vararg subClasses: Class<out T>
): B? {
    val moshi = getPolymorphicMoshi(typeLabel, *subClasses)
    val type = Types.newParameterizedType(B::class.java, T::class.java)
    val adapter = moshi.adapter<B>(type)
    return adapter.fromJson(json)
}