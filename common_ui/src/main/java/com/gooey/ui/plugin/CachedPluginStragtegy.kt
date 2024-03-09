package com.gooey.ui.plugin

import android.util.SparseArray
/**
 * @author lishihui
 */
abstract class CachedPluginStragtegy<META, CHILD : IPlugin<META>> : PluginStrategy<META, CHILD>() {
    protected val cache = SparseArray<CHILD?>()

    override fun updateStatus(status: Int, data: META?) {
        val target = statusToType(status)
        if (currentState == target && currentHolder != null) {
            switchTo(currentHolder, obtainInfo(status) ?: data)
        } else {
            currentState = target
            switchTo(cachedViewHolder(target), obtainInfo(status) ?: data)
        }
    }

    open fun needCacheHolder(holderType: Int): Boolean = true

    private fun cachedViewHolder(holderType: Int): CHILD {
        val fromCache = needCacheHolder(holderType)
        var holder: CHILD? = if (fromCache) {
            cache.get(holderType)
        } else {
            null
        }
        if (holder == null) {
            val local = initViewHolder(holderType)
            if (fromCache) {
                if (local is SimplePlugin<*>) {
                    local.cached = true
                }
                cache.put(holderType, local)
            }
            holder = local
        }
        return holder
    }
}