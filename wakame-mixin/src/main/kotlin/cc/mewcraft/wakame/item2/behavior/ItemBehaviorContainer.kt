package cc.mewcraft.wakame.item2.behavior

import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.item2.behavior.ItemBehaviorContainer.Builder
import cc.mewcraft.wakame.util.adventure.toSimpleString
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.Collections.emptyList
import java.util.stream.Stream

interface ItemBehaviorContainer : Iterable<ItemBehavior>, Examinable {

    /**
     * [ItemBehaviorContainer] 的构造函数.
     */
    companion object {
        /**
         * 获取一个空的 [ItemBehaviorContainer].
         */
        fun empty(): ItemBehaviorContainer {
            return Empty
        }

        /**
         * 获取一个 builder.
         */
        fun build(block: Builder.() -> Unit): ItemBehaviorContainer {
            return ItemBehaviorContainerBuilderImpl().apply(block).build()
        }
    }

    /**
     * Checks whether this [KoishItem] has specific [ItemBehavior].
     */
    fun <T : ItemBehavior> has(type: ItemBehaviorType<T>): Boolean

    /**
     * Gets the specific [ItemBehavior] if this [KoishItem] has it.
     */
    fun <T : ItemBehavior> get(type: ItemBehaviorType<T>): T?

    /**
     * [ItemBehaviorContainer] 的 builder, 用于构建一个 [ItemBehaviorContainer].
     */
    interface Builder : Examinable {
        /**
         * 添加一个 [ItemBehavior]. 已存在的 [type] 会被覆盖.
         */
        fun <T : ItemBehavior> put(type: ItemBehaviorType<T>, behavior: T)

        /**
         * 构建.
         */
        fun build(): ItemBehaviorContainer
    }

    private object Empty : ItemBehaviorContainer {

        private val empty = emptyList<ItemBehavior>()
        override fun <T : ItemBehavior> has(type: ItemBehaviorType<T>): Boolean = false
        override fun <T : ItemBehavior> get(type: ItemBehaviorType<T>): T? = null
        override fun iterator(): Iterator<ItemBehavior> = empty.iterator()
        override fun toString(): String = toSimpleString()

    }
}

private class ItemBehaviorContainerImpl(
    data: Map<ItemBehaviorType<*>, ItemBehavior>,
) : ItemBehaviorContainer {
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

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("data", data)
    )

    override fun toString(): String = toSimpleString()
}

private class ItemBehaviorContainerBuilderImpl : Builder {
    private val data: Reference2ReferenceMap<ItemBehaviorType<*>, ItemBehavior> = Reference2ReferenceArrayMap()

    override fun <T : ItemBehavior> put(type: ItemBehaviorType<T>, behavior: T) {
        data.put(type, behavior)
    }

    override fun build(): ItemBehaviorContainer {
        return ItemBehaviorContainerImpl(data)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("data", data)
    )

    override fun toString(): String = toSimpleString()
}
