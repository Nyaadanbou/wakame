package cc.mewcraft.wakame.item.components.tracks

import org.jetbrains.annotations.ApiStatus

interface TrackMap<T, K, V> {
    fun get(key: K): V
    fun set(key: K, value: V): T
    fun remove(key: K): T

    @ApiStatus.Internal
    fun edit(block: (MutableMap<K, V>) -> Unit): T
}