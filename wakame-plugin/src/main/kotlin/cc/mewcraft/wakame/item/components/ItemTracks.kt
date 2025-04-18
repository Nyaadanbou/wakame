package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.StatisticsConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.components.tracks.*
import cc.mewcraft.wakame.util.data.removeAll
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
import net.kyori.examination.Examinable
import net.minecraft.nbt.CompoundTag
import java.util.Collections.emptyMap

interface ItemTracks : Examinable, Iterable<Map.Entry<TrackType<*>, Track>> {

    /**
     * TBD.
     */
    fun has(type: TrackType<*>): Boolean

    /**
     * TBD.
     */
    fun <T : Track> get(type: TrackType<T>): T?

    /**
     * @return 修改之后的 [ItemTracks]
     */
    fun <T : Track> set(type: TrackType<T>, value: T): ItemTracks

    /**
     * 如果 [type] 对应的信息存在则执行 [block]; 否则不会执行 [block].
     *
     * @return 修改之后的 [ItemTracks]
     */
    fun <T : Track> modify(type: TrackType<T>, block: (T) -> T): ItemTracks

    interface Builder {
        fun has(type: TrackType<*>): Boolean
        fun <T : Track> get(type: TrackType<T>): T?
        fun <T : Track> set(type: TrackType<T>, value: T): T?
        fun build(): ItemTracks
    }

    companion object : ItemComponentBridge<ItemTracks> {
        /**
         * 该组件的配置文件
         */
        private val config = ItemComponentConfig.provide(ItemConstants.TRACKABLE)

        /**
         * 返回一个空的 [ItemTracks] 实例.
         */
        fun of(): ItemTracks {
            return Value(emptyMap())
        }

        /**
         * 从 [nbt] 中读取 [ItemTracks] 实例.
         */
        fun of(nbt: CompoundTag): ItemTracks {
            val builder = builder()
            for (tagKey in nbt.allKeys) {
                val tag = nbt.get(tagKey) as? CompoundTag ?: continue
                when (tagKey) {
                    StatisticsConstants.ENTITY_KILLS -> {
                        builder.set(TrackTypes.ENTITY_KILLS, TrackEntityKills.fromNbt(tag))
                    }

                    StatisticsConstants.PEAK_DAMAGE -> {
                        builder.set(TrackTypes.PEAK_DAMAGE, TrackPeakDamage.fromNbt(tag))
                    }

                    StatisticsConstants.REFORGE_HISTORY -> {
                        builder.set(TrackTypes.REFORGE_HISTORY, TrackReforgeHistory.of(tag))
                    }

                    else -> {
                        LOGGER.warn("Found an unknown track type: '$tagKey'")
                    }
                }
            }
            return builder.build()
        }

        /**
         * 返回一个 [ItemTracks] 的构建器.
         */
        fun builder(): Builder {
            return BuilderImpl()
        }

        override fun codec(id: String): ItemComponentType<ItemTracks> {
            return Codec(id)
        }
    }

    private class BuilderImpl : Builder {
        private val map: Reference2ObjectArrayMap<TrackType<*>, Track> = Reference2ObjectArrayMap()

        override fun has(type: TrackType<*>): Boolean {
            return map.containsKey(type)
        }

        override fun <T : Track> get(type: TrackType<T>): T? {
            @Suppress("UNCHECKED_CAST")
            return map[type] as T?
        }

        override fun <T : Track> set(type: TrackType<T>, value: T): T? {
            @Suppress("UNCHECKED_CAST")
            return map.put(type, value) as T?
        }

        override fun build(): ItemTracks {
            return Value(map)
        }
    }

    private data class Value(
        private val map: Map<TrackType<*>, Track>,
    ) : ItemTracks {
        override fun has(type: TrackType<*>): Boolean {
            return map.containsKey(type)
        }

        override fun <T : Track> get(type: TrackType<T>): T? {
            @Suppress("UNCHECKED_CAST")
            return map[type] as T?
        }

        override fun <T : Track> set(type: TrackType<T>, value: T): ItemTracks {
            return edit { map ->
                map.put(type, value)
            }
        }

        override fun <T : Track> modify(type: TrackType<T>, block: (T) -> T): ItemTracks {
            return edit { map ->
                val track = map[type]
                if (track != null) {
                    @Suppress("UNCHECKED_CAST")
                    val newTrack = block(track as T)
                    map[type] = newTrack
                }
            }
        }

        override fun iterator(): Iterator<Map.Entry<TrackType<*>, Track>> {
            return map.iterator()
        }

        private fun edit(block: (MutableMap<TrackType<*>, Track>) -> Unit): ItemTracks {
            // 由于 TrackType 都是单例, 所以使用 Reference2ObjectMap 以获得最佳性能.
            // 而且目前 TrackType 的类型极少, 意味着 HashMap 在性能开销上不如数组划算.
            val map = Reference2ObjectArrayMap(this.map)
            block(map)
            return Value(map)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemTracks> {
        override fun read(holder: ItemComponentHolder): ItemTracks? {
            val tag = holder.getNbt() ?: return null
            return of(tag)
        }

        override fun write(holder: ItemComponentHolder, value: ItemTracks) {
            holder.editNbt { tag ->
                tag.removeAll() // 总是重新写入所有数据
                for ((trackType, track) in value) {
                    val tagKey = trackType.id
                    val tagValue = track.saveNbt()
                    tag.put(tagKey, tagValue)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeNbt()
        }
    }

    // 开发日记 2024/7/2 小米
    // ItemTracks 没有必要添加模板,
    // 因为其数据不应该由配置文件指定,
    // 而是由玩家的交互去更新.
    //
    // 开发日记 2024/7/6
    // 如果没有模板, 那系统怎么知道要不要
    // 往物品里写入需要跟踪的信息? 读行为?
    //
    // 开发日记 2024/7/10
    // 对的, 就是读行为.
}