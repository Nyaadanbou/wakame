package cc.mewcraft.wakame.item.behavior

import cc.mewcraft.wakame.item.NekoItem
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap

interface ItemBehaviorMap : Iterable<ItemBehavior> {
    /**
     * Checks whether this [NekoItem] has specific [ItemBehavior].
     */
    fun <T : ItemBehavior> has(type: ItemBehaviorType<T>): Boolean

    /**
     * Gets the specific [ItemBehavior] if this [NekoItem] has it.
     */
    fun <T : ItemBehavior> get(type: ItemBehaviorType<T>): T?

    /**
     * [ItemBehaviorMap] 的 builder, 用于构建一个 [ItemBehaviorMap].
     */
    interface Builder {
        /**
         * 添加一个 [ItemBehavior]. 已存在的 [type] 会被覆盖.
         */
        fun <T : ItemBehavior> put(type: ItemBehaviorType<T>, behavior: T)

        /**
         * 构建.
         */
        fun build(): ItemBehaviorMap
    }

    /**
     * [ItemBehaviorMap] 的构造函数.
     */
    companion object {
        /**
         * 获取一个空的 [ItemBehaviorMap].
         */
        fun empty(): ItemBehaviorMap {
            return Empty
        }

        /**
         * 获取一个 builder.
         */
        fun build(block: Builder.() -> Unit): ItemBehaviorMap {
            return BuilderImpl().apply(block).build()
        }
    }

    private object Empty : ItemBehaviorMap {
        private val empty = emptyList<ItemBehavior>()
        override fun <T : ItemBehavior> has(type: ItemBehaviorType<T>): Boolean = false
        override fun <T : ItemBehavior> get(type: ItemBehaviorType<T>): T? = null
        override fun iterator(): Iterator<ItemBehavior> = empty.iterator()
    }

    private class MapImpl(
        data: Map<ItemBehaviorType<*>, ItemBehavior>,
    ) : ItemBehaviorMap {
        private val data: Reference2ReferenceArrayMap<ItemBehaviorType<*>, ItemBehavior> = Reference2ReferenceArrayMap(data)

        override fun <T : ItemBehavior> has(type: ItemBehaviorType<T>): Boolean {
            return data.containsKey(type)
        }

        override fun <T : ItemBehavior> get(type: ItemBehaviorType<T>): T? {
            return data[type] as T?
        }

        override fun iterator(): Iterator<ItemBehavior> {
            return data.values.iterator()
        }
    }

    private class BuilderImpl : Builder {
        private val data: Reference2ReferenceMap<ItemBehaviorType<*>, ItemBehavior> = Reference2ReferenceArrayMap()

        override fun <T : ItemBehavior> put(type: ItemBehaviorType<T>, behavior: T) {
            data.put(type, behavior)
        }

        override fun build(): ItemBehaviorMap {
            return MapImpl(data)
        }
    }
}
