package cc.mewcraft.wakame.item2.behavior

import cc.mewcraft.wakame.item2.behavior.ItemBehaviorContainer.Builder
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.adventure.toSimpleString
import it.unimi.dsi.fastutil.objects.ReferenceArraySet
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 代表一个容器, 存放关于 *物品类型* 的行为逻辑.
 */
sealed interface ItemBehaviorContainer : Iterable<ItemBehavior>, Examinable {

    /**
     * [ItemBehaviorContainer] 的构造函数.
     */
    companion object {

        @JvmField
        val EMPTY: ItemBehaviorContainer = EmptyItemBehaviorContainer

        /**
         * 获取一个 [TypeSerializer2] 用于序列化 [ItemBehaviorContainer].
         */
        fun makeDirectSerializer(): TypeSerializer2<ItemBehaviorContainer> {
            return SimpleItemBehaviorContainer.Serializer
        }

        /**
         * 构建一个 [ItemBehaviorContainer].
         */
        fun build(block: Builder.() -> Unit): ItemBehaviorContainer {
            return builder().apply(block).build()
        }

        /**
         * 获取一个 [Builder] 用来创建 [ItemBehaviorContainer].
         */
        fun builder(): Builder {
            return SimpleItemBehaviorContainer()
        }
    }

    /**
     * 检查该容器是否有指定的 [ItemBehavior].
     */
    infix fun has(type: ItemBehavior): Boolean

    /**
     * 检查该容器是否有指定的 [ItemBehavior].
     */
    operator fun contains(type: ItemBehavior): Boolean = has(type)

    /**
     * [ItemBehaviorContainer] 的生成器, 用于构建一个 [ItemBehaviorContainer].
     */
    sealed interface Builder {

        /**
         * 添加一个 [ItemBehavior]. 已存在的 [behavior] 会被覆盖.
         */
        fun add(behavior: ItemBehavior)

        /**
         * 添加一个 [ItemBehavior]. 已存在的 [behavior] 会被覆盖.
         */
        operator fun plusAssign(behavior: ItemBehavior) = add(behavior)

        /**
         * 构建.
         */
        fun build(): ItemBehaviorContainer

    }

}

// ------------
// 内部实现
// ------------

private data object EmptyItemBehaviorContainer : ItemBehaviorContainer {
    private val empty: List<ItemBehavior> = emptyList<ItemBehavior>()
    override fun has(type: ItemBehavior): Boolean = false
    override fun iterator(): Iterator<ItemBehavior> = empty.iterator()
    override fun toString(): String = toSimpleString()
}

// 该 class 同时实现了 ItemBehaviorContainer 和 ItemBehaviorContainer.Builder
private class SimpleItemBehaviorContainer(
    private val behaviorMap: ReferenceArraySet<ItemBehavior> = ReferenceArraySet(),
) : ItemBehaviorContainer, Builder {
    override fun has(type: ItemBehavior): Boolean {
        return behaviorMap.contains(type)
    }

    override fun add(behavior: ItemBehavior) {
        behaviorMap.add(behavior)
    }

    override fun build(): ItemBehaviorContainer {
        return if (behaviorMap.isEmpty()) ItemBehaviorContainer.EMPTY else this
    }

    override fun iterator(): Iterator<ItemBehavior> {
        return behaviorMap.iterator()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("behaviorMap", behaviorMap)
    )

    override fun toString(): String = toSimpleString()

    object Serializer : TypeSerializer2<ItemBehaviorContainer> {
        override fun deserialize(type: Type, node: ConfigurationNode): ItemBehaviorContainer {
            val builder = ItemBehaviorContainer.builder()
            for ((rawNodeKey, _) in node.childrenMap()) {
                // 实现上只要 Node 存在那么 ItemBehavior 就存在
                val nodeKey = rawNodeKey.toString()
                val dataValue = BuiltInRegistries.ITEM_BEHAVIOR[nodeKey] ?: continue
                builder += dataValue
            }
            return builder.build()
        }

        override fun emptyValue(specificType: Type, options: ConfigurationOptions): ItemBehaviorContainer? {
            return ItemBehaviorContainer.EMPTY
        }
    }
}
