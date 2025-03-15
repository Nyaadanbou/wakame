package cc.mewcraft.wakame.item2.behavior

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
            return EmptyItemBehaviorContainer
        }

        /**
         * 获取一个 builder.
         */
        fun build(block: Builder.() -> Unit): ItemBehaviorContainer {
            return ItemBehaviorContainerImpl().apply(block).build()
        }
    }

    /**
     * 检查该容器是否有指定的 [ItemBehavior].
     */
    fun has(type: ItemBehavior): Boolean

    /**
     * [ItemBehaviorContainer] 的生成器, 用于构建一个 [ItemBehaviorContainer].
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

}

private data object EmptyItemBehaviorContainer : ItemBehaviorContainer {
    private val empty = emptyList<ItemBehavior>()
    override fun has(type: ItemBehavior): Boolean = false
    override fun iterator(): Iterator<ItemBehavior> = empty.iterator()
    override fun toString(): String = toSimpleString()
}

// 该 class 同时实现了 ItemBehaviorContainer 和 ItemBehaviorContainer.Builder
private class ItemBehaviorContainerImpl(
    private val data: ReferenceArraySet<ItemBehavior> = ReferenceArraySet(),
) : ItemBehaviorContainer, Builder {
    override fun has(type: ItemBehavior): Boolean {
        return data.contains(type)
    }

    override fun put(behavior: ItemBehavior) {
        data.add(behavior)
    }

    override fun build(): ItemBehaviorContainer {
        return this
    }

    override fun iterator(): Iterator<ItemBehavior> {
        return this.data.iterator()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("data", data)
    )

    override fun toString(): String = toSimpleString()
}
