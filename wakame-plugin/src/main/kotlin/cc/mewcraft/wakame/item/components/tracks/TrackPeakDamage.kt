package cc.mewcraft.wakame.item.components.tracks

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.StatisticsConstants
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.util.adventure.asMinimalStringKoish
import cc.mewcraft.wakame.util.data.CompoundTag
import cc.mewcraft.wakame.util.data.keySet
import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap
import net.minecraft.nbt.CompoundTag
import java.util.Collections.emptyMap

/**
 * 跟踪使用物品单次打出过的最高伤害.
 */
class TrackPeakDamage(
    private val map: Map<Element, Double>,
) : Track, TrackMap<TrackPeakDamage, Element, Double> {

    companion object : TrackType<TrackPeakDamage> {
        fun empty(): TrackPeakDamage {
            return TrackPeakDamage(emptyMap())
        }

        fun fromNbt(nbt: CompoundTag): TrackPeakDamage {
            val map = if (nbt.size() < 8) Reference2DoubleArrayMap<Element>() else Reference2DoubleOpenHashMap()
            for (tagKey: String in nbt.keySet()) {
                val element = BuiltInRegistries.ELEMENT[tagKey] ?: continue // 直接跳过无效的元素
                val damage = nbt.getDouble(tagKey)
                map[element] = damage
            }
            return TrackPeakDamage(map)
        }

        override val id: String = StatisticsConstants.PEAK_DAMAGE
    }

    override fun get(key: Element): Double {
        return map.getOrDefault(key, 0.0)
    }

    override fun set(key: Element, value: Double): TrackPeakDamage {
        return edit { map -> map[key] = value }
    }

    override fun remove(key: Element): TrackPeakDamage {
        return edit { map -> map.remove(key) }
    }

    override fun saveNbt(): CompoundTag {
        return CompoundTag {
            for ((element: Element, damage: Double) in map) {
                putDouble(element.key().asMinimalStringKoish(), damage)
            }
        }
    }

    override fun edit(block: (MutableMap<Element, Double>) -> Unit): TrackPeakDamage {
        return TrackPeakDamage(Reference2DoubleArrayMap(map).apply(block))
    }
}
