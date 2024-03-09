package com.gooey.common.sp

import android.os.Parcelable
import com.tencent.mmkv.MMKV


/**
 *@author lishihui01
 *@Date 2023/9/1
 *@Describe:
 */
class Preferences private constructor(private val kv: MMKV) {
    companion object {
        private const val DEFAULT_KEY = "common"
        private val kvMap = HashMap<String, MMKV>()
        fun getPreferences(spName: String = DEFAULT_KEY): Preferences {
            return if (kvMap.containsKey(spName)) {
                Preferences(kvMap[spName]!!)
            } else {
                val kv = MMKV.mmkvWithID(spName)
                kvMap[spName] = kv
                Preferences(kv)
            }
        }
    }

    fun putString(key: String, value: String) {
        kv.encode(key, value)
    }

    fun putBoolean(key: String, value: Boolean) {
        kv.encode(key, value)
    }

    fun putLong(key: String, value: Long?) {
        kv.encode(key, value ?: 0L)
    }

    fun putInt(key: String, value: Int?) {
        kv.encode(key, value ?: 0)
    }

    fun putFloat(key: String, value: Float?) {
        kv.encode(key, value ?: 0f)
    }

    fun putDouble(key: String, value: Double?) {
        kv.encode(key, value ?: 0.0)
    }

    fun putByteArray(key: String, value: ByteArray) {
        kv.encode(key, value)
    }

    fun putParcelable(key: String, value: Parcelable) {
        kv.encode(key, value)
    }

    fun putSet(key: String, value: Set<String>) {
        kv.encode(key, value)
    }

    fun getString(key: String, default: String?): String {
        return kv.decodeString(key, default ?: "") ?: ""
    }

    fun getBoolean(key: String, default: Boolean?): Boolean {
        return kv.decodeBool(key, default ?: false)
    }

    fun getLong(key: String, default: Long?): Long {
        return kv.decodeLong(key, default ?: 0L)
    }

    fun getInt(key: String, default: Int?): Int {
        return kv.decodeInt(key, default ?: 0)
    }

    fun getFloat(key: String, default: Float?): Float {
        return kv.decodeFloat(key, default ?: 0f)
    }

    fun getDouble(key: String, default: Double?): Double {
        return kv.decodeDouble(key, default ?: 0.0)
    }

    fun getByteArray(key: String): ByteArray? {
        return kv.decodeBytes(key)
    }

    fun <T : Parcelable> getParcelable(key: String?, tClass: Class<T>, default: T): T {
        return kv.decodeParcelable(key, tClass) ?: default
    }

    fun getSet(key: String): Set<String>? {
        return kv.decodeStringSet(key)
    }


}