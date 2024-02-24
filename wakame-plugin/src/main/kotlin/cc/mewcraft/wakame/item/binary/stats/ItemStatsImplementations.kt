@file:OptIn(InternalApi::class)

package cc.mewcraft.wakame.item.binary.stats

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.util.toStableByte
import cc.mewcraft.wakame.util.toStableShort
import net.kyori.adventure.key.Key

////// Use CMD-7 to navigate the whole file

// TODO 可以进一步抽象，减少重复代码

data class EntityKillsStats(
    override val accessor: ItemStatsAccessor,
) : ItemStats, NumericMapLikeItemStats<Key, Int> {
    override val nbtPath: String = NekoTags.Stats.ENTITY_KILLS
    override fun get(key: Key): Int = rootOrNull?.getInt(key.asString()) ?: 0
    override fun set(key: Key, value: Int) = rootOrCreate.putShort(key.asString(), value.toStableShort())
    override fun increment(key: Key, value: Int) {
        val entityKey = key.asString()
        val oldValue = rootOrCreate.getShort(entityKey)
        val newValue = (oldValue + value).toStableShort()
        rootOrCreate.putShort(key.asString(), newValue)
    }
}

data class PeakDamageStats(
    override val accessor: ItemStatsAccessor,
) : ItemStats, NumericMapLikeItemStats<Element, Int> {
    override val nbtPath: String = NekoTags.Stats.PEAK_DAMAGE
    override fun get(key: Element): Int = rootOrNull?.getInt(key.key) ?: 0
    override fun set(key: Element, value: Int) = rootOrCreate.putShort(key.key, value.toStableShort())
    override fun increment(key: Element, value: Int) {
        val damageKey = key.key
        val oldValue = rootOrCreate.getShort(damageKey)
        val newValue = (oldValue + value).toStableShort()
        rootOrCreate.putShort(damageKey, newValue)
    }
}

data class ReforgeStats(
    override val accessor: ItemStatsAccessor,
) : ItemStats {
    override val nbtPath: String = NekoTags.Stats.REFORGE
    val count: NumericSingleItemStats<Int> = object : NumericSingleItemStats<Int> {
        override val nbtPath: String = "count"
        override fun get(): Int = rootOrNull?.getInt(nbtPath) ?: 0
        override fun set(value: Int) = rootOrCreate.putByte(nbtPath, value.toStableByte())
        override fun increment(value: Int) {
            val oldValue = rootOrCreate.getInt(nbtPath)
            val newValue = (oldValue + value).toStableByte()
            rootOrCreate.putByte(nbtPath, newValue)
        }
    }
    val cost: NumericSingleItemStats<Int> = object : NumericSingleItemStats<Int> {
        override val nbtPath: String = "cost"
        override fun get(): Int = rootOrNull?.getInt(nbtPath) ?: 0
        override fun set(value: Int) = rootOrCreate.putShort(nbtPath, value.toStableShort())
        override fun increment(value: Int) {
            val oldValue = rootOrCreate.getInt(nbtPath)
            val newValue = (oldValue + value).toStableShort()
            rootOrCreate.putShort(nbtPath, newValue)
        }
    }
}