@file:OptIn(InternalApi::class)

package cc.mewcraft.wakame.item.binary.stats

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.util.toStableByte
import cc.mewcraft.wakame.util.toStableShort
import net.kyori.adventure.key.Key

////// Use CMD-7 to navigate the whole file

// TODO 可以进一步抽象，减少重复代码

class EntityKillsStats(
    override val accessor: ItemStatsAccessor,
) : ItemStats, NumericMapLikeItemStats<Key, Int> {
    override val nbtPath: String = ItemStatsTagNames.ENTITY_KILLS
    override fun get(key: Key): Int = tags.getInt(key.asString())
    override fun set(key: Key, value: Int) = tags.putShort(key.asString(), value.toStableShort())
    override fun increment(key: Key, value: Int) {
        val entityKey = key.asString()
        val oldValue = tags.getShort(entityKey)
        val newValue = (oldValue + value).toStableShort()
        tags.putShort(key.asString(), newValue)
    }
}

class PeakDamageStats(
    override val accessor: ItemStatsAccessor,
) : ItemStats, NumericMapLikeItemStats<Element, Int> {
    override val nbtPath: String = ItemStatsTagNames.PEAK_DAMAGE
    override fun get(key: Element): Int = tags.getInt(key.name)
    override fun set(key: Element, value: Int) = tags.putShort(key.name, value.toStableShort())
    override fun increment(key: Element, value: Int) {
        val damageKey = key.name
        val oldValue = tags.getShort(damageKey)
        val newValue = (oldValue + value).toStableShort()
        tags.putShort(damageKey, newValue)
    }
}

class ReforgeStats(
    override val accessor: ItemStatsAccessor,
) : ItemStats {
    override val nbtPath: String = ItemStatsTagNames.REFORGE
    val count: NumericSingleItemStats<Int> = object : NumericSingleItemStats<Int> {
        override val nbtPath: String = "count"
        override fun get(): Int = tags.getInt(nbtPath)
        override fun set(value: Int) = tags.putByte(nbtPath, value.toStableByte())

        override fun increment(value: Int) {
            val oldValue = tags.getInt(nbtPath)
            val newValue = (oldValue + value).toStableByte()
            tags.putByte(nbtPath, newValue)
        }
    }
    val cost: NumericSingleItemStats<Int> = object : NumericSingleItemStats<Int> {
        override val nbtPath: String = "cost"
        override fun get(): Int = tags.getInt(nbtPath)
        override fun set(value: Int) = tags.putShort(nbtPath, value.toStableShort())

        override fun increment(value: Int) {
            val oldValue = tags.getInt(nbtPath)
            val newValue = (oldValue + value).toStableShort()
            tags.putShort(nbtPath, newValue)
        }
    }
}