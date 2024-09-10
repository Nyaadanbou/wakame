package cc.mewcraft.wakame.item.components.tracks

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.StatisticsConstants
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.CompoundTag
import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap

/**
 * 跟踪使用物品单次打出过的最高伤害.
 */
interface TrackPeakDamage : Track, TrackMap<TrackPeakDamage, Element, Double> {

    companion object : TrackType<TrackPeakDamage> {
        fun empty(): TrackPeakDamage {
            return TrackPeakDamageImpl(emptyMap())
        }

        fun of(nbt: CompoundTag): TrackPeakDamage {
            val map = if (nbt.size() < 8) Reference2DoubleArrayMap<Element>() else Reference2DoubleOpenHashMap()
            for (tagKey: String in nbt.keySet()) {
                val element = ElementRegistry.INSTANCES.find(tagKey) ?: continue // 直接跳过无效的元素
                val damage = nbt.getDouble(tagKey)
                map[element] = damage
            }
            return TrackPeakDamageImpl(map)
        }

        override val id: String = StatisticsConstants.PEAK_DAMAGE
    }
}

private class TrackPeakDamageImpl(
    private val map: Map<Element, Double>,
) : TrackPeakDamage {
    override fun get(key: Element): Double {
        return map.getOrDefault(key, 0.0)
    }

    override fun set(key: Element, value: Double): TrackPeakDamage {
        return edit { map ->
            map[key] = value
        }
    }

    override fun remove(key: Element): TrackPeakDamage {
        return edit { map ->
            map.remove(key)
        }
    }

    override fun serializeAsTag(): CompoundTag = CompoundTag {
        for ((element: Element, damage: Double) in map) {
            putDouble(element.uniqueId, damage)
        }
    }

    override fun edit(block: (MutableMap<Element, Double>) -> Unit): TrackPeakDamage {
        val map = Reference2DoubleArrayMap(this.map)
        block(map)
        return TrackPeakDamageImpl(map)
    }
}