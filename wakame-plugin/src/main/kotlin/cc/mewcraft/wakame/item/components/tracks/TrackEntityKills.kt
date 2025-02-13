package cc.mewcraft.wakame.item.components.tracks

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.item.StatisticsConstants
import cc.mewcraft.wakame.util.CompoundTag
import cc.mewcraft.wakame.world.entity.EntityKeyLookup
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.kyori.adventure.key.Key

// 开发日记 2024/7/5
// 用 Int 储存击杀数, 最多可以存 2^31-1

/**
 * 跟踪使用物品击杀实体的数量.
 */
class TrackEntityKills(
    private val map: Map<Key, Int>,
) : Track, TrackMap<TrackEntityKills, Key, Int> {

    companion object : TrackType<TrackEntityKills> {

        private val entityKeyLookup: EntityKeyLookup by Injector.inject<EntityKeyLookup>()

        override val id: String = StatisticsConstants.ENTITY_KILLS

        fun empty(): TrackEntityKills {
            return TrackEntityKills(emptyMap())
        }

        fun fromNbt(nbt: CompoundTag): TrackEntityKills {
            val map = if (nbt.size() < 8) Object2IntArrayMap<Key>() else Object2IntOpenHashMap()
            for (tagKey in nbt.keySet()) {
                val entityKey = Key.key(tagKey).takeIf { entityKeyLookup.validate(it) } ?: continue // 直接跳过无效的 key
                val kills = nbt.getInt(tagKey)
                map[entityKey] = kills
            }
            return TrackEntityKills(map)
        }

    }

    override fun get(key: Key): Int {
        return map.getOrDefault(key, 0)
    }

    override fun set(key: Key, value: Int): TrackEntityKills {
        return edit { map -> map[key] = value }
    }

    override fun remove(key: Key): TrackEntityKills {
        return edit { map -> map.remove(key) }
    }

    fun grow(key: Key, count: Int): TrackEntityKills {
        return set(key, get(key) + count)
    }

    override fun saveNbt(): CompoundTag {
        return CompoundTag {
            for ((entityKey, kills) in map) {
                putInt(entityKey.toString(), kills)
            }
        }
    }

    override fun edit(block: (MutableMap<Key, Int>) -> Unit): TrackEntityKills {
        return TrackEntityKills(Object2IntOpenHashMap(map).apply(block))
    }
}
