package cc.mewcraft.wakame.item2.behavior

import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.item2.behavior.ItemBehaviorContainer.Builder
import cc.mewcraft.wakame.util.adventure.toSimpleString
import it.unimi.dsi.fastutil.objects.ReferenceArraySet
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
    fun has(type: ItemBehavior): Boolean

    /**
     * [ItemBehaviorContainer] 的 builder, 用于构建一个 [ItemBehaviorContainer].
     */
    interface Builder : Examinable {
        /**
         * 添加一个 [ItemBehavior]. 已存在的 [behavior] 会被覆盖.
         */
        fun put(behavior: ItemBehavior)

        /**
         * 构建.
         */
        fun build(): ItemBehaviorContainer
    }

    private object Empty : ItemBehaviorContainer {

        private val empty = emptyList<ItemBehavior>()
        override fun has(type: ItemBehavior): Boolean = false
        override fun iterator(): Iterator<ItemBehavior> = empty.iterator()
        override fun toString(): String = toSimpleString()

    }

}

private class ItemBehaviorContainerImpl(
    data: Set<ItemBehavior>,
) : ItemBehaviorContainer {
    private val data: ReferenceArraySet<ItemBehavior> = ReferenceArraySet(data)

    override fun has(type: ItemBehavior): Boolean {
        return data.contains(type)
    }

    override fun iterator(): Iterator<ItemBehavior> {
        return this.data.iterator()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("data", data)
    )

    override fun toString(): String = toSimpleString()
}

private class ItemBehaviorContainerBuilderImpl : Builder {
    private val data: ReferenceArraySet<ItemBehavior> = ReferenceArraySet()

    override fun put(behavior: ItemBehavior) {
        data.add(behavior)
    }

    override fun build(): ItemBehaviorContainer {
        return ItemBehaviorContainerImpl(data)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("data", data)
    )

    override fun toString(): String = toSimpleString()
}
