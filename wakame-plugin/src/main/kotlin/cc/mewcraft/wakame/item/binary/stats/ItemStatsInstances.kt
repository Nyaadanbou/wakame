package cc.mewcraft.wakame.item.binary.stats

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.util.toStableByte
import cc.mewcraft.wakame.util.toStableShort
import net.kyori.adventure.key.Key

/* Use CMD-7 to navigate the whole file */

// TODO 减少重复代码

@JvmInline
value class EntityKillsStatistics(
    override val holder: ItemStatisticsHolder,
) : ItemStatistics, NumericMapLikeItemStats<Key, Int> {
    override val nbtPath: String get() = NekoTags.Stats.ENTITY_KILLS

    override fun get(key: Key): Int = rootOrNull?.getInt(key.asString()) ?: 0
    override fun set(key: Key, value: Int) = rootOrCreate.putShort(key.asString(), value.toStableShort())
    override fun increment(key: Key, value: Int) {
        val entityKey = key.asString()
        val oldValue = rootOrCreate.getShort(entityKey)
        val newValue = (oldValue + value).toStableShort()
        rootOrCreate.putShort(key.asString(), newValue)
    }
}

@JvmInline
value class PeakDamageStatistics(
    override val holder: ItemStatisticsHolder,
) : ItemStatistics, NumericMapLikeItemStats<Element, Int> {
    override val nbtPath: String get() = NekoTags.Stats.PEAK_DAMAGE

    override fun get(key: Element): Int = rootOrNull?.getInt(key.uniqueId) ?: 0
    override fun set(key: Element, value: Int) = rootOrCreate.putShort(key.uniqueId, value.toStableShort())
    override fun increment(key: Element, value: Int) {
        val damageKey = key.uniqueId
        val oldValue = rootOrCreate.getShort(damageKey)
        val newValue = (oldValue + value).toStableShort()
        rootOrCreate.putShort(damageKey, newValue)
    }
}

data class ReforgeStatistics(
    override val holder: ItemStatisticsHolder,
) : ItemStatistics {
    override val nbtPath: String get() = NekoTags.Stats.REFORGE

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