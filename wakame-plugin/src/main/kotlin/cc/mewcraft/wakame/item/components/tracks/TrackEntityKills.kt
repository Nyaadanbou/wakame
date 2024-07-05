package cc.mewcraft.wakame.item.components.tracks

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.entity.EntityKeyLookup
import cc.mewcraft.wakame.item.StatisticsConstants
import cc.mewcraft.wakame.util.CompoundTag
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// 开发日记 2024/7/5
// 用 Int 储存击杀数, 最多可以存 2^31-1

/**
 * 跟踪使用物品击杀实体的数量.
 */
interface TrackEntityKills : Track, TrackMap<TrackEntityKills, Key, Int> {

    /**
     * 给指定的 [key] 增加击杀数.
     */
    fun grow(key: Key, count: Int = 1): TrackEntityKills

    companion object : TrackType<TrackEntityKills>, KoinComponent {
        private val entityKeyLookup: EntityKeyLookup by inject()

        fun empty(): TrackEntityKills {
            return TrackEntityKillsImpl(emptyMap())
        }

        fun of(nbt: CompoundTag): TrackEntityKills {
            val map = if (nbt.size() < 8) Object2IntArrayMap<Key>() else Object2IntOpenHashMap()
            for (tagKey in nbt.keySet()) {
                val entityKey = Key.key(tagKey).takeIf {
                    entityKeyLookup.validate(it)
                } ?: continue // 直接跳过无效的 key
                val kills = nbt.getInt(tagKey)
                map[entityKey] = kills
            }
            return TrackEntityKillsImpl(map)
        }

        override val id: String = StatisticsConstants.ENTITY_KILLS
    }
}

private class TrackEntityKillsImpl(
    private val map: Map<Key, Int>,
) : TrackEntityKills {
    override fun get(key: Key): Int {
        return map.getOrDefault(key, 0)
    }

    override fun set(key: Key, value: Int): TrackEntityKills {
        return edit { map ->
            map[key] = value
        }
    }

    override fun remove(key: Key): TrackEntityKills {
        return edit { map ->
            map.remove(key)
        }
    }

    override fun grow(key: Key, count: Int): TrackEntityKills {
        return set(key, get(key) + count)
    }

    override fun serializeAsTag(): Tag = CompoundTag {
        for ((entityKey, kills) in map) {
            putInt(entityKey.asString(), kills)
        }
    }

    override fun edit(block: (MutableMap<Key, Int>) -> Unit): TrackEntityKills {
        val map = Object2IntOpenHashMap(this.map)
        block(map)
        return TrackEntityKillsImpl(map)
    }
}